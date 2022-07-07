/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apushnikov.sommelier_notebook.billing.ui;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SingleMediatorLiveEvent<T> - Наблюдаемая с учетом жизненного цикла, которая отправляет только
 * новые обновления после подписки, используется для таких событий, как навигация и
 * сообщения Snackbar. Его также можно использовать как MediatorLiveData для преобразования других
 * событий SingleMediatorLiveEvents.
 * <p>
 * Это позволяет избежать общей проблемы с событиями: при изменении конфигурации
 * (например, при вращении) может быть выпущено обновление, если наблюдатель активен.
 * LiveData вызывает наблюдаемое только в том случае, если есть явный вызов setValue () или call ().
 * <p>
 * Обратите внимание, что только один наблюдатель будет уведомлен об изменениях.
 * <p>
 * <p>
 * MediatorLiveData<T> - Подкласс LiveData, который может наблюдать за другими объектами LiveData и
 * реагировать на их события OnChanged.
 * <p>
 * Этот класс правильно передает свое активное / неактивное состояние на исходные объекты LiveData.
 * <p>
 * Рассмотрим следующий сценарий: у нас есть 2 экземпляра LiveData, назовем их liveData1 и liveData2,
 * и мы хотим объединить их выбросы в один объект: liveDataMerger.
 * Затем liveData1 и liveData2 станут источниками для MediatorLiveData liveDataMerger, и каждый раз,
 * когда для любого из них вызывается обратный вызов onChanged, мы устанавливаем новое значение
 * в liveDataMerger.
 *    LiveData liveData1 = ...;
 *    LiveData liveData2 = ...;
 *
 *    MediatorLiveData liveDataMerger = новый MediatorLiveData <> ();
 *    liveDataMerger.addSource (liveData1, значение -> liveDataMerger.setValue (значение));
 *    liveDataMerger.addSource (liveData2, значение -> liveDataMerger.setValue (значение));
 *
 * Предположим, мы хотим, чтобы только 10 значений, испускаемых liveData1, были объединены в liveDataMerger.
 * Затем, после 10 значений, мы можем перестать слушать liveData1 и удалить его как источник.
 *    liveDataMerger.addSource (liveData1, new Observer () {
 *         частное число int = 1;
 *
 *          @Override public void onChanged (@Nullable Integer s) {
 *             count ++;
 *             liveDataMerger.setValue (s);
 *             if (count> 10) {
 *                 liveDataMerger.removeSource (liveData1);
 *             }
 *         }
 *    });
 *
 *
 *
 * A lifecycle-aware observable that sends only new updates after subscription, used for events like
 * navigation and Snackbar messages. This can also be used like a MediatorLiveData to transform
 * other SingleMediatorLiveEvents.
 * <p>
 * This avoids a common problem with events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to setValue() or call().
 * <p>
 * Note that only one observer is going to be notified of changes.
 */
public class SingleMediatorLiveEvent<T> extends MediatorLiveData<T> {

    private static final String TAG = "SingleMediatorLiveEvent";

    /**mPending - В ожидании (Типа AtomicBoolean) - Логическое значение, которое может обновляться атомарно.
     * См. Описание свойств атомарных переменных в спецификации пакета java.util.concurrent.atomic.
     * AtomicBoolean используется в таких приложениях, как атомарно обновляемые флаги, и
     * не может использоваться в качестве замены Boolean
     */
    private final AtomicBoolean mPending = new AtomicBoolean(false);

    /**=====================================================================================
     * observe - наблюдатель
     *
     * @param owner     Класс, имеющий жизненный цикл Android. Эти события могут использоваться
     *                  настраиваемыми компонентами для обработки изменений жизненного цикла
     *                  без реализации какого-либо кода внутри Activity или Fragment.
     * @param observer  Простой обратный вызов, который можно получить от LiveData.
     * =====================================================================================
     */
    @MainThread
    public void observe(@NonNull LifecycleOwner owner,
                        @NonNull final Observer<? super T> observer) {

        // Возвращает true, если у LiveData есть активные наблюдатели.
        if (hasActiveObservers()) {
            Log.w(TAG, "Зарегистрировано несколько наблюдателей, но только один будет уведомлен об изменениях.");
        }

        // Observe the internal MutableLiveData
        // Обратите внимание на внутренний MutableLiveData
        //
        // super.observe - Добавляет данного наблюдателя в список наблюдателей в течение срока
        // жизни данного владельца. События отправляются в основном потоке.
        // Если LiveData уже имеет набор данных, он будет доставлен наблюдателю.
        // Наблюдатель будет получать события, только если владелец находится в состоянии
        // Lifecycle.State.STARTED или Lifecycle.State.RESUMED (активен).
        // Если владелец переходит в состояние Lifecycle.State.DESTROYED, наблюдатель будет
        // автоматически удален.
        // Параметры:
        //      owner - LifecycleOwner, который управляет наблюдателем
        //      t - Наблюдатель, который будет получать события
        super.observe(owner, t -> {
            if (mPending.compareAndSet(true, false)) {
                // onChanged(t) - Вызывается при изменении данных.
                // Параметры:
                //      t - Новые данные
                observer.onChanged(t);
            }
        });
    }

    /**=======================================================================================
     * setValue - Устанавливает значение. Если есть активные наблюдатели, значение будет отправлено им
     *
     * @param t Новое значение
     * =======================================================================================
     */
    @MainThread
    public void setValue(@Nullable T t) {
        // Для mPending (В ожидании) Безоговорочно устанавливается на заданное значение (true)
        mPending.set(true);
        // super.setValue(t) - Описание скопировано из класса:
        // androidx.lifecycle.LiveData Устанавливает значение. Если есть активные наблюдатели,
        // значение будет отправлено им.
        // Этот метод необходимо вызывать из основного потока. Если вам нужно установить значение
        // из фонового потока, вы можете использовать postValue (Object)
        // Заменяет:
        //      setValue в классе LiveData
        //  Параметры:
        //      value - Новое значение
        super.setValue(t);
    }

    /**=======================================================================================
     * Используется в случаях, когда T равно Void, чтобы сделать звонки более чистыми.
     * <p>
     * Used for cases where T is Void, to make calls cleaner.
     * =======================================================================================
     */
    @MainThread
    public void call() {
        setValue(null);
    }
}