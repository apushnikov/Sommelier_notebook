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

package com.apushnikov.sommelier_notebook.billing;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.Transformations;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.apushnikov.sommelier_notebook.billing.ui.SingleMediatorLiveEvent;
import com.apushnikov.sommelier_notebook.log_file.LogFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * =========================================================================================
 * BillingDataSource реализует все функции выставления счетов для нашего тестового приложения.
 * Покупки могут происходить как В ПРИЛОЖЕНИИ, так и в любое время ВНЕ ПРИЛОЖЕНИЯ,
 * поэтому BillingDataSource должен это учитывать.
 * <p>
 * Поскольку каждый SKU может иметь индивидуальное состояние, все SKU имеют связанные LiveData,
 * позволяющие наблюдать за их состоянием.
 * <p>
 * Этот BillingDataSource ничего не знает о приложении;
 * вся необходимая информация либо передается в конструктор, либо экспортируется
 * как наблюдаемые LiveData, либо экспортируется через обратные вызовы.
 * Этот код можно повторно использовать в различных приложениях.
 * <p>
 * При этом, если вы используете Kotlin с сопрограммами, нет причин иметь LiveData на этом уровне.
 * То же самое, если вы используете RxJava.
 * Это служит необходимости отделения конечного автомата биллинга от логики игры,
 * которая в основном реализована в репозитории.
 * <p>
 * Начало процесса покупки включает передачу действия в библиотеку биллинга,
 * но мы просто передаем его в API.
 * <p>
 * Этот источник данных имеет несколько автоматических функций:
 * 1) Он проверяет наличие действительной подписи на всех покупках,
 * прежде чем пытаться их подтвердить.
 * 2) Он автоматически подтверждает все известные SKU для нерасходованных материалов и
 * не устанавливает состояние «куплено» до тех пор, пока подтверждение не будет завершено.
 * 3) Источник данных будет автоматически использовать skus, которые установлены
 * в unknownAutoConsumeSKU.
 * По мере использования SKU будет инициировано событие SingleLiveEvent для одного наблюдателя.
 * 4) Если BillingService отключен, он попытается повторно подключиться с экспоненциальным откатом.
 * <p>
 * Этот источник данных пытается сохранить специфические знания библиотеки биллинга в
 * пределах этого файла;
 * Единственное, что необходимо знать клиентам BillingDataSource, - это номера SKU,
 * используемые их приложением.
 * <p>
 * BillingClient требуется доступ к контексту приложения для привязки службы удаленного биллинга.
 * <p>
 * BillingDataSource также может действовать как LifecycleObserver для Activity;
 * это позволяет обновлять покупки во время onResume.
 * <p>
 * The BillingDataSource implements all billing functionality for our test application. Purchases
 * can happen while in the app or at any time while out of the app, so the BillingDataSource has to
 * account for that.
 * <p>
 * Since every SKU can have an individual state, all SKUs have an associated LiveData to allow their
 * state to be observed.
 * <p>
 * This BillingDataSource knows nothing about the application; all necessary information is either
 * passed into the constructor, exported as observable LiveData, or exported through callbacks. This
 * code can be reused in a variety of apps.
 * <p>
 * That being said, if you're using Kotlin with coroutines, there's no reason to have LiveData at
 * this layer. Same thing if you're using RxJava. This serves the need of decoupling the billing
 * state machine from the logic of the game, which is mostly implemented in the repository.
 * <p>
 * Beginning a purchase flow involves passing an Activity into the Billing Library, but we merely
 * pass it along to the API.
 * <p>
 * This data source has a few automatic features: 1) It checks for a valid signature on all
 * purchases before attempting to acknowledge them. 2) It automatically acknowledges all known SKUs
 * for non-consumables, and doesn't set the state to purchased until the acknowledgement is
 * complete. 3) The data source will automatically consume skus that are set in
 * knownAutoConsumeSKUs. As SKUs are consumed, a SingleLiveEvent will be triggered for a single
 * observer. 4) If the BillingService is disconnected, it will attempt to reconnect with exponential
 * fallback.
 * <p>
 * This data source attempts to keep billing library specific knowledge confined to this file; The
 * only thing that clients of the BillingDataSource need to know are the SKUs used by their
 * application.
 * <p>
 * The BillingClient needs access to the Application context in order to bind the remote billing
 * service.
 * <p>
 * The BillingDataSource can also act as a LifecycleObserver for an Activity; this allows it to
 * refresh purchases during onResume.
 * ==========================================================================================
 */
public class BillingDataSource implements
        LifecycleObserver,
        PurchasesUpdatedListener,
        BillingClientStateListener,
        SkuDetailsResponseListener {


    /**
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    private LogFile logFile;


    private static final String TAG = "TrivialDrive:" + BillingDataSource.class.getSimpleName();

    /**
     * Начало переподключения таймера в миллисекундах
     */
    private static final long RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L;
    /**
     * Максимальное время переподключения таймера в миллисекундах
     */
    private static final long RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L; // 15 минут
    /**
     * Время перезапроса для SKU деталей в миллисекундах
     */
    private static final long SKU_DETAILS_REQUERY_TIME = 1000L * 60L * 60L * 4L; // 4 часа

    // Handler - Обработчик позволяет отправлять и обрабатывать объекты Message и Runnable,
    // связанные с MessageQueue потока. Каждый экземпляр Handler связан с одним потоком и
    // очередью сообщений этого потока. Когда вы создаете новый обработчик, он привязывается
    // к луперу. Он будет доставлять сообщения и исполняемые файлы в очередь сообщений этого
    // Looper и выполнять их в потоке этого Looper.
    // У Handler есть два основных применения:
    // (1) для планирования сообщений и исполняемых файлов, которые должны быть выполнены в
    // какой-то момент в будущем; и
    // (2) поставить в очередь действие, которое будет выполняться в другом потоке,
    // чем ваш собственный
    /**
     * handler - Обработчик позволяет отправлять и обрабатывать объекты Message и Runnable
     */
    private static final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * sInstance - экземпляр класса BillingDataSource, volatile - Т.е. для разных потоков ОДНА переменная
     */
    private static volatile BillingDataSource sInstance;
    /**
     * billingSetupComplete - Это флаг, обзначающий готовность биллингова клиента
     */
    private boolean billingSetupComplete = false;
    // Billing client, connection, cached data
    /**
     * billingClient - Клиент биллинга, подключение, кешированные данные
     */
    private final BillingClient billingClient;
    // known SKUs (used to query sku data and validate responses)
    /**
     * knownInappSKUs - известные SKU (используются для запроса данных SKU и проверки ответов)
     */
    final private List<String> knownInappSKUs;
    /**
     * knownSubscriptionSKUs - Артикулы подписок, о которых должен знать источник
     */
    final private List<String> knownSubscriptionSKUs;
    // SKUs to auto-consume
    /**
     * knownAutoConsumeSKUs - Артикулы для автоматического потребления
     */
    final private Set<String> knownAutoConsumeSKUs;

    // LiveData that is mostly maintained so it can be transformed into observables.
    /**
     * skuStateMap - LiveData, которая в основном поддерживается, поэтому ее можно преобразовать в наблюдаемые.
     * <p>
     * Тип skuStateMap: Map(String, MutableLiveData(SkuState)).
     * В Map мы добавляем не отдельные объекты, а пары объектов (ключ, значение).
     * Методы: put(Object key, Object value) - установить пару, get() - извлечь по ключу
     * <p>
     * ключ - String, значение - MutableLiveData(SkuState))
     * <p>
     * MutableLiveData - LiveData, который публично предоставляет методы setValue (T) и postValue (T).
     * <p>
     * setValue (T) - LiveData Устанавливает значение. Этот метод необходимо вызывать из основного потока
     * <p>
     * postValue (T) - LiveData Устанавливает значение. Этот метод необходимо вызывать из фонового потока
     */
    final private Map<String, MutableLiveData<SkuState>> skuStateMap = new HashMap<>();
    /**
     * skuDetailsLiveDataMap - LiveData, которая в основном поддерживается, поэтому ее можно преобразовать в наблюдаемые.
     */
    final private Map<String, MutableLiveData<SkuDetails>> skuDetailsLiveDataMap = new HashMap<>();

    // Observables that are used to communicate state.
    /**
     * purchaseConsumptionInProcess - Наблюдаемые, которые используются для передачи состояния.
     */
    final private Set<Purchase> purchaseConsumptionInProcess = new HashSet<>();
    /**
     * newPurchase - Это единичное мероприятие в прямом эфире, посвященное новым покупкам.
     * Эти покупки могут быть результатом потока выставления счетов или из другого источника.
     * <p>
     * SingleMediatorLiveEvent - Наблюдаемая с учетом жизненного цикла, которая отправляет
     * только новые обновления после подписки, используется для таких событий, как навигация
     * и сообщения Snackbar. Его также можно использовать как MediatorLiveData для
     * преобразования других событий SingleMediatorLiveEvents.
     * <p>
     * Это позволяет избежать общей проблемы с событиями: при изменении конфигурации
     * (например, при вращении) может быть выпущено обновление, если наблюдатель активен.
     * LiveData вызывает наблюдаемое только в том случае, если есть явный вызов setValue ()
     * или call ().
     * Обратите внимание, что только один наблюдатель будет уведомлен об изменениях.
     */
    final private SingleMediatorLiveEvent<List<String>> newPurchase = new SingleMediatorLiveEvent<>();
    /**
     * purchaseConsumed - Это единственное событие в реальном времени, которое наблюдает
     * за потребленными покупками при вызове метода потребления
     */
    final private SingleMediatorLiveEvent<List<String>> purchaseConsumed =
            new SingleMediatorLiveEvent<>();
    /**
     * billingFlowInProcess - платежный процесс в процессе
     * <p>
     * MutableLiveData<Boolean> - LiveData, который публично предоставляет методы setValue (T)
     * и postValue (T).
     * <p>
     * postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
     * <p>
     * setValue (T) - Устанавливает значение
     * <p>
     * Параметры типа:
     * <p>
     * <T> - Тип данных, хранимых этим экземпляром
     */
    final private MutableLiveData<Boolean> billingFlowInProcess = new MutableLiveData<>();
    // how long before the data source tries to reconnect to Google play
    /**
     * reconnectMilliseconds - как скоро источник данных попытается повторно подключиться к Google play
     */
    private long reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS;
    // when was the last successful SkuDetailsResponse?
    /**
     * skuDetailsResponseTime - когда был последний успешный ответ SkuDetailsResponse?
     */
    private long skuDetailsResponseTime = -SKU_DETAILS_REQUERY_TIME;

    /**
     * =================================================================================
     * Наш конструктор. Поскольку мы синглтон, это используется только для внутреннего
     * использования.
     *
     * @param application           Класс приложения Android.
     * @param knownInappSKUs        Артикулы покупок в приложении, о которых должен знать источник
     * @param knownSubscriptionSKUs Артикулы подписок, о которых должен знать источник
     * @param autoConsumeSKUs       Артикулы для автоматического потребления
     *                              =================================================================================
     */
    private BillingDataSource(
            @NonNull Application application,
            String[] knownInappSKUs,
            String[] knownSubscriptionSKUs,
            String[] autoConsumeSKUs,
            LogFile logFile) {

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
//        logFile.writeLogFile("BillingDataSource: Начало private BillingDataSource");
        this.logFile = logFile;


        // Присваиваем knownInappSKUs - известные SKU (используются для запроса данных SKU и проверки ответов)
        // Если knownInappSKUs (Артикулы покупок в приложении, о которых должен знать источник) == null
        // то присваивается новый пустой ArrayList<>
        // иначе Возвращает список фиксированного размера, поддерживаемый указанным массивом.
        // (Изменения в возвращаемом списке «сквозная запись» в массив.)
        // Этот метод действует как мост между API на основе массива и на основе коллекции
        // в сочетании с Collection # toArray.
        // Возвращенный список является сериализуемым и реализует RandomAccess
        this.knownInappSKUs = knownInappSKUs == null ? new ArrayList<>() : Arrays.asList(
                knownInappSKUs);
        // Присваиваем knownSubscriptionSKUs - Артикулы подписок, о которых должен знать источник
        this.knownSubscriptionSKUs =
                knownSubscriptionSKUs == null ? new ArrayList<>() : Arrays.asList(
                        knownSubscriptionSKUs);
        // Присваиваем knownAutoConsumeSKUs - Артикулы для автоматического потребления
        knownAutoConsumeSKUs = new HashSet<>();
        if (autoConsumeSKUs != null) {
            knownAutoConsumeSKUs.addAll(Arrays.asList(autoConsumeSKUs));
        }
        // Создаем Клиент биллинга
        // Инициирует процесс выставления счетов для покупки в приложении или подписки.
        // Появится экран покупки в Google Play.
        // Результат будет доставлен через реализацию интерфейса PurchasesUpdatedListener,
        // установленную BillingClient.Builder.setListener (PurchasesUpdatedListener).
        billingClient = BillingClient.newBuilder(application).setListener(
                this).enablePendingPurchases().build();
        // Асинхронно запускает процесс настройки BillingClient.
        // Вы получите уведомление через прослушиватель BillingClientStateListener,
        // когда процесс настройки будет завершен (вызовется метод onBillingSetupFinished)
        billingClient.startConnection(this);
        // Создает объект LiveData для каждого известного SKU, чтобы сведения о состоянии
        // и SKU можно было наблюдать на других уровнях
        initializeLiveData();

    }

    /**
     * =====================================================================================
     * getInstance - создаем экземпляр синглтона BillingDataSource
     * <p>
     * Стандартный шаблонный шаблон блокировки с двойной проверкой для
     * ниточно-безопасных синглтонов.
     * <p>
     * Standard boilerplate double check locking pattern for thread-safe singletons.
     *
     * @param application           Класс приложения Android.
     * @param knownInappSKUs        Артикулы покупок в приложении, о которых должен знать источник
     * @param knownSubscriptionSKUs Артикулы подписок, о которых должен знать источник
     * @param autoConsumeSKUs       Артикулы для автоматического потребления
     *                              =====================================================================================
     */
    public static BillingDataSource getInstance(
            @NonNull Application application,
            String[] knownInappSKUs,
            String[] knownSubscriptionSKUs,
            String[] autoConsumeSKUs,
            LogFile logFile) {

        // Если еще не создан экземпляр класса BillingDataSource
        if (sInstance == null) {
            // Ключевое слово synchronized используется для указания того, что метод может быть
            // доступен только одним потоком одновременно
            synchronized (BillingDataSource.class) {
                // Если еще не создан экземпляр класса BillingDataSource
                if (sInstance == null) {
                    // Создаем экземпляр класса BillingDataSource
                    sInstance = new BillingDataSource(
                            application,
                            knownInappSKUs,
                            knownSubscriptionSKUs,
                            autoConsumeSKUs,
                            logFile);
                }
            }
        }

        return sInstance;
    }

    /**
     * ======================================================================================
     * onBillingSetupFinished - метод интерфейса BillingClientStateListener
     * <p>
     * onBillingSetupFinished - Вызывается для уведомления о завершении настройки
     *
     * @param billingResult Параметры, содержащие код ответа и сообщение отладки из
     *                      ответа In-app Billing API.
     *                      ======================================================================================
     */
    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {

        // Код ответа, возвращаемый при вызовах In-app Billing API
        int responseCode = billingResult.getResponseCode();
        // Сообщение отладки возвращается при вызовах In-app Billing API.
        String debugMessage = billingResult.getDebugMessage();

        Log.d(TAG, "onBillingSetupFinished: " + responseCode + " " + debugMessage);
        switch (responseCode) {
            case BillingClient.BillingResponseCode.OK:  // Если успешно
                // Биллинговый клиент готов. Здесь вы можете запросить покупки.
                // Это не означает, что ваше приложение правильно настроено в консоли -
                // это просто означает, что у вас есть соединение со службой биллинга.
                // The billing client is ready. You can query purchases here.
                // This doesn't mean that your app is set up correctly in the console -- it just
                // means that you have a connection to the Billing service.

                // reconnectMilliseconds - как скоро источник данных попытается повторно
                // подключиться к Google play
                reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS;
                // Выставляем флаг - биллинговый клиент успешно готов
                billingSetupComplete = true;
                // Вызывает функции биллинг-клиента для запроса сведений о SKU как для
                // встроенного приложения, так и для SKU подписки. Детали SKU полезны для
                // отображения названий товаров и прайс-листов для пользователя и необходимы
                // для совершения покупки
                // Потом вызывается метод onSkuDetailsResponse (это метод интерфейса
                // SkuDetailsResponseListener - Вызывается для уведомления о завершении
                // операции получения сведений о SKU)
                querySkuDetailsAsync();
                // refreshPurchasesAsync - асинхронный запрос покупок
                refreshPurchasesAsync();
                break;
            default:                                    // Во всех остальных случаях
                retryBillingServiceConnectionWithExponentialBackoff();
                break;
        }

    }

    /**
     * ======================================================================================
     * onBillingServiceDisconnected - метод интерфейса BillingClientStateListener
     * <p>
     * onBillingServiceDisconnected - Вызывается для уведомления о потере связи с биллинговой службой.
     * <p>
     * Примечание. Это не удаляет само соединение биллинговой службы - эта привязка к сервису
     * останется активной, и вы получите вызов onBillingSetupFinished (BillingResult),
     * когда биллинговый сервис будет запущен в следующий раз и установка будет завершена.
     * <p>
     * Это довольно необычное явление. Это происходит в первую очередь, если Google Play Store
     * обновляется самостоятельно или принудительно закрывается.
     * <p>
     * This is a pretty unusual occurrence. It happens primarily if the Google Play Store
     * self-upgrades or is force closed.
     * ======================================================================================
     */
    @Override
    public void onBillingServiceDisconnected() {

        // Выставляем флаг - биллинговый клиент НЕ ГОТОВ
        billingSetupComplete = false;
        // Повторяет соединение биллинговой службы с экспоненциальной отсрочкой, достигая
        // максимального значения во время, указанное в RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
        retryBillingServiceConnectionWithExponentialBackoff();

    }

    /**
     * =====================================================================================
     * Повторяет соединение биллинговой службы с экспоненциальной отсрочкой, достигая
     * максимального значения во время, указанное в RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
     * <p>
     * Retries the billing service connection with exponential backoff, maxing out at the time
     * specified by RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
     * =====================================================================================
     */
    private void retryBillingServiceConnectionWithExponentialBackoff() {

        // postDelayed - Заставляет Runnable r быть добавленным в очередь сообщений для
        // запуска по истечении указанного времени.
        handler.postDelayed(() ->
                        // Асинхронно запускает процесс настройки BillingClient.
                        // Вы получите уведомление через прослушиватель BillingClientStateListener,
                        // когда процесс настройки будет завершен (вызовется метод onBillingSetupFinished)
                        billingClient.startConnection(BillingDataSource.this),
                reconnectMilliseconds);
        reconnectMilliseconds = Math.min(reconnectMilliseconds * 2,
                RECONNECT_TIMER_MAX_TIME_MILLISECONDS);

    }

    /**
     * ======================================================================================
     * Вызывается функцией initializeLiveData для создания различных объектов LiveData,
     * которые мы планируем создать.
     *
     * @param skuList Список <String> артикулов, представляющих покупки и подписки.
     *                ======================================================================================
     */
    private void addSkuLiveData(List<String> skuList) {

        // Цикл по sku в skuList
        for (String sku : skuList) {
            // MutableLiveData<T> - LiveData, который публично предоставляет методы setValue (T)
            // и postValue (T).
            // <T> - Тип данных, хранимых этим экземпляром
            MutableLiveData<SkuState> skuState = new MutableLiveData<>();
            // SkuDetails - Представляет сведения о продукте или подписке для продажи в приложении.
            MutableLiveData<SkuDetails> details = new MutableLiveData<SkuDetails>() {
                @Override
                protected void onActive() {
                    // SystemClock.elapsedRealtime() - Возвращает миллисекунды с момента загрузки,
                    // включая время, проведенное в спящем режиме.
                    // Если с момента последней фиксации времени прошло больше
                    // SKU_DETAILS_REQUERY_TIME (Время перезапроса для SKU деталей в миллисекундах)
                    if (SystemClock.elapsedRealtime() - skuDetailsResponseTime
                            > SKU_DETAILS_REQUERY_TIME) {
                        // Фиксируем время (отсчитивается с момента загрузки)
                        skuDetailsResponseTime = SystemClock.elapsedRealtime();
                        Log.v(TAG, "Артикул не свежий, требуется requerying");
                        // Вызывает функции биллинг-клиента для запроса сведений о SKU как для
                        // встроенного приложения, так и для SKU подписки.
                        // Детали SKU полезны для отображения названий товаров и прайс-листов
                        // для пользователя и необходимы для совершения покупки.
                        // Потом вызывается метод onSkuDetailsResponse (это метод интерфейса
                        // SkuDetailsResponseListener - Вызывается для уведомления о завершении
                        // операции получения сведений о SKU)
                        querySkuDetailsAsync();
                    }

                }
            };
            // skuStateMap - LiveData, которая в основном поддерживается, поэтому ее можно
            // преобразовать в наблюдаемые.
            // put - Связывает указанное значение с указанным ключом на этой карте
            // (необязательная операция).
            skuStateMap.put(sku, skuState);
            skuDetailsLiveDataMap.put(sku, details);
        }

    }

    /**
     * ======================================================================================
     * Создает объект LiveData для каждого известного SKU, чтобы сведения о состоянии и SKU
     * можно было наблюдать на других уровнях.
     * <p>
     * Репозиторий отвечает за отображение этих данных более удобными для приложения способами.
     * <p>
     * Creates a LiveData object for every known SKU so the state and SKU details can be observed in
     * other layers. The repository is responsible for mapping this data in ways that are more
     * useful for the application.
     * ======================================================================================
     */
    private void initializeLiveData() {

        // Создаются различных объектов LiveData. Параметр - knownInappSKUs
        addSkuLiveData(knownInappSKUs);
        // Создаются различных объектов LiveData. Параметр - knownSubscriptionSKUs
        addSkuLiveData(knownSubscriptionSKUs);
        // setValue (T) - Устанавливает значение
        // Для платежного процесса устанавливаем значение false
        billingFlowInProcess.setValue(false);

    }

    /**
     * ======================================================================================
     * Это единичное мероприятие в прямом эфире, посвященное новым покупкам.
     * Эти покупки могут быть результатом потока выставления счетов или из другого источника.
     * <p>
     * This is a single live event that observes new purchases. These purchases can be the result of
     * a billing flow or from another source.
     *
     * @return LiveData, который содержит артикул новой покупки.
     * ======================================================================================
     */
    public final LiveData<List<String>> observeNewPurchases() {
        return newPurchase;
    }

    /**
     * ======================================================================================
     * Это единственное событие в реальном времени, которое наблюдает за потребленными покупками
     * при вызове метода потребления.
     * <p>
     * This is a single live event that observes consumed purchases from calling the consume
     * method.
     *
     * @return LiveData, который содержит артикул потребленной покупки.
     * =======================================================================================
     */
    public final LiveData<List<String>> observeConsumedPurchases() {
        return purchaseConsumed;
    }

    /**
     * =======================================================================================
     * Возвращает, приобрел ли пользователь артикул. Он делает это, возвращая MediatorLiveData,
     * который возвращает истину, если SKU находится в состоянии PURCHASED (КУПЛЕНО)
     * и покупка была подтверждена.
     * <p>
     * Returns whether or not the user has purchased a SKU. It does this by returning a
     * MediatorLiveData that returns true if the SKU is in the PURCHASED state and the Purchase has
     * been acknowledged.
     *
     * @return LiveData, которая наблюдает за состоянием покупки SKU
     * =======================================================================================
     */
    public LiveData<Boolean> isPurchased(String sku) {

        // skuStateMap - LiveData, которая в основном поддерживается, поэтому ее можно
        // преобразовать в наблюдаемые.
        // get - Возвращает значение, которому сопоставлен указанный ключ
        final LiveData<SkuState> skuStateLiveData = skuStateMap.get(sku);
        // assert - Набор методов assert. Сообщения отображаются только в случае сбоя утверждения.
        assert skuStateLiveData != null;
        // Transformations - Методы преобразования LiveData.
        // Эти методы позволяют функциональную композицию и делегирование экземпляров LiveData.
        // Преобразования вычисляются лениво и будут выполняться только при соблюдении
        // возвращенных LiveData. Поведение жизненного цикла распространяется от входного
        // источника LiveData к возвращаемому.
        // Общий смысл наверно таков - возвращается true, если sku имеет признак:
        // SKU_STATE_PURCHASED_AND_ACKNOWLEDGED - КУПЛЕНО И ПРИЗНАНО
        return Transformations.map(skuStateLiveData, skuState ->
                skuState == SkuState.SKU_STATE_PURCHASED_AND_ACKNOWLEDGED);

    }

    /**
     * =======================================================================================
     * ??? наверно, смысл таков - можно приобрести в деталях SKU и приобрести LiveData
     *
     * @param result
     * @param skuDetailsLiveData
     * @param skuStateLiveData   =======================================================================================
     */
    private void canPurchaseFromSkuDetailsAndPurchaseLiveData(
            @NonNull MediatorLiveData<Boolean> result,
            @NonNull LiveData<SkuDetails> skuDetailsLiveData,
            @NonNull LiveData<SkuState> skuStateLiveData
    ) {

        // getValue() - Возвращает текущее значение.
        // Обратите внимание, что вызов этого метода в фоновом потоке не гарантирует получение
        // последнего набора значений.
        SkuState skuState = skuStateLiveData.getValue();
        if (null == skuDetailsLiveData.getValue()) {
            // setValue (T) - Устанавливает значение
            result.setValue(false);
        } else {
            // это может быть временное состояние, но если мы не знаем о покупке,
            // мы обычно можем совершить покупку. Можно приобрести недействительные покупки.
            // this might be a transient state, but if we don't know about the purchase, we
            // typically can purchase. Not valid purchases can be purchased.
            // setValue (T) - Устанавливает значение
            result.setValue(null == skuState
                    || skuState == SkuState.SKU_STATE_UNPURCHASED);
        }

    }

    /**
     * ========================================================================================
     * Возвращает, может ли пользователь приобрести артикул. Он делает это, возвращая
     * преобразование LiveData, которое возвращает true, если SKU находится в состоянии UNSPECIFIED,
     * а также если у нас есть skuDetails для SKU.
     * <p>
     * Returns whether or not the user can purchase a SKU. It does this by returning a LiveData
     * transformation that returns true if the SKU is in the UNSPECIFIED state, as well as if we
     * have skuDetails for the SKU.
     *
     * @return LiveData, которая наблюдает за состоянием покупки SKU
     * ========================================================================================
     */
    public LiveData<Boolean> canPurchase(String sku) {

        final MediatorLiveData<Boolean> result = new MediatorLiveData<>();
        // get - Возвращает значение, которому сопоставлен указанный ключ
        final LiveData<SkuDetails> skuDetailsLiveData = skuDetailsLiveDataMap.get(sku);
        // get - Возвращает значение, которому сопоставлен указанный ключ
        final LiveData<SkuState> skuStateLiveData = skuStateMap.get(sku);
        assert skuStateLiveData != null;
        assert skuDetailsLiveData != null;
        // установить начальное состояние из значений LiveData перед обратными вызовами наблюдения.
        canPurchaseFromSkuDetailsAndPurchaseLiveData(result, skuDetailsLiveData, skuStateLiveData);
        result.addSource(skuDetailsLiveData, skuDetails ->
                canPurchaseFromSkuDetailsAndPurchaseLiveData(result, skuDetailsLiveData,
                        skuStateLiveData));
        result.addSource(skuStateLiveData, isValid ->
                canPurchaseFromSkuDetailsAndPurchaseLiveData(result, skuDetailsLiveData,
                        skuStateLiveData));
        return result;
    }

    /**
     * ====================================================================================
     * Название нашего SKU от SkuDetails.
     * <p>
     * The title of our SKU from SkuDetails.
     *
     * @param sku чтобы получить название
     * @return название запрошенного SKU как наблюдаемого LiveData <String>
     * ====================================================================================
     */
    public final LiveData<String> getSkuTitle(String sku) {

        // get - Возвращает значение, которому сопоставлен указанный ключ
        LiveData<SkuDetails> skuDetailsLiveData = skuDetailsLiveDataMap.get(sku);
        assert skuDetailsLiveData != null;
        return Transformations.map(skuDetailsLiveData, SkuDetails::getTitle);

    }

    // В SkuDetails много информации, но нашему приложению нужно всего несколько вещей,
    // так как наши товары никогда не поступают в продажу, имеют начальные цены и т. Д.
    // There's lots of information in SkuDetails, but our app only needs a few things, since our
    // goods never go on sale, have introductory pricing, etc.

    /**
     * ====================================================================================
     * Цена нашего SKU от SkuDetails.
     *
     * @param sku чтобы получить цену
     * @return цена запрошенного SKU как наблюдаемого LiveData <String>
     * ====================================================================================
     */
    public final LiveData<String> getSkuPrice(String sku) {

        // get - Возвращает значение, которому сопоставлен указанный ключ
        LiveData<SkuDetails> skuDetailsLiveData = skuDetailsLiveDataMap.get(sku);
        assert skuDetailsLiveData != null;
        return Transformations.map(skuDetailsLiveData, SkuDetails::getPrice);
    }

    /**
     * ====================================================================================
     * Описание нашего SKU от SkuDetails.
     *
     * @param sku чтобы получить описание
     * @return описание запрошенного SKU как наблюдаемого LiveData <String>
     * ====================================================================================
     */
    public final LiveData<String> getSkuDescription(String sku) {

        // get - Возвращает значение, которому сопоставлен указанный ключ
        LiveData<SkuDetails> skuDetailsLiveData = skuDetailsLiveDataMap.get(sku);
        assert skuDetailsLiveData != null;
        return Transformations.map(skuDetailsLiveData, SkuDetails::getDescription);
    }

    /**
     * ====================================================================================
     * onSkuDetailsResponse - это метод интерфейса SkuDetailsResponseListener
     * <p>
     * ОЧЕНЬ ВАЖНО - ЗДЕСЬ ПОЛУЧАЕМ ИНФОРМАЦИЮ О SKU, ОПУБЛИКОВАННЫХ В GOOGLE PLAY
     * <p>
     * Вызывается для уведомления о завершении операции получения сведений о SKU.
     * <p>
     * Получает результат от {@link #querySkuDetailsAsync ()}}.
     * <p>
     * Сохраните SkuDetails и опубликуйте их в {@link #skuDetailsLiveDataMap}.
     * Это позволяет другим частям приложения использовать {@link SkuDetails} для
     * отображения информации об артикуле и совершения покупок.
     * <p>
     * Store the SkuDetails and post them in the {@link #skuDetailsLiveDataMap}. This allows other
     * parts of the app to use the {@link SkuDetails} to show SKU information and make purchases.
     * ====================================================================================
     */
    @Override
    public void onSkuDetailsResponse(
            @NonNull BillingResult billingResult,
            List<SkuDetails> skuDetailsList) {

        // Получаем код возврата
        int responseCode = billingResult.getResponseCode();
        // Сообщение отладки возвращается при вызовах In-app Billing API.
        String debugMessage = billingResult.getDebugMessage();

        switch (responseCode) {
            case BillingClient.BillingResponseCode.OK:                  // Если успешно
                Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                // Если skuDetailsList равен null или пуст
                if (skuDetailsList == null || skuDetailsList.isEmpty()) {
                    Log.e(TAG, "onSkuDetailsResponse: " +
                            "SkuDetails пуст или null. " +
                            "Убедитесь, что запрошенные вами SKU правильно опубликованы в консоли Google Play.");
                }
                // Если skuDetailsList НЕ равен null И НЕ пуст
                else {
                    // Цикл skuDetails в skuDetailsList
                    for (SkuDetails skuDetails : skuDetailsList) {
                        // getSku() - Возвращает идентификатор продукта.
                        String sku = skuDetails.getSku();

                        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
                         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
                         * Этот фрагмент добавлен для тестирования
                         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
                         */

//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: Полученная информация о sku=" + sku);
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: Description=" + skuDetails.getDescription());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: Price=" + skuDetails.getPrice());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: Title=" + skuDetails.getTitle());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: Type=" + skuDetails.getType());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: OriginalPrice=" + skuDetails.getOriginalPrice());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: FreeTrialPeriod=" + skuDetails.getFreeTrialPeriod());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: IconUrl=" + skuDetails.getIconUrl());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: IntroductoryPrice=" + skuDetails.getIntroductoryPrice());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: IntroductoryPricePeriod=" + skuDetails.getIntroductoryPricePeriod());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: OriginalJson=" + skuDetails.getOriginalJson());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: PriceCurrencyCode=" + skuDetails.getPriceCurrencyCode());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: SubscriptionPeriod=" + skuDetails.getSubscriptionPeriod());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: IntroductoryPriceAmountMicros=" + skuDetails.getIntroductoryPriceAmountMicros());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: IntroductoryPriceCycles=" + skuDetails.getIntroductoryPriceCycles());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: PriceAmountMicros=" + skuDetails.getPriceAmountMicros());
//                        logFile.writeLogFile("   BillingDataSource: onSkuDetailsResponse: OriginalPriceAmountMicros=" + skuDetails.getOriginalPriceAmountMicros());


                        // get - Возвращает значение, которому сопоставлен указанный ключ
                        MutableLiveData<SkuDetails> detailsMutableLiveData =
                                skuDetailsLiveDataMap.get(sku);
                        if (null != detailsMutableLiveData) {
                            // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                            detailsMutableLiveData.postValue(skuDetails);
                        } else {
                            Log.e(TAG, "Неизвестный артикул (sku): " + sku);
                        }
                    }
                }
                break;
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:// Сервис Play Store
                // сейчас не подключен -
                // потенциально временное состояние.
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE: // Сетевое соединение не работает.
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE: // Версия Billing API
                // не поддерживается для запрошенного типа.
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:    // Запрошенный продукт
                // недоступен для покупки.
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:     // API предоставлены
                // неверные аргументы.
            case BillingClient.BillingResponseCode.ERROR:               // Неустранимая ошибка во
                // время действия API.
                Log.e(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:       // Пользователь вернул назад
                // или отменил диалог.
                Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                break;
            // Эти коды ответов не ожидаются.
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:// Запрошенная функция не
                // поддерживается Play Store
                // на текущем устройстве.
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:  // Невозможность покупки,
                // поскольку товар уже принадлежит.
            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:      // Неспособность потребить,
                // поскольку предмет не принадлежит.
            default:
                Log.wtf(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
        }
        // Если успешно
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            // skuDetailsResponseTime - когда был последний успешный ответ SkuDetailsResponse
            // Фиксируем время (отсчитивается с момента загрузки)
            skuDetailsResponseTime = SystemClock.elapsedRealtime();
        }
        // Если НЕ успешно
        else {
            // Фиксируем время - Время перезапроса для SKU деталей в миллисекундах
            skuDetailsResponseTime = -SKU_DETAILS_REQUERY_TIME;
        }

    }

    /**
     * =====================================================================================
     * Вызывает функции биллинг-клиента для запроса сведений о SKU как для встроенного
     * приложения, так и для SKU подписки. Детали SKU полезны для отображения названий товаров
     * и прайс-листов для пользователя и необходимы для совершения покупки.
     * <p>
     * Calls the billing client functions to query sku details for both the inapp and subscription
     * SKUs. SKU details are useful for displaying item names and price lists to the user, and are
     * required to make a purchase.
     * =====================================================================================
     */
    private void querySkuDetailsAsync() {

        // Если известные SKU (используются для запроса данных SKU и проверки ответов)
        // != null и не пуст
        if (null != knownInappSKUs && !knownInappSKUs.isEmpty()) {
            // querySkuDetailsAsync - Выполняет сетевой запрос для получения сведений
            // об артикуле и асинхронного возврата результата с параметрами:
            // SkuDetailsParams - Параметры для запроса сведений о артикуле
            // INAPP - Примечание: это также означает, что элементы INAPP поддерживаются
            // для покупок, запросов и всех других действий. Если вам нужно проверить
            // поддержку SUBSCRIPTIONS или чего-то другого, используйте метод
            // isFeatureSupported (String).
            // Строка: тип SKU, либо «inapp», либо «subs», как в BillingClient.SkuType.
            //      INAPP - Тип SKU для продуктов в приложениях Android
            //      SUBS - Тип SKU для подписок на приложения Android
            // Потом вызывается метод onSkuDetailsResponse (это метод интерфейса
            // SkuDetailsResponseListener - Вызывается для уведомления о завершении
            // операции получения сведений о SKU)
            billingClient.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                    .setType(BillingClient.SkuType.INAPP)
                    .setSkusList(knownInappSKUs)    // knownInappSKUs - известные SKU
                    // (используются для запроса данных SKU
                    // и проверки ответов)
                    .build(), this);
        }
        // Если Артикулы подписок, о которых должен знать источник
        // != null и не пуст
        if (null != knownSubscriptionSKUs && !knownSubscriptionSKUs.isEmpty()) {
            // querySkuDetailsAsync - Выполняет сетевой запрос для получения сведений
            // об артикуле и асинхронного возврата результата с параметрами:
            // Потом вызывается метод onSkuDetailsResponse (это метод интерфейса
            // SkuDetailsResponseListener - Вызывается для уведомления о завершении
            // операции получения сведений о SKU)
            billingClient.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                    .setType(BillingClient.SkuType.SUBS)
                    .setSkusList(knownSubscriptionSKUs)     // knownSubscriptionSKUs - Артикулы
                    // подписок, о которых должен
                    // знать источник
                    .build(), this);
        }
    }

    /**
     * ===================================================================================
     * refreshPurchasesAsync - асинхронный запрос покупок
     * <p>
     * GPBL v4 теперь запрашивает покупки асинхронно. Это только активные покупки
     * <p>
     * GPBL v4 now queries purchases asynchronously. This only gets active
     * purchases.
     * ===================================================================================
     */
    public void refreshPurchasesAsync() {

        // queryPurchasesAsync - Возвращает сведения о покупках для принадлежащих в
        // настоящее время товаров, купленных в вашем приложении.
        // Возвращаются только активные подписки и неиспользованные разовые покупки.
        // Этот метод использует кеш приложения Google Play Store без инициирования сетевого запроса.
        // Примечание. В целях безопасности рекомендуется пройти проверку покупок на вашем сервере
        // (если он у вас есть), вызвав один из следующих API:
        // https://developers.google.com/android-publisher/api-ref/purchases/products /
        // получить https://developers.google.com/android-publisher/api-ref/purchases/subscriptions/get
        // Параметры:
        // - skuType - Строка: тип SKU, либо «inapp», либо «subs», как в BillingClient.SkuType.
        //      INAPP - Тип SKU для продуктов в приложениях Android
        //      SUBS - Тип SKU для подписок на приложения Android
        // - listener - PurchasesResponseListener: прослушиватель результата запроса,
        // возвращенного асинхронно через обратный вызов с BillingResult и списком Purchase.
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP,  // INAPP - Тип SKU для
                // продуктов в приложениях Android
                (billingResult, list) -> {
                    // Если код возврата НЕ равен ОК
                    if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "Проблема с покупками: " +
                                billingResult.getDebugMessage());
                    }
                    // Если код возврата равен ОК
                    else {
                        // Прорабатывает каждую покупку и проверяет, что состояние покупки
                        // обработано и доступно через LiveData. Проверяет подпись и подтверждает
                        // покупки. PURCHASED не возвращается, пока покупка не подтверждена.
                        processPurchaseList(list, knownInappSKUs);
                    }
                });

        // queryPurchasesAsync - Возвращает сведения о покупках для принадлежащих в
        // настоящее время товаров, купленных в вашем приложении.
        // Возвращаются только активные подписки и неиспользованные разовые покупки.
        // Этот метод использует кеш приложения Google Play Store без инициирования сетевого запроса.
        // Примечание. В целях безопасности рекомендуется пройти проверку покупок на вашем сервере
        // (если он у вас есть), вызвав один из следующих API:
        // https://developers.google.com/android-publisher/api-ref/purchases/products /
        // получить https://developers.google.com/android-publisher/api-ref/purchases/subscriptions/get
        // Параметры:
        // - skuType - Строка: тип SKU, либо «inapp», либо «subs», как в BillingClient.SkuType.
        //      INAPP - Тип SKU для продуктов в приложениях Android
        //      SUBS - Тип SKU для подписок на приложения Android
        // - listener - PurchasesResponseListener: прослушиватель результата запроса,
        // возвращенного асинхронно через обратный вызов с BillingResult и списком Purchase.
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS,   // SUBS - Тип SKU для
                // подписок на приложения Android
                (billingResult, list) -> {
                    // Если код возврата НЕ равен ОК
                    if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "Проблема с получением подписок: " +
                                billingResult.getDebugMessage());
                    }
                    // Если код возврата равен ОК
                    else {
                        // Прорабатывает каждую покупку и проверяет, что состояние покупки
                        // обработано и доступно через LiveData. Проверяет подпись и подтверждает
                        // покупки. PURCHASED не возвращается, пока покупка не подтверждена.
                        processPurchaseList(list, knownSubscriptionSKUs);
                    }

                });
        Log.d(TAG, "Обновление покупок началось.");

    }

    /**
     * =======================================================================================
     * Используется внутри компании для получения покупок из запрошенного набора SKU.
     * Это особенно важно при изменении подписок, поскольку onPurchasesUpdated не обновляет
     * состояние покупки подписки, с которой было выполнено обновление.
     * <p>
     * Used internally to get purchases from a requested set of SKUs. This is particularly important
     * when changing subscriptions, as onPurchasesUpdated won't update the purchase state of a
     * subscription that has been upgraded from.
     *
     * @param skus    skus, чтобы получить информацию о покупке
     * @param skuType тип sku, inapp или подписка, чтобы получить информацию о покупке.
     *                sku type, inapp or subscription, to get purchase information for.
     * @return purchases
     * =======================================================================================
     */
    private List<Purchase> getPurchases(String[] skus, String skuType) {

        // Purchase - Представляет покупку через приложение.
        // Purchase.PurchasesResult - Список результатов и код ответа для метода BillingClient.queryPurchases (String).
        // billingClient.queryPurchases - Этот метод устарел.
        // Вместо этого используйте queryPurchasesAsync (String, PurchasesResponseListener).
        // Возвращает сведения о покупках для принадлежащих в
        // настоящее время товаров, купленных в вашем приложении.
        // Возвращаются только активные подписки и неиспользованные разовые покупки.
        // Этот метод использует кеш приложения Google Play Store без инициирования сетевого запроса.
        // pr - сведения о покупках для принадлежащих в настоящее время товаров,
        // купленных в вашем приложении
        Purchase.PurchasesResult pr = billingClient.queryPurchases(skuType);
        // br - Возвращает BillingResult операции.
        BillingResult br = pr.getBillingResult();
        // LinkedList<> - Реализация двусвязного списка интерфейсов List и Deque.
        // Реализует все необязательные операции со списком и разрешает все элементы (включая null).
        // returnPurchasesList - вернуть список покупок
        List<Purchase> returnPurchasesList = new LinkedList<>();
        // Если код возврата НЕ равен ОК
        if (br.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Проблема с покупками: " + br.getDebugMessage());
        }
        // Если код возврата равен ОК
        else {
            // getPurchasesList() - Возвращает список покупок.
            // purchasesList - список покупок
            List<Purchase> purchasesList = pr.getPurchasesList();
            // список покупок не равен null
            if (null != purchasesList) {
                // Цикл для покупок из список покупок
                for (Purchase purchase : purchasesList) {
                    // Цикл для sku в skus
                    for (String sku : skus) {
                        // purchase.getSkus() - Возвращает идентификаторы продукта.
                        // Цикл purchaseSku в идентификаторах продукта
                        for (String purchaseSku : purchase.getSkus()) {
                            if (purchaseSku.equals(sku)) {
                                // Если в списке покупок не содержится purchase
                                if (!returnPurchasesList.contains(purchase)) {
                                    // то в список покупок добавляем purchase
                                    returnPurchasesList.add(purchase);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Возвращаем список покупок
        return returnPurchasesList;
    }

    /**
     * =======================================================================================
     * Потребляет покупки в приложении.
     * Заинтересованные слушатели могут наблюдать за покупкойConsumed LiveEvent.
     * <p>
     * Чтобы упростить задачу, вы можете отправить список SKU, которые автоматически
     * используются BillingDataSource.
     * <p>
     * Consumes an in-app purchase. Interested listeners can watch the purchaseConsumed LiveEvent.
     * To make things easy, you can send in a list of SKUs that are auto-consumed by the
     * BillingDataSource.
     * =======================================================================================
     */
    public void consumeInappPurchase(@NonNull String sku) {

        // queryPurchasesAsync - Возвращает сведения о покупках для принадлежащих в
        // настоящее время товаров, купленных в вашем приложении.
        // Возвращаются только активные подписки и неиспользованные разовые покупки.
        // Этот метод использует кеш приложения Google Play Store без инициирования сетевого запроса.
        // Примечание. В целях безопасности рекомендуется пройти проверку покупок на вашем сервере
        // (если он у вас есть), вызвав один из следующих API:
        // https://developers.google.com/android-publisher/api-ref/purchases/products /
        // получить https://developers.google.com/android-publisher/api-ref/purchases/subscriptions/get
        // Параметры:
        // - skuType - Строка: тип SKU, либо «inapp», либо «subs», как в BillingClient.SkuType.
        //      INAPP - Тип SKU для продуктов в приложениях Android
        //      SUBS - Тип SKU для подписок на приложения Android
        // - listener - PurchasesResponseListener: прослушиватель результата запроса,
        // возвращенного асинхронно через обратный вызов с BillingResult и списком Purchase.
        billingClient.queryPurchasesAsync(
                BillingClient.SkuType.INAPP,            // INAPP - Тип SKU для
                // продуктов в приложениях Android
                (billingResult, list) -> {
                    assert list != null;
                    // Если код возврата НЕ равен ОК
                    if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "Проблема с покупками: " +
                                billingResult.getDebugMessage());
                    }
                    // Если код возврата равен ОК
                    else {
                        for (Purchase purchase : list) {
                            // на данный момент любой набор SKU должен быть расходным
                            // getSkus() - Возвращает идентификаторы продукта.
                            for (String purchaseSku : purchase.getSkus())
                                if (purchaseSku.equals(sku)) {
                                    // consumePurchase(purchase) - Только внутренний вызов.
                                    // Предполагается, что все проверки подписей выполнены и
                                    // покупка готова к употреблению. Если артикул уже израсходован,
                                    // ничего не делает.
                                    consumePurchase(purchase);
                                    return;
                                }
                        }
                    }
                    Log.e(TAG, "Невозможно использовать SKU: " + sku + " Артикул не найден.");
                });
    }

    /**
     * ========================================================================================
     * Вызов этого означает, что у нас есть самая последняя информация для Sku в объекте покупки.
     * При этом используется состояние покупки (Ожидание, Не указано, Покупка) вместе с
     * подтвержденным состоянием.
     * <p>
     * Calling this means that we have the most up-to-date information for a Sku in a purchase
     * object. This uses the purchase state (Pending, Unspecified, Purchased) along with the
     * acknowledged state.
     *
     * @param purchase актуальный объект для установки состояния для Sku
     *                 ========================================================================================
     */
    private void setSkuStateFromPurchase(@NonNull Purchase purchase) {

        // getSkus() - Возвращает идентификаторы продукта.
        // Цикл purchaseSku в purchase.getSkus()
        for (String purchaseSku : purchase.getSkus()) {
            MutableLiveData<SkuState> skuStateLiveData = skuStateMap.get(purchaseSku);
            if (null == skuStateLiveData) {
                Log.e(TAG, "Неизвестный SKU " + purchaseSku + ". Убедитесь, что SKU " +
                        "соответствует SKUS в консоли разработчика Play.");
            } else {
                // getPurchaseState() - Возвращает одно из Purchase.PurchaseState,
                // указывающее состояние покупки.
                switch (purchase.getPurchaseState()) {
                    case Purchase.PurchaseState.PENDING:
                        // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                        // Отправляет SKU_STATE_PENDING - В ОЖИДАНИИ
                        skuStateLiveData.postValue(SkuState.SKU_STATE_PENDING);
                        break;
                    case Purchase.PurchaseState.UNSPECIFIED_STATE:
                        // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                        // Отправляет SKU_STATE_UNPURCHASED - НЕ ПРИОБРЕТАЕТСЯ
                        skuStateLiveData.postValue(SkuState.SKU_STATE_UNPURCHASED);
                        break;
                    case Purchase.PurchaseState.PURCHASED:
                        // isAcknowledged() - Указывает, подтверждена ли покупка.
                        // Если покупка подтверждена
                        if (purchase.isAcknowledged()) {
                            // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                            // Отправляет SKU_STATE_PURCHASED_AND_ACKNOWLEDGED - КУПЛЕНО И ПРИЗНАНО
                            skuStateLiveData.postValue(
                                    SkuState.SKU_STATE_PURCHASED_AND_ACKNOWLEDGED);
                        }
                        // Если покупка НЕ подтверждена
                        else {
                            // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                            // Отправляет SKU_STATE_PURCHASED - КУПЛЕНО
                            skuStateLiveData.postValue(SkuState.SKU_STATE_PURCHASED);
                        }
                        break;
                    default:
                        Log.e(TAG, "Покупка в неизвестном состоянии: " + purchase.getPurchaseState());
                }
            }
        }

    }

    /**
     * ===========================================================================================
     * Поскольку мы (в основном) получаем статус SKU, когда фактически совершаем покупку или
     * обновляем покупки, мы сохраняем некоторое внутреннее состояние, когда делаем такие вещи,
     * как подтверждение или использование.
     * <p>
     * Since we (mostly) are getting sku states when we actually make a purchase or update
     * purchases, we keep some internal state when we do things like acknowledge or consume.
     *
     * @param sku         sku изменить состояние
     * @param newSkuState новое состояние артикула.
     *                    ===========================================================================================
     */
    private void setSkuState(@NonNull String sku, SkuState newSkuState) {

        // get - Возвращает значение, которому сопоставлен указанный ключ
        MutableLiveData<SkuState> skuStateLiveData = skuStateMap.get(sku);
        if (null == skuStateLiveData) {
            Log.e(TAG, "Неизвестный SKU " + sku + ". Убедитесь, что SKU " +
                    "соответствует SKUS в консоли разработчика Play.");
        } else {
            // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
            skuStateLiveData.postValue(newSkuState);
        }
    }

    /**
     * ========================================================================================
     * Прорабатывает каждую покупку и проверяет, что состояние покупки обработано и доступно
     * через LiveData. Проверяет подпись и подтверждает покупки. PURCHASED не возвращается,
     * пока покупка не подтверждена.
     * <p>
     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     * <p>
     * Разработчики могут подтвердить покупки с сервера с помощью Google Play Developer API.
     * Сервер имеет прямой доступ к базе данных пользователей, поэтому использование API
     * разработчика Google Play для подтверждения может быть более надежным.
     * <p>
     * Если токен покупки не будет подтвержден в течение 3 дней, Google Play автоматически
     * вернет деньги и отменит покупку. Такое поведение помогает гарантировать, что с
     * пользователей не будет взиматься плата, если пользователь успешно не получил доступ
     * к контенту. Это устраняет категорию проблем, когда пользователи жалуются разработчикам,
     * что они заплатили за то, что приложение им не дает.
     * <p>
     * Если в этот метод передается список skusToUpdate, для любых покупок, не входящих
     * в список покупок, будет установлено состояние UNPURCHASED (НЕ ПРИОБРЕТАЕТСЯ).
     * <p>
     * Goes through each purchase and makes sure that the purchase state is processed and the state
     * is available through LiveData. Verifies signature and acknowledges purchases. PURCHASED isn't
     * returned until the purchase is acknowledged. * <p> https://developer.android
     * .com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     * <p>
     * Developers can choose to acknowledge purchases from a server using the Google Play Developer
     * API. The server has direct access to the user database, so using the Google Play Developer
     * API for acknowledgement might be more reliable.
     * <p>
     * If the purchase token is not acknowledged within 3 days, then Google Play will automatically
     * refund and revoke the purchase. This behavior helps ensure that users are not charged unless
     * the user has successfully received access to the content. This eliminates a category of
     * issues where users complain to developers that they paid for something that the app is not
     * giving to them.
     * <p>
     * If a skusToUpdate list is passed-into this method, any purchases not in the list of purchases
     * will have their state set to UNPURCHASED.
     *
     * @param purchases    Список покупок для обработки.
     * @param skusToUpdate список SKU, состояние которого мы хотим обновить --- это позволяет
     *                     нам установить состояние невозвращенных SKU на UNPURCHASED (НЕ ПРИОБРЕТАЕТСЯ).
     *                     a list of skus that we want to update the state from --- this allows us
     *                     to set the state of non-returned SKUs to UNPURCHASED.
     *                     ========================================================================================
     */
    private void processPurchaseList(
            List<Purchase> purchases,
            List<String> skusToUpdate) {

        HashSet<String> updatedSkus = new HashSet<>();

        if (null != purchases) {
            for (final Purchase purchase : purchases) {
                // getSkus() - Возвращает идентификаторы продукта.
                for (String sku : purchase.getSkus()) {
                    // get - Возвращает значение, которому сопоставлен указанный ключ
                    final MutableLiveData<SkuState> skuStateLiveData = skuStateMap.get(sku);
                    if (null == skuStateLiveData) {
                        Log.e(TAG, "Неизвестный SKU " + sku + ". Убедитесь, что SKU " +
                                "соответствует SKUS в консоли разработчика Play.");
                        continue;
                    }
                    updatedSkus.add(sku);
                }
                // getPurchaseState() - Возвращает одно из Purchase.PurchaseState,
                // указывающее состояние покупки.
                // Global check to make sure all purchases are signed correctly.
                // This check is best performed on your server.
                int purchaseState = purchase.getPurchaseState();
                if (purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!isSignatureValid(purchase)) {
                        Log.e(TAG, "Неверная подпись при покупке. " +
                                "Убедитесь, что ваш открытый ключ правильный");
                        continue;
                    }
                    // only set the purchased state after we've validated the signature.
                    // Вызов этого означает, что у нас есть самая последняя информация для Sku в
                    // объекте покупки. При этом используется состояние покупки
                    // (Ожидание, Не указано, Покупка) вместе с подтвержденным состоянием.
                    setSkuStateFromPurchase(purchase);
                    boolean isConsumable = false;
                    // getSkus() - Возвращает идентификаторы продукта.
                    for (String sku : purchase.getSkus()) {
                        if (knownAutoConsumeSKUs.contains(sku)) {
                            isConsumable = true;
                        } else {
                            if (isConsumable) {
                                Log.e(TAG, "Покупка не может содержать" +
                                        " расходные и непотребляемые предметы.: " + purchase.getSkus().toString());
                                isConsumable = false;
                                break;
                            }
                        }
                    }
                    if (isConsumable) {
                        // consumePurchase(purchase) - Только внутренний вызов.
                        // Предполагается, что все проверки подписей выполнены и
                        // покупка готова к употреблению. Если артикул уже израсходован,
                        // ничего не делает.
                        consumePurchase(purchase);
                    } else
                    // isAcknowledged() - Указывает, подтверждена ли покупка.
                    // Если покупка НЕ подтверждена
                    if (!purchase.isAcknowledged()) {
                        billingClient.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build(), billingResult -> {
                            if (billingResult.getResponseCode()
                                    == BillingClient.BillingResponseCode.OK) {
                                // покупка подтверждена
                                // getSkus() - Возвращает идентификаторы продукта.
                                for (String sku : purchase.getSkus()) {
                                    setSkuState(sku, SkuState.SKU_STATE_PURCHASED_AND_ACKNOWLEDGED);
                                }
                                // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                                // getSkus() - Возвращает идентификаторы продукта.
                                newPurchase.postValue(purchase.getSkus());
                            }
                        });
                    }
                } else {
                    // убедитесь, что состояние установлено
                    // Вызов этого означает, что у нас есть самая последняя информация для Sku в
                    // объекте покупки. При этом используется состояние покупки
                    // (Ожидание, Не указано, Покупка) вместе с подтвержденным состоянием.
                    setSkuStateFromPurchase(purchase);
                }
            }
        } else {
            Log.d(TAG, "Пустой список покупок.");
        }
        // Очистите состояние покупки всего, чего не было в этом списке покупок,
        // если это часть обновления.
        if (null != skusToUpdate) {
            for (String sku : skusToUpdate) {
                if (!updatedSkus.contains(sku)) {
                    setSkuState(sku, SkuState.SKU_STATE_UNPURCHASED);
                }
            }
        }
    }

    /**
     * ========================================================================================
     * Только внутренний вызов. Предполагается, что все проверки подписей выполнены и
     * покупка готова к употреблению. Если артикул уже израсходован, ничего не делает.
     * <p>
     * Internal call only. Assumes that all signature checks have been completed and the purchase is
     * ready to be consumed. If the sku is already being consumed, does nothing.
     *
     * @param purchase покупка для потребления
     *                 ========================================================================================
     */
    private void consumePurchase(@NonNull Purchase purchase) {

        // слабая проверка, чтобы убедиться, что мы еще не потребляем артикул
        // weak check to make sure we're not already consuming the sku
        if (purchaseConsumptionInProcess.contains(purchase)) {
            // уже потреблено
            return;
        }
        purchaseConsumptionInProcess.add(purchase);
        // consumeAsync - Потребляет определенный продукт в приложении.
        // Параметры:
        //  - ConsumeParams consumeParams - Параметры для потребления покупки.
        //  См. BillingClient.consumeAsync (ConsumeParams, ConsumeResponseListener).
        //  - ConsumeResponseListener listener - Обратный вызов, который уведомляет о
        //  завершении операции потребления.
        // Потребление может быть выполнено только с принадлежащим ему предметом,
        // и в результате потребления пользователь больше не будет владеть им.
        // Потребление выполняется асинхронно, и слушатель получает обратный вызов, указанный по завершении.
        // Предупреждение! Все покупки требуют подтверждения. Непризнание покупки приведет к
        // возврату денег за эту покупку. Для одноразовых продуктов убедитесь, что вы используете
        // этот метод, который действует как неявное подтверждение, или вы можете явно подтвердить
        // покупку с помощью подтвержденияPurchase (AcknowledgePurchaseParams,
        // AcknowledgePurchaseResponseListener). Для подписок используйте
        // acceptPurchase (AcknowledgePurchaseParams, AcknowledgePurchaseResponseListener).
        // Дополнительные сведения см. На странице
        // https://developer.android.com/google/play/billing/billing_library_overview#acknowledge.
        billingClient.consumeAsync(ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build(),           // Параметры для потребления покупки
                (billingResult, s) -> {     // ConsumeResponseListener - Обратный вызов, который
                    // уведомляет о завершении операции потребления.
                    // Удаляет указанный элемент из этого набора, если он присутствует
                    purchaseConsumptionInProcess.remove(purchase);
                    // Если код возврата ОК
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Расход успешный. Поставка права (Consumption successful. Delivering entitlement.)");
                        // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                        // getSkus() - Возвращает идентификаторы продукта.
                        purchaseConsumed.postValue(purchase.getSkus());
                        // Цикл
                        // getSkus() - Возвращает идентификаторы продукта.
                        for (String sku : purchase.getSkus()) {
                            // Поскольку мы израсходовали покупку
                            setSkuState(sku, SkuState.SKU_STATE_UNPURCHASED);
                            // И это тоже считается новой покупкой.
                        }
                        // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                        // getSkus() - Возвращает идентификаторы продукта.
                        newPurchase.postValue(purchase.getSkus());
                    }
                    // Если код возврата НЕ равен ОК
                    else {
                        Log.e(TAG, "Ошибка при потреблении: " + billingResult.getDebugMessage());
                    }
                    Log.d(TAG, "Конечный расход потребления (End consumption flow.)");
                });
    }

    /**
     * =========================================================================================
     * Запустите процесс биллинга. Это запустит внешнее действие для результата,
     * поэтому для него требуется ссылка на действие.
     * <p>
     * Для подписок он поддерживает обновление с одного типа SKU на
     * другой путем передачи обновляемых SKU.
     * <p>
     * Launch the billing flow. This will launch an external Activity for a result, so it requires
     * an Activity reference. For subscriptions, it supports upgrading from one SKU type to another
     * by passing in SKUs to be upgraded.
     *
     * @param activity    активная деятельность по запуску нашего платежного потока с
     * @param sku         SKU для покупки
     * @param upgradeSkus Артикулы, с которых можно обновить подписку
     *                    =========================================================================================
     */
    public void launchBillingFlow(Activity activity, @NonNull String sku,
                                  String... upgradeSkus) {

        // get - Возвращает значение, которому сопоставлен указанный ключ
        LiveData<SkuDetails> skuDetailsLiveData = skuDetailsLiveDataMap.get(sku);
        assert skuDetailsLiveData != null;
        // getValue() - Возвращает текущее значение.
        // Обратите внимание, что вызов этого метода в фоновом потоке не гарантирует получение
        // последнего набора значений.
        SkuDetails skuDetails = skuDetailsLiveData.getValue();
        if (null != skuDetails) {
            // Если Артикулы, с которых можно обновить подписку не равно null и непустой
            if (null != upgradeSkus && upgradeSkus.length > 0) {
                // queryPurchasesAsync - Возвращает сведения о покупках для принадлежащих в
                // настоящее время товаров, купленных в вашем приложении.
                // Возвращаются только активные подписки и неиспользованные разовые покупки.
                // Этот метод использует кеш приложения Google Play Store без инициирования сетевого запроса.
                // Примечание. В целях безопасности рекомендуется пройти проверку покупок на вашем сервере
                // (если он у вас есть), вызвав один из следующих API:
                // https://developers.google.com/android-publisher/api-ref/purchases/products /
                // получить https://developers.google.com/android-publisher/api-ref/purchases/subscriptions/get
                // Параметры:
                // - skuType - Строка: тип SKU, либо «inapp», либо «subs», как в BillingClient.SkuType.
                //      INAPP - Тип SKU для продуктов в приложениях Android
                //      SUBS - Тип SKU для подписок на приложения Android
                // - listener - PurchasesResponseListener: прослушиватель результата запроса,
                // возвращенного асинхронно через обратный вызов с BillingResult и списком Purchase.
                billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS,   // SUBS - Тип SKU для
                        // подписок на приложения Android
                        (br, purchasesList) -> {
                            // heldSubscriptions - лист проведенные подписки
                            List<Purchase> heldSubscriptions = new LinkedList<>();
                            // Если код возврата НЕ равен ОК
                            if (br.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                                Log.e(TAG, "Проблема с покупками: " + br.getDebugMessage());
                            }
                            // Если код возврата равен ОК
                            else {
                                if (null != purchasesList) {
                                    for (Purchase purchase : purchasesList) {
                                        for (String upgradeSku : upgradeSkus) {
                                            // getSkus() - Возвращает идентификаторы продукта.
                                            for (String purchaseSku : purchase.getSkus()) {
                                                if (purchaseSku.equals(upgradeSku)) {
                                                    if (!heldSubscriptions.contains(purchase)) {
                                                        // В лист проведенные подписки добавляем покупку
                                                        heldSubscriptions.add(purchase);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            BillingFlowParams.Builder billingFlowParamsBuilder = BillingFlowParams.newBuilder();
                            billingFlowParamsBuilder.setSkuDetails(skuDetails);

                            // Анализ размеров лист проведенные подписки
                            switch (heldSubscriptions.size()) {
                                case 1:  // Процесс обновления!
                                    // берем первый элемент из лист проведенные подписки
                                    Purchase purchase = heldSubscriptions.get(0);
                                    billingFlowParamsBuilder.setSubscriptionUpdateParams(
                                            BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                                                    .setOldSkuPurchaseToken(heldSubscriptions.get(0)
                                                            .getPurchaseToken())
                                                    .build()
                                    );
                                    // launchBillingFlow - Инициирует процесс выставления счетов
                                    // для покупки в приложении или подписки.
                                    // Появится экран покупки в Google Play.
                                    // Результат будет доставлен через реализацию интерфейса
                                    // PurchasesUpdatedListener, установленную
                                    // BillingClient.Builder.setListener (PurchasesUpdatedListener).
                                    br = billingClient.launchBillingFlow(activity,
                                            billingFlowParamsBuilder.build());
                                    // Если код возврата ОК
                                    if (br.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                        // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                                        // Для платежного процесса отправляем задачу в основной
                                        // поток, что бы установить значение true
                                        billingFlowInProcess.postValue(true);
                                    }
                                    // Если код возврата не равен ОК
                                    else {
                                        Log.e(TAG, "Биллинг не прошел: + " + br.getDebugMessage());
                                    }
                                    break;
                                case 0:

//ЭТО ДЛЯ ПРОБЫ

                                    // launchBillingFlow - Инициирует процесс выставления счетов
                                    // для покупки в приложении или подписки.
                                    // Появится экран покупки в Google Play.
                                    // Результат будет доставлен через реализацию интерфейса
                                    // PurchasesUpdatedListener, установленную
                                    // BillingClient.Builder.setListener (PurchasesUpdatedListener).
                                    br = billingClient.launchBillingFlow(activity,
                                            billingFlowParamsBuilder.build());
                                    // Если код возврата ОК
                                    if (br.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                        // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                                        // Для платежного процесса отправляем задачу в основной
                                        // поток, что бы установить значение true
                                        billingFlowInProcess.postValue(true);
                                    }
                                    // Если код возврата не равен ОК
                                    else {
                                        Log.e(TAG, "Биллинг не прошел: + " + br.getDebugMessage());
                                    }
                                    break;
//ЭТО ДЛЯ ПРОБЫ

/*                                    logFile.writeLogFile("   BillingDataSource: launchBillingFlow: switch (heldSubscriptions.size()) case 0");
                                    logFile.writeLogFile("   BillingDataSource: launchBillingFlow: break");

                                    break;*/

                                default:
                                    Log.e(TAG, heldSubscriptions.size() +
                                            " подписки, на которые подписаны. Обновление невозможно.");
                            }
                        });
            }
            // Если Артикулы, с которых можно обновить подписку равно null или он пустой
            else {
                BillingFlowParams.Builder billingFlowParamsBuilder = BillingFlowParams.newBuilder();
                billingFlowParamsBuilder.setSkuDetails(skuDetails);
                // launchBillingFlow - Инициирует процесс выставления счетов
                // для покупки в приложении или подписки.
                // Появится экран покупки в Google Play.
                // Результат будет доставлен через реализацию интерфейса
                // PurchasesUpdatedListener, установленную
                // BillingClient.Builder.setListener (PurchasesUpdatedL
                BillingResult br = billingClient.launchBillingFlow(activity,
                        billingFlowParamsBuilder.build());
                // Если код возврата ОК
                if (br.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // postValue (T) - Отправляет задачу в основной поток, чтобы установить заданное значение
                    // Для платежного процесса отправляем задачу в основной
                    // поток, что бы установить значение true
                    billingFlowInProcess.postValue(true);
                }
                // Если код возврата не равен ОК
                else {
                    Log.e(TAG, "Биллинг не прошел: + " + br.getDebugMessage());
                }
            }
        } else {
            Log.e(TAG, "SkuDetails не найден для: " + sku);
        }
    }

    /**
     * =======================================================================================
     * Возвращает LiveData, который сообщает, обрабатывается ли процесс выставления счетов,
     * что означает, что launchBillingFlow вернул BillingResponseCode.OK, а onPurchasesUpdated
     * еще не был вызван.
     * <p>
     * Returns a LiveData that reports if a billing flow is in process, meaning that
     * launchBillingFlow has returned BillingResponseCode.OK and onPurchasesUpdated hasn't yet been
     * called.
     *
     * @return LiveData который указывает известное состояние потока выставления счетов.
     * =======================================================================================
     */
    public LiveData<Boolean> getBillingFlowInProcess() {
        return billingFlowInProcess;
    }

    /**
     * ====================================================================================
     * onPurchasesUpdated - это метод интерфейса PurchasesUpdatedListener
     * <p>
     * Вызывается библиотекой BillingLibrary при обнаружении новых покупок;
     * обычно в ответ на launchBillingFlow.
     * <p>
     * Реализуйте этот метод, чтобы получать уведомления об обновлениях покупок.
     * Здесь будут указаны как покупки, инициированные ВАШИМ приложением, так и покупки,
     * инициированные ВНЕ ВАШЕГО приложения.
     * <p>
     * Предупреждение! Все покупки, указанные здесь, должны быть либо потреблены, либо подтверждены.
     * Неспособность использовать (через BillingClient.consumeAsync
     * (ConsumeParams, ConsumeResponseListener)) или подтвердить
     * (через BillingClient.acknowledgePurchase (AcknowledgePurchaseParams, AcknowledgePurchaseResponseListener))
     * покупку приведет к возмещению этой покупки.
     * Пожалуйста, обратитесь к руководству по интеграции для получения более подробной информации.
     * <p>
     * Called by the BillingLibrary when new purchases are detected; typically in response to a
     * launchBillingFlow.
     *
     * @param billingResult результат потока покупок.
     * @param list          лист новых покупок.
     *                      ====================================================================================
     */
    @Override
    public void onPurchasesUpdated(
            @NonNull BillingResult billingResult,
            @Nullable List<Purchase> list) {

        // Анализ кода возврата
        switch (billingResult.getResponseCode()) {
            case BillingClient.BillingResponseCode.OK:              // OK
                // Если лист новых покупок не равен null
                if (null != list) {
                    // Прорабатывает каждую покупку и проверяет, что состояние покупки
                    // обработано и доступно через LiveData. Проверяет подпись и подтверждает
                    // покупки. PURCHASED не возвращается, пока покупка не подтверждена.
                    processPurchaseList(list, null);
                    return;
                }
                // Если лист новых покупок равен null
                else {
                    Log.d(TAG, "Нулевой список покупок, возвращенный из ответа OK!");
                }
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:   // Пользователь вернул назад
                // или отменил диалог.
                Log.i(TAG, "onPurchasesUpdated: Пользователь отменил покупку");
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:  // Невозможность покупки,
                // поскольку товар уже принадлежит.
                Log.i(TAG, "onPurchasesUpdated: У пользователя уже есть этот предмет");
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR: // API предоставлены неверные аргументы.
                // Эта ошибка также может указывать на то,
                // что приложение не было правильно подписано
                // или неправильно настроено для In-app Billing
                // в Google Play или не имеет необходимых
                // разрешений в своем манифесте.
                Log.e(TAG, "onPurchasesUpdated: ошибка разработчика означает, что Google Play" +
                        " не распознает конфигурацию. Если вы только начинаете, убедитесь, " +
                        "что вы правильно настроили приложение в консоли Google Play. " +
                        "Идентификатор продукта SKU должен совпадать, а APK, который вы " +
                        "используете, должен быть подписан ключами выпуска."
                );
                break;
            default:
                Log.d(TAG, "BillingResult [" + billingResult.getResponseCode() + "]: "
                        + billingResult.getDebugMessage());
        }
        // Для платежного процесса отправляем задачу в основной
        // поток, что бы установить значение false
        billingFlowInProcess.postValue(false);
    }

    /**
     * ========================================================================================
     * В идеале ваша реализация будет включать безопасный сервер, что делает эту проверку ненужной.
     * <p>
     * Ideally your implementation will comprise a secure server, rendering this check unnecessary.
     *
     * @see [Security]
     * ========================================================================================
     */
    private boolean isSignatureValid(@NonNull Purchase purchase) {
        // Проверяет, что данные были подписаны данной подписью
        return Security.verifyPurchase(purchase.getOriginalJson(), purchase.getSignature());
    }

    /**
     * ========================================================================================
     * Рекомендуется запрашивать покупки во время onResume.
     * <p>
     * It's recommended to requery purchases during onResume.
     * <p>
     * Lifecycle.Event.ON_RESUME - Константа для события onResume объекта LifecycleOwner.
     * ========================================================================================
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void resume() {

        Log.d(TAG, "ON_RESUME");
        // Для платежного процесса Возвращает текущее значение.
        // Обратите внимание, что вызов этого метода в фоновом потоке не гарантирует
        // получение последнего набора значений.
        Boolean billingInProcess = billingFlowInProcess.getValue();

        // это просто позволяет избежать дополнительного обновления покупки после
        // завершения процесса выставления счетов
        if (billingSetupComplete && (null == billingInProcess || !billingInProcess)) {
            // refreshPurchasesAsync - асинхронный запрос покупок
            refreshPurchasesAsync();
        }

    }

    /**
     * ====================================================================================
     * SkuState - статусы Sku
     * <p>
     * SKU_STATE_UNPURCHASED - НЕ ПРИОБРЕТАЕТСЯ
     * <p>
     * SKU_STATE_PENDING - В ОЖИДАНИИ
     * <p>
     * SKU_STATE_PURCHASED - КУПЛЕНО
     * <p>
     * SKU_STATE_PURCHASED_AND_ACKNOWLEDGED - КУПЛЕНО И ПРИЗНАНО
     * ====================================================================================
     */
    private enum SkuState {
        SKU_STATE_UNPURCHASED,
        SKU_STATE_PENDING,
        SKU_STATE_PURCHASED,
        SKU_STATE_PURCHASED_AND_ACKNOWLEDGED,
    }
}
