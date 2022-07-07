package com.apushnikov.sommelier_notebook.utilities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import com.apushnikov.sommelier_notebook.R;

//===============================================================================
// Окно настроек
//===============================================================================
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Если savedInstanceState == null
        if (savedInstanceState == null) {
            // Вернуть FragmentManager для взаимодействия с фрагментами, связанными с этим действием.
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment()) // Создаем новый SettingsFragment
                    .commit();
        }

        // Инициализация toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_settins);
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
        toolbar.setTitle("Настройки");

/*        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Укажите, должен ли дом отображаться как доступный вариант.
            // Установите значение true, если выбор «домой» возвращается на один уровень
            // в пользовательском интерфейсе, а не на верхний уровень или первую страницу.
            // Чтобы установить сразу несколько параметров отображения, см. Методы setDisplayOptions.
            actionBar.setDisplayHomeAsUpEnabled(false);
        }*/
    }

    // Создаем SettingsFragment
    // PreferenceFragmentCompat - это точка входа в библиотеку Preference.
    // Этот фрагмент отображает для пользователя иерархию объектов предпочтений.
    // Он также обрабатывает сохраняемые значения на устройстве
    // Вы можете определить иерархию предпочтений как ресурс XML или
    // вы можете построить иерархию в коде.
    // В обоих случаях вам нужно использовать PreferenceScreen в качестве корневого компонента
    // в вашей иерархии.
    // Чтобы раздуть из XML, используйте setPreferencesFromResource (int, String)
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Раздуваем
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    //============================================================================
    // Действие по нажатию стрелочки "Назад"
    //============================================================================
    @Override
    public void onBackPressed () {
        finish();
    }



}