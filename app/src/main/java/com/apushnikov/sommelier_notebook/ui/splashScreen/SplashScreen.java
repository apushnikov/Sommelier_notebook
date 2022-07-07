package com.apushnikov.sommelier_notebook.ui.splashScreen;

import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.isTheProfessionalVersion;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.apushnikov.sommelier_notebook.R;

public class SplashScreen extends AppCompatActivity {

    ImageView imageFullGlass;
    TextView tvIsPremiumText;
    TextView tvIsFreeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

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
        Toolbar toolbar = findViewById(R.id.toolbarSplashScreen);
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
        toolbar.setTitle(R.string.how_to_start);
    }

    //====================================================================
    // Инициализируем переменные экрана
    //====================================================================
    private void initVariables() {

        imageFullGlass = findViewById(R.id.imageFullGlass);
        tvIsPremiumText = findViewById(R.id.tvIsPremiumText);
        tvIsFreeText = findViewById(R.id.tvIsFreeText);

    }

    /**====================================================================================
     * onResume()
     *
     * ====================================================================================
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Проверка премиума
        premiumCheck();
    }

    /**====================================================================================
     * premiumCheck() - Проверка премиума
     *
     * ====================================================================================
     */
    private void premiumCheck() {

        // Если версия профессиональная
        if (isTheProfessionalVersion) {
            // Показываем бокал
            imageFullGlass.setVisibility(View.VISIBLE);
            tvIsPremiumText.setVisibility(View.VISIBLE);
            tvIsFreeText.setVisibility(View.GONE);
        }
        // Если версия НЕ профессиональная
        else {
            // Скрываем бокал
            imageFullGlass.setVisibility(View.GONE);
            tvIsPremiumText.setVisibility(View.GONE);
            tvIsFreeText.setVisibility(View.VISIBLE);
        }

    }

}