package com.apushnikov.sommelier_notebook.utilities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.apushnikov.sommelier_notebook.R;

import org.jetbrains.annotations.NotNull;

// Диалог ввода наименования новой винной полки
// Использует собственный макет
public class NewWineShelfDialogFragment extends DialogFragment {

    private static final String TAG = "myLogs";     //Для логов

    // Параметры - строки для передачи парометов и ввода значения
    private String mNameShelf = "";

    // Переменные экрана диалога
    private TextView nameShelf;

    //======================================================================
    // ИНТЕРФЕЙСЫ
    // Действие, которое создает экземпляр этого фрагмента диалога,
    // должно реализовывать этот интерфейс, чтобы получать обратные вызовы событий.
    // Каждый метод передает DialogFragment на случай, если хосту нужно его запросить.
    //======================================================================
    public interface InputDialogListener {
        public void onDialogPositiveClickNewWineShelf(NewWineShelfDialogFragment dialog);
        public void onDialogNegativeClickNewWineShelf(NewWineShelfDialogFragment dialog);
    }
    // Используйте этот экземпляр интерфейса для доставки событий действия
    InputDialogListener listener;

/*    //======================================================================
    // Конструктор, для передачи начальных значений
    //======================================================================
    public InputDialogFragment(String mName, String mPasword) {
        this.mName = mName;
        this.mPasword = mPasword;
    }*/

    // Переопределите метод Fragment.onAttach () для создания экземпляра InputDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d(TAG, "NewWineShelfDialogFragment: onAttach");

        // Убедитесь, что активность хоста реализует интерфейс обратного вызова
        try {
            // Создайте экземпляр InputDialogListener, чтобы мы могли отправлять события на хост
            listener = (InputDialogListener) context;
        } catch (ClassCastException e) {
            // Действие не реализует интерфейс, выбрасывает исключение
            throw new ClassCastException(" Родительское окно должно реализовать InputDialogListener");
        }
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.AlertDialogTheme);
        // Получить надувной макет
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Надуть макет для диалога
        View viewDialog = inflater.inflate(R.layout.dialog_wine_shelf, null);
        // Зополняем переменные для ввода
        nameShelf = viewDialog.findViewById(R.id.nameShelf);
//        nameShelf.setText(mNameShelf);

        // Установить макет для диалога
        //Передайте null как родительский вид, потому что он идет в макете диалогового окна
        builder.setView(viewDialog)
                // Добавить кнопки действий
                .setPositiveButton(R.string.dialog_new_or_edit_wine_shelf_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Запоминаем результат ввода данных в диалоге
                        mNameShelf = nameShelf.getText().toString();
                        // Отправьте событие положительной кнопки обратно в активность хоста
                        listener.onDialogPositiveClickNewWineShelf(NewWineShelfDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.dialog_new_or_edit_wine_shelf_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Отправить событие отрицательной кнопки обратно в активность хоста
                        listener.onDialogNegativeClickNewWineShelf(NewWineShelfDialogFragment.this);
                    }
                });
        return builder.create();
    }

    public String getNameShelf() {
        return mNameShelf;
    }

}
