package com.apushnikov.sommelier_notebook.ui.chooseFromPictures;

import static android.app.Activity.RESULT_OK;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.CHOOSE_FROM_PICTURES_INDEX;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.CHOOSE_FROM_PICTURES_POSITION_FROM;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.CHOOSE_FROM_PICTURES_WINE_SHELF_FROM;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.db.WineShelf;

import org.jetbrains.annotations.NotNull;

import java.util.List;

//====================================================================================
// Адаптер для экрана для выбора картинки для винных полок из готового списка
//====================================================================================
public class ChooseFromPicturesAdapter extends
        RecyclerView.Adapter<ChooseFromPicturesAdapter.MyViewChooseFromPicturesHolder> {

    // Это контекст ChooseFromPictures
    Context context;

    // Массив индексов картинок
    private List<Integer> viewPictures;
    private LayoutInflater mInflater;

    // Переданная винная полка
    WineShelf wineShelf;
    // Переданная позиция адаптера
    int position;

    //=========================================================================
    // Конструктор
    //=========================================================================
    ChooseFromPicturesAdapter(
            Context context,
            List<Integer> viewPictures,
            WineShelf wineShelf,
            int position) {

        // Это контекст ChooseFromPictures
        this.context = context;

        this.mInflater = LayoutInflater.from(context);
        this.viewPictures = viewPictures;
        this.wineShelf = wineShelf;
        this.position = position;
    }

    //=========================================================================
    // при необходимости раздувает макет строки из xml
    //=========================================================================
    @NotNull
    @Override
    public MyViewChooseFromPicturesHolder onCreateViewHolder(
            @NotNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_list_pictures_layout, parent, false);
        return new MyViewChooseFromPicturesHolder(view);

    }

    //=========================================================================
    // связывает данные с представлением и текстовым представлением в каждой строке
    //=========================================================================
    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewChooseFromPicturesHolder holder, int position) {

        // Передаем индекс картинки
        holder.setData(viewPictures.get(position));
    }

    //=========================================================================
    // общее количество строк
    //=========================================================================
    @Override
    public int getItemCount() {
        return viewPictures.size();
    }

    //=========================================================================
    // сохраняет и перерабатывает просмотры по мере их прокрутки за пределы экрана
    //=========================================================================
    public class MyViewChooseFromPicturesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView vPictures;

        //============================================================================
        // Конструктор
        //============================================================================
        MyViewChooseFromPicturesHolder (View itemView) {
            super(itemView);
            vPictures = itemView.findViewById(R.id.vPictures);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent myIntent = new Intent();
            // Помещаем в интетент возврата индекс выбранной картинки
            myIntent.putExtra(CHOOSE_FROM_PICTURES_INDEX, viewPictures.get(getAdapterPosition()));
            // Погружаем в интент винную полку (целиком) за счет того, что класс WineShelf сериализуем
            myIntent.putExtra(CHOOSE_FROM_PICTURES_WINE_SHELF_FROM, wineShelf);
            // Погружаем в интент позицию адаптера
            myIntent.putExtra(CHOOSE_FROM_PICTURES_POSITION_FROM, position);

            ((AppCompatActivity) context).setResult(RESULT_OK, myIntent);
            ((AppCompatActivity) context).finish();
        }

        //============================================================================
        // setData - заполняет объекте-держателе представления винные полки
        // String nameShelf - Наименование винной полки
        //============================================================================
        public void setData(Integer number) {

            // Подгоняем размер под экран
            // Абсолютная ширина доступного размера дисплея в пикселях
            int widthPixels = context.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
            // Настроиваем высоту и ширину vPictures
            vPictures.getLayoutParams().height = widthPixels/2;
            vPictures.getLayoutParams().width = widthPixels/2;

            switch (number) {
                case 1:
                    vPictures.setImageResource(R.drawable.wine_shift_01);
                    break;
                case 2:
                    vPictures.setImageResource(R.drawable.wine_shift_02);
                    break;
                case 3:
                    vPictures.setImageResource(R.drawable.wine_shift_03);
                    break;
                case 4:
                    vPictures.setImageResource(R.drawable.wine_shift_04);
                    break;
                case 5:
                    vPictures.setImageResource(R.drawable.wine_shift_05);
                    break;
                case 6:
                    vPictures.setImageResource(R.drawable.wine_shift_06);
                    break;
                case 7:
                    vPictures.setImageResource(R.drawable.wine_shift_07);
                    break;
                case 8:
                    vPictures.setImageResource(R.drawable.wine_shift_08);
                    break;
                case 9:
                    vPictures.setImageResource(R.drawable.wine_shift_09);
                    break;
                case 10:
                    vPictures.setImageResource(R.drawable.wine_shift_10);
                    break;
                case 11:
                    vPictures.setImageResource(R.drawable.wine_shift_11);
                    break;
                case 12:
                    vPictures.setImageResource(R.drawable.wine_shift_12);
                    break;
                case 13:
                    vPictures.setImageResource(R.drawable.wine_shift_13);
                    break;
                case 14:
                    vPictures.setImageResource(R.drawable.wine_shift_14);
                    break;
                case 15:
                    vPictures.setImageResource(R.drawable.wine_shift_15);
                    break;
                case 16:
                    vPictures.setImageResource(R.drawable.wine_shift_16);
                    break;
                case 17:
                    vPictures.setImageResource(R.drawable.wine_shift_17);
                    break;
                case 18:
                    vPictures.setImageResource(R.drawable.wine_shift_18);
                    break;
                case 19:
                    vPictures.setImageResource(R.drawable.wine_shift_19);
                    break;
                case 20:
                    vPictures.setImageResource(R.drawable.wine_shift_20);
                    break;
                case 21:
                    vPictures.setImageResource(R.drawable.wine_shift_21);
                    break;
                case 22:
                    vPictures.setImageResource(R.drawable.wine_shift_22);
                    break;
                default :
                    vPictures.setImageResource(R.drawable.wine_shift_01);
            }
        }

    }

}
