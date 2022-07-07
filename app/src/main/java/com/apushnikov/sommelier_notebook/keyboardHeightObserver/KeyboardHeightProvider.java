/*
 * This file is part of Siebe Projects samples.
 *
 * Siebe Projects samples is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Siebe Projects samples is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with Siebe Projects samples.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apushnikov.sommelier_notebook.keyboardHeightObserver;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

import com.apushnikov.sommelier_notebook.R;


/**
 * Поставщик высоты клавиатуры, этот класс использует PopupWindow для вычисления высоты окна
 * при открытии и закрытии плавающей клавиатуры.
 */
public class KeyboardHeightProvider extends PopupWindow {

    /** Тег для ведения журнала */
    private final static String TAG = "sample_KeyboardHeightProvider";

    /** Наблюдатель высоты клавиатуры */
    private KeyboardHeightObserver observer;

    /** Кэшированная высота клавиатуры в альбомной ориентации */
    private int keyboardLandscapeHeight;

    /** Кешированная портретная высота клавиатуры */
    private int keyboardPortraitHeight;

    /** Вид, который используется для расчета высоты клавиатуры */
    private View popupView;

    /** Родительский view */
    private View parentView;

    /** Корневое действие, использующее этот KeyboardHeightProvider */
    private Activity activity;

    //===========================================================================
    // Конструктор
    //===========================================================================
    /** 
     * Создайте новый KeyboardHeightProvider
     * 
     * @param activity Родительская activity
     */

    public KeyboardHeightProvider(Activity activity) {
		super(activity);
		// Корневое действие, использующее этот KeyboardHeightProvider
        this.activity = activity;

        // LAYOUT_INFLATER_SERVICE - Используйте с getSystemService (java.lang.String)
        // для получения LayoutInflater для увеличения ресурсов макета в этом контексте.
        LayoutInflater inflator = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // Вид, который используется для расчета высоты клавиатуры
        // Раздуваем popupwindow.xml
        this.popupView = inflator.inflate(R.layout.popupwindow, null, false);
        // Установите для содержимого действия явный вид.
        // Это представление помещается непосредственно в иерархию представлений действия.
        // Это может быть сложная иерархия представлений.
        setContentView(popupView);

        // Укажите явный режим мягкого ввода для использования в окне в соответствии с
        // WindowManager.LayoutParams.softInputMode.
        // Предоставление здесь чего-либо, кроме «неопределенного», переопределит режим ввода,
        // который окно обычно извлекает из своей темы.
        // SOFT_INPUT_ADJUST_RESIZE - Параметр настройки для softInputMode: установлен, чтобы
        // разрешить изменение размера окна при отображении метода ввода, чтобы его содержимое
        // не перекрывалось методом ввода. Это не может быть объединено с SOFT_INPUT_ADJUST_PAN;
        // если ни один из них не установлен, система попытается выбрать один или другой в
        // зависимости от содержимого окна. Если флаги параметров макета окна включают
        // FLAG_FULLSCREEN, это значение для softInputMode будет проигнорировано; размер окна
        // не изменится, но останется полноэкранным.
        // SOFT_INPUT_STATE_ALWAYS_VISIBLE - Состояние видимости для softInputMode:
        // всегда делайте программную область ввода видимой, когда это окно получает фокус ввода.
        // В приложениях, нацеленных на Build.VERSION_CODES.P и более поздние версии,
        // этот флаг игнорируется, если нет сфокусированного представления, которое возвращает
        // true из View # isInEditMode (), когда окно сфокусировано.
        setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        // Управляйте тем, как всплывающее окно работает с методом ввода:
        // одним из INPUT_METHOD_FROM_FOCUSABLE, INPUT_METHOD_NEEDED или INPUT_METHOD_NOT_NEEDED.
        // Если всплывающее окно отображается, вызов этого метода вступит в силу только при
        // следующем отображении всплывающего окна или при ручном вызове одного из методов update ().
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

        // Находим Родительский view
        parentView = activity.findViewById(android.R.id.content);

        // Устанавливает требуемую ширину всплывающего окна
        setWidth(0);
        // Устанавливает запрашиваемую высоту всплывающего окна.
        // Может быть константой макета, например LayoutParams # WRAP_CONTENT
        // или LayoutParams # MATCH_PARENT
        setHeight(LayoutParams.MATCH_PARENT);

        // getViewTreeObserver() - Возвращает ViewTreeObserver для иерархии этого представления.
        // Наблюдатель дерева представлений может использоваться для получения уведомлений,
        // когда происходят глобальные события, такие как макет
        // addOnGlobalLayoutListener - Зарегистрируйте обратный вызов, который будет вызываться
        // при изменении состояния глобального макета или видимости представлений в дереве представлений.
        // OnGlobalLayoutListener() - Определение интерфейса для обратного вызова, который будет
        // вызываться при изменении состояния глобального макета или видимости представлений
        // в дереве представлений
        popupView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (popupView != null) {
                        // Само всплывающее окно размером с окно Activity. Затем клавиатура может быть рассчитана
                        // путем извлечения нижней части всплывающего окна из высоты окна активности
                        handleOnGlobalLayout();
                    }
                }
            });
    }

    //===========================================================================
    // Запустите KeyboardHeightProvider, он должен вызываться после onResume Activity.
    // PopupWindows не могут быть зарегистрированы до завершения действия onResume.
    //===========================================================================
    public void start() {

        // Если это PopupWindow не отображается на экране И
        // Уникальный токен, идентифицирующий окно, к которому прикреплено это представление != null
        if (!isShowing() && parentView.getWindowToken() != null) {
            // Задает фон, который можно рисовать для этого всплывающего окна
            setBackgroundDrawable(new ColorDrawable(0));
            // Отобразите представление содержимого во всплывающем окне в указанном месте.
            // Если всплывающее окно не помещается на экране, оно будет обрезано
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    //===========================================================================
    // Закройте поставщик высоты клавиатуры, этот поставщик больше не будет использоваться.
    //===========================================================================
    public void close() {
        // Наблюдатель высоты клавиатуры
        this.observer = null;
        // Удаляет всплывающее окно. Этот метод можно вызвать только после выполнения
        // showAsDropDown (android.view.View). В противном случае вызов этого метода
        // не будет иметь никакого эффекта.
        dismiss();
    }

    //===========================================================================
    // Установите наблюдателя высоты клавиатуры для этого провайдера.
    // Наблюдатель будет уведомлен об изменении высоты клавиатуры.
    // Например, когда клавиатура открыта или закрыта.
    //===========================================================================
    /**
     * @param observer Наблюдатель будет добавлен к этому провайдеру.
     */
    public void setKeyboardHeightObserver(KeyboardHeightObserver observer) {
        this.observer = observer;
    }

    //===========================================================================
    // Само всплывающее окно размером с окно Activity. Затем клавиатура может быть рассчитана
    // путем извлечения нижней части всплывающего окна из высоты окна активности
    //===========================================================================
    private void handleOnGlobalLayout() {

        // screenSize - это точка
        // Точка содержит две целочисленные координаты
        Point screenSize = new Point();
        // По запросу от активности (с помощью getWindowManager () или (WindowManager)
        // getSystemService (Context.WINDOW_SERVICE)) результирующий размер будет соответствовать
        // текущему размеру окна приложения. В этом случае он может быть меньше физического
        // размера в многооконном режиме.
        // getSize - Получает размер дисплея в пикселях. Значение, возвращаемое этим методом,
        // не обязательно представляет фактический необработанный размер (собственное разрешение) дисплея.
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);

        // Прямоугольник
        // Rect содержит четыре целочисленные координаты прямоугольника.
        // Прямоугольник представлен координатами его 4-х сторон (левого, верхнего, правого нижнего).
        // К этим полям можно получить доступ напрямую. Используйте width () и height (),
        // чтобы получить ширину и высоту прямоугольника.
        Rect rect = new Rect();
        // Получить общий размер видимого экрана, в котором было расположено окно,
        // к которому прикреплено это представление
        popupView.getWindowVisibleDisplayFrame(rect);

        // НАПОМИНАНИЕ, вы можете изменить это, используя полноэкранный размер телефона,
        // а также используя строку состояния и высоту панели навигации телефона для расчета
        // высоты клавиатуры. Но это нормально работало на Nexus.
        // Получает ориентацию экрана
        int orientation = getScreenOrientation();
        int keyboardHeight = screenSize.y - rect.bottom;
        
        if (keyboardHeight == 0) {
            notifyKeyboardHeightChanged(0, orientation);
        }
        else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.keyboardPortraitHeight = keyboardHeight; 
            notifyKeyboardHeightChanged(keyboardPortraitHeight, orientation);
        }
        else {
            this.keyboardLandscapeHeight = keyboardHeight; 
            notifyKeyboardHeightChanged(keyboardLandscapeHeight, orientation);
        }
    }

    //===========================================================================
    // Получает ориентацию экрана
    //===========================================================================
    private int getScreenOrientation() {
        return activity.getResources().getConfiguration().orientation;
    }
    
    private void notifyKeyboardHeightChanged(int height, int orientation) {
        if (observer != null) {
            observer.onKeyboardHeightChanged(height, orientation);
        }
    }
}
