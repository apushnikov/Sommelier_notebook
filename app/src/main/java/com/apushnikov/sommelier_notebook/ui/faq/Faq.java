package com.apushnikov.sommelier_notebook.ui.faq;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.apushnikov.sommelier_notebook.R;

public class Faq extends AppCompatActivity implements View.OnClickListener{

    private TextView tvQuestion01;
    private TextView tvAnswer01;
    private TextView tvQuestion02;
    private TextView tvAnswer02;
    private TextView tvQuestion03;
    private TextView tvAnswer03;
    private TextView tvQuestion04;
    private TextView tvAnswer04;
    private TextView tvQuestion05;
    private TextView tvAnswer05;
    private TextView tvQuestion06;
    private TextView tvAnswer06;
    private TextView tvQuestion07;
    private TextView tvAnswer07;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        // Инициализируем:
        //      - панель инструментов Toolbar
        initToolbar();

        // Инициализируем переменные экрана
        initVariables();
    }

    //====================================================================================
    // Инициализируем:
    //      - панель инструментов Toolbar
    //====================================================================================
    private void initToolbar() {

        // Инициализация toolbar
        Toolbar toolbar = findViewById(R.id.toolbarFaq);
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

        // Заголовок toolbar
        toolbar.setTitle(R.string.tvFAQHeading);
    }

    //====================================================================
    // Инициализируем переменные экрана
    //====================================================================
    private void initVariables() {

        tvQuestion01 = findViewById(R.id.tvQuestion01);
        tvAnswer01 = findViewById(R.id.tvAnswer01);
        tvQuestion02 = findViewById(R.id.tvQuestion02);
        tvAnswer02 = findViewById(R.id.tvAnswer02);
        tvQuestion03 = findViewById(R.id.tvQuestion03);
        tvAnswer03 = findViewById(R.id.tvAnswer03);
        tvQuestion04 = findViewById(R.id.tvQuestion04);
        tvAnswer04 = findViewById(R.id.tvAnswer04);
        tvQuestion05 = findViewById(R.id.tvQuestion05);
        tvAnswer05 = findViewById(R.id.tvAnswer05);
        tvQuestion06 = findViewById(R.id.tvQuestion06);
        tvAnswer06 = findViewById(R.id.tvAnswer06);
        tvQuestion07 = findViewById(R.id.tvQuestion07);
        tvAnswer07 = findViewById(R.id.tvAnswer07);

        tvQuestion01.setOnClickListener(this);
        tvAnswer01.setOnClickListener(this);
        tvQuestion02.setOnClickListener(this);
        tvAnswer02.setOnClickListener(this);
        tvQuestion03.setOnClickListener(this);
        tvAnswer03.setOnClickListener(this);
        tvQuestion04.setOnClickListener(this);
        tvAnswer04.setOnClickListener(this);
        tvQuestion05.setOnClickListener(this);
        tvAnswer05.setOnClickListener(this);
        tvQuestion06.setOnClickListener(this);
        tvAnswer06.setOnClickListener(this);
        tvQuestion07.setOnClickListener(this);
        tvAnswer07.setOnClickListener(this);

    }

    //====================================================================
    // onClick
    //====================================================================
    @Override
    public void onClick(View view) {

        // по id определеяем кнопку, вызвавшую этот обработчик
        switch (view.getId()) {

            // Вопрос - скрываем или раскрываем ответ
            case R.id.tvQuestion01:
                if (tvAnswer01.getVisibility() == View.GONE) {
                    tvAnswer01.setVisibility(View.VISIBLE);
                } else {
                    tvAnswer01.setVisibility(View.GONE);
                }
                break;
            // Ответ - скрываем ответ
            case R.id.tvAnswer01:
                tvAnswer01.setVisibility(View.GONE);
                break;

            // Вопрос - скрываем или раскрываем ответ
            case R.id.tvQuestion02:
                if (tvAnswer02.getVisibility() == View.GONE) {
                    tvAnswer02.setVisibility(View.VISIBLE);
                } else {
                    tvAnswer02.setVisibility(View.GONE);
                }
                break;
            // Ответ - скрываем ответ
            case R.id.tvAnswer02:
                tvAnswer02.setVisibility(View.GONE);
                break;

            // Вопрос - скрываем или раскрываем ответ
            case R.id.tvQuestion03:
                if (tvAnswer03.getVisibility() == View.GONE) {
                    tvAnswer03.setVisibility(View.VISIBLE);
                } else {
                    tvAnswer03.setVisibility(View.GONE);
                }
                break;
            // Ответ - скрываем ответ
            case R.id.tvAnswer03:
                tvAnswer03.setVisibility(View.GONE);
                break;

            // Вопрос - скрываем или раскрываем ответ
            case R.id.tvQuestion04:
                if (tvAnswer04.getVisibility() == View.GONE) {
                    tvAnswer04.setVisibility(View.VISIBLE);
                } else {
                    tvAnswer04.setVisibility(View.GONE);
                }
                break;
            // Ответ - скрываем ответ
            case R.id.tvAnswer04:
                tvAnswer04.setVisibility(View.GONE);
                break;

            // Вопрос - скрываем или раскрываем ответ
            case R.id.tvQuestion05:
                if (tvAnswer05.getVisibility() == View.GONE) {
                    tvAnswer05.setVisibility(View.VISIBLE);
                } else {
                    tvAnswer05.setVisibility(View.GONE);
                }
                break;
            // Ответ - скрываем ответ
            case R.id.tvAnswer05:
                tvAnswer05.setVisibility(View.GONE);
                break;

            // Вопрос - скрываем или раскрываем ответ
            case R.id.tvQuestion06:
                if (tvAnswer06.getVisibility() == View.GONE) {
                    tvAnswer06.setVisibility(View.VISIBLE);
                } else {
                    tvAnswer06.setVisibility(View.GONE);
                }
                break;
            // Ответ - скрываем ответ
            case R.id.tvAnswer06:
                tvAnswer06.setVisibility(View.GONE);
                break;

            // Вопрос - скрываем или раскрываем ответ
            case R.id.tvQuestion07:
                if (tvAnswer07.getVisibility() == View.GONE) {
                    tvAnswer07.setVisibility(View.VISIBLE);
                } else {
                    tvAnswer07.setVisibility(View.GONE);
                }
                break;
            // Ответ - скрываем ответ
            case R.id.tvAnswer07:
                tvAnswer07.setVisibility(View.GONE);
                break;

        }

    }
}