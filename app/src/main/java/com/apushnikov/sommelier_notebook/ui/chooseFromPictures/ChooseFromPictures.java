package com.apushnikov.sommelier_notebook.ui.chooseFromPictures;

import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.CHOOSE_FROM_PICTURES_POSITION;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.CHOOSE_FROM_PICTURES_WINE_SHELF;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.db.WineShelf;

import java.util.ArrayList;

//====================================================================================
// Экран для выбора картинки для винных полок из готового списка
//====================================================================================
public class ChooseFromPictures extends AppCompatActivity {

    // Переменная для экрана для RecyclerView лист фото
    private RecyclerView rcListPictures;
    private ChooseFromPicturesAdapter chooseFromPicturesAdapter;

    // Переданная винная полка
    WineShelf wineShelf;
    // Переданная позиция адаптера
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_from_pictures);

        // Инициализируем:
        //      - панель инструментов Toolbar
        initToolbar();

        // Анализируем интент, с которого началось активити
        getMyIntent();

        // Инициализируем GRID RecyclerView для показа фото
        initPicturesView();
    }


    //====================================================================================
    // Инициализируем:
    //      - панель инструментов Toolbar
    //====================================================================================
    private void initToolbar() {

        // Инициализация toolbar
        Toolbar toolbar = findViewById(R.id.toolbarListPictures);

        // Установите значок, который будет использоваться для кнопки навигации панели инструментов
        // - (в данном случае это стрелка назад).
        // Кнопка навигации появляется в начале панели инструментов, если она есть.
        // Установка значка сделает кнопку навигации видимой.
        // Если вы используете значок навигации, вы также должны задать описание его действия с
        // помощью setNavigationContentDescription (int).
        // Это используется для специальных возможностей и всплывающих подсказок.
        //
        //Параметры:
        //      resId - идентификатор ресурса для рисования, чтобы установить
        //      androidx.appcompat.R.attr.navigationIcon
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        // Заголовок toolbar
        toolbar.setTitle(R.string.setTitle_ChooseFromPictures);

        // Установите прослушиватель для ответа на события навигации.
        // Этот слушатель будет вызываться всякий раз, когда пользователь нажимает кнопку
        // навигации в начале панели инструментов. Для отображения кнопки навигации
        // необходимо установить значок.
        //
        //Параметры:
        //      listener - Слушатель для установки
        toolbar.setNavigationOnClickListener(v -> {
            // Вызывается, когда активность обнаруживает нажатие пользователем клавиши возврата.
            // OnBackPressedDispatcher получит возможность обработать кнопку «Назад» до того,
            // как будет вызвано поведение по умолчанию android.app.Activity.onBackPressed ().
            //
            //Заменяет:
            //onBackPressed в классе Activity
            //Смотрите также:
            //getOnBackPressedDispatcher ()
            onBackPressed();
        });

    }

    //====================================================================================
    // Анализируем интент, с которого началось активити
    //====================================================================================
    private void getMyIntent(){

        // Вернуть намерение, с которого началось это активити
        Intent i = getIntent();
        // Если намерение не равно null (т.е. оно есть)
        if (i != null) {
            // Получаем расширенные данные из намерения
            // Используем что класс WineShelf сериализуем Serializable, поэтому можно в интенте
            // передавать весть класс и брать из интента используя getSerializableExtra
            // Обязательно указываем (WineShelf), что бы знать, что возвращаем именно (WineShelf)
            wineShelf = (WineShelf) i.getSerializableExtra(CHOOSE_FROM_PICTURES_WINE_SHELF);
            // Получаем позицию адаптера
            position = i.getIntExtra(CHOOSE_FROM_PICTURES_POSITION, 0);
        }
    }

    //====================================================================================
    // Инициализируем GRID RecyclerView для показа фото
    //====================================================================================
    private void initPicturesView() {

        // Инициализируем массив
        ArrayList<Integer> viewPictures = new ArrayList<>();
        for (int i = 1; i <= 22; i++) {
            viewPictures.add(i);
        }

        rcListPictures = findViewById(R.id.rcListPictures);
        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(
                        this,
                        2);
        rcListPictures.setLayoutManager(gridLayoutManager);
        chooseFromPicturesAdapter = new ChooseFromPicturesAdapter(
                this,
                viewPictures,
                wineShelf,
                position);
        rcListPictures.setAdapter(chooseFromPicturesAdapter);
    }

    //============================================================================
    // Действие по нажатию стрелочки "Назад"
    //============================================================================
    @Override
    public void onBackPressed () {
        // Возвращаем отказ
        Intent myIntent = new Intent();
        setResult(RESULT_CANCELED, myIntent);
        finish();
    }

}