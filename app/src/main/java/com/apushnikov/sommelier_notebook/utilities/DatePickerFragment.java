package com.apushnikov.sommelier_notebook.utilities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

// Диалог выбора даты
// Для передачи выбранной даты используется интерфейс
// Возвращает класс DateDDMMYYYY
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

/*    // Строка результата в формате ДД.ММ.ГГГГ
    private String mReturn;*/

    //============================================================================
    // Описываем интерфейс onReturnDateListener для передачи выбранной в диалоге даты
    //============================================================================
    public interface onReturnDateListener {
        public void returnDate(DateDDMMYYYY dateDDMMYYYY);
    }
    public onReturnDateListener returnDateListener;
    //============================================================================

    // Это конткест вызывающего активити
    private Context context;
    private int year = 0;
    private int month = 0;
    private int day = 0;

    //============================================================================
    // Конструктор
    //============================================================================
    //      - Запоминаем контекст вызывающего активити
    public DatePickerFragment(Context context, int year, int month, int day) {
        // Запоминаем контекст вызывающего активити
        this.context = context;
        this.year = year;
        this.month = month-1;
        this.day = day;

        try {
            returnDateListener = (onReturnDateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onReturnDateListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
/*        // Использовать текущую дату как дату по умолчанию в средстве выбора
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);*/

        // Создайте новый экземпляр DatePickerDialog и верните его
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public class DateDDMMYYYY{
//        public String mReturn; // Строка результата в формате ДД.ММ.ГГГГ
        public int mYear;
        public int mMonth;
        public int mDay;
    }

    // Сделайте что-нибудь с датой, выбранной пользователем
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Увеличиваем месяц, т.к. месяцы нумеруются от 0 до 11
        month++;

        DateDDMMYYYY dateDDMMYYYY = new DateDDMMYYYY();
        dateDDMMYYYY.mDay = dayOfMonth;
        dateDDMMYYYY.mMonth = month;
        dateDDMMYYYY.mYear = year;
/*        // Строка результата в формате ДД.ММ.ГГГГ
        dateDDMMYYYY.mReturn = Utilities.getDateWine(1, dayOfMonth, month, year);*/

        // Вызываем через интерфейс обработчик события
        returnDateListener.returnDate(dateDDMMYYYY);
    }

}
