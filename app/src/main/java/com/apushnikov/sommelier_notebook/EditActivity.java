package com.apushnikov.sommelier_notebook;

import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.AD_MANAGER_AD_UNIT_ID;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.AD_MANAGER_AD_UNIT_ID_VIDEO;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PICK_IMAGE_CODE;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.REQUEST_CODE_RECOGNIZER_INTENT;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.REQUEST_LARGE_PHOTO;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.SIMPLE_TEMPLATE_ID;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_HeightOfFloatingButtons;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_KeyboardHeight;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_enable_voice_input;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_rotation;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_theme;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_type_of_assessmen;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_wine_shelf_save_last;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.isTheProfessionalVersion;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.launchVideoAd;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.maxIncrease;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.wine_shelf_save_last_value;
import static com.apushnikov.sommelier_notebook.utilities.Utilities.voiceLineToLineFloat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apushnikov.sommelier_notebook.Global.GlobalConstants;
import com.apushnikov.sommelier_notebook.Global.GlobalKeyWord;
import com.apushnikov.sommelier_notebook.db.AppExecuter;
import com.apushnikov.sommelier_notebook.db.OnDataInsertOrUpdateWine;
import com.apushnikov.sommelier_notebook.db.OnDataReceivedListPhoto;
import com.apushnikov.sommelier_notebook.db.Photo;
import com.apushnikov.sommelier_notebook.db.Wine;
import com.apushnikov.sommelier_notebook.keyboardHeightObserver.KeyboardHeightObserver;
import com.apushnikov.sommelier_notebook.keyboardHeightObserver.KeyboardHeightProvider;
import com.apushnikov.sommelier_notebook.ui.adapterFhotoList.PhotoAdapter;
import com.apushnikov.sommelier_notebook.ui.faq.Faq;
import com.apushnikov.sommelier_notebook.utilities.DatePickerFragment;
import com.apushnikov.sommelier_notebook.utilities.SettingsActivity;
import com.apushnikov.sommelier_notebook.utilities.Utilities;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// EditActivity - просмотр и редактивание карточки вина
// вызывается с помощью интента
public class EditActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener,
        PhotoAdapter.onDeletePhotoInterfaceListener,
        DatePickerFragment.onReturnDateListener,
        OnDataReceivedListPhoto,
        OnDataInsertOrUpdateWine,
        KeyboardHeightObserver {

    //===========================================================================================
    // region: Поля и константы
    //===========================================================================================

    private static final String TAG = "myLogs";     //Для логов

    //============================================================================
    // Это для показа рекламы
    //============================================================================
    /** Открытый интерфейс NativeCustomFormatAd
     * Пользовательский формат адаптивного объявления Менеджера рекламы.
     * Настраиваемые (определяемые пользователем) форматы нативных объявлений позволяют
     * определять собственные переменные для традиционных нативных объявлений.
     * Пользовательские форматы нативной рекламы позволяют определять и отображать
     * собственные ресурсы, а также позволяют определять поведение объявления при нажатии
     */
    private NativeCustomFormatAd nativeCustomFormatAd;

    /** Нативная реклама Google */
    private NativeAd nativeAd;

    /** LinearLayout для наитивной рекламы (понадобится, что бы закрывать рекламу) */
    private LinearLayout llNativeAdView;
    /** LinearLayout для пользовательской рекламы (понадобится, что бы закрывать рекламу) */
    private LinearLayout llAdCustom;

    //============================================================================
    // Это для высоты клавиатуры
    //============================================================================
    // Поставщик высоты клавиатуры, этот класс использует PopupWindow для вычисления высоты окна
    // при открытии и закрытии плавающей клавиатуры.
    private KeyboardHeightProvider keyboardHeightProvider;

    //============================================================================
    // Это для вызова класса DebugScreen (для отладки)
    //============================================================================
    private String myLog = "";

    // экземпляр SharedPreferences
    SharedPreferences prefs = null;

    // Сообщения экрана на экране редактирования
    private TextView tvMessagePhoto;

    // Для скроллирование экрана, чтобы поле ввода было над клавиатурой
    private NestedScrollView mNestedScrollView;

    private FloatingActionButton fabMic;

    //====================================================================
    // TODO: Список сортов вина необходимо куда-то разместить
    //Это для спиннера при выборе сортов вина
    //====================================================================
    private static final String SPINNER_RED = "Красное";
    private static final String SPINNER_WHITE = "Белое";
    private static final String SPINNER_ROSE = "Розовое";
    private static final String SPINNER_ORANGE = "Оранжевое";
    private static final String[] SPINNER_ARRAY_SORT = {
            SPINNER_RED,
            SPINNER_WHITE,
            SPINNER_ROSE,
            SPINNER_ORANGE
            };

    // Номер вина
    private long wineId;
    // Вновь создаваемое или редактируемое вино
    private Wine wine;
    // shelfId: Номер в базе данных винной полки position
    private long shelfId;
    // Вино в начальном состоянии, чтобы при выходе сравнить, изменилось ли
    private Wine oldWine;
    // Основной контейнер окна activity_edit.xml
    private CoordinatorLayout coordinator_layout;

    // Переменная экрана для спиннера
    private Spinner spinnerSort;

    // Переменная для экрана для RecyclerView фото
    private RecyclerView rcPhotoView;
    private PhotoAdapter photoAdapter;
    //====================================================================

    // Переменные экрана
    private EditText edNameWine;

    // Дата - составное поле
    private TextView edDateWine;
    // День
    private int mDateWineDay;
    // Месяц - диапазон от 1 до 12 (при передаче в окно выбора даты нужно уменьшать на 1)
    private int mDateWineMonth;
    // Год
    private int mDateWineYear;

    private EditText edTastingPlace;
    private EditText edCountry;
    private EditText edRegion;
    private EditText edGrapeSort;
    private EditText edYear;
    private EditText edStrength;
    private EditText edPrice;
    private EditText edProducer;
    private EditText edDistributor;
    private EditText edAppearance;
    private EditText edAroma;
    private EditText edTaste;
    private EditText edStoragePotential;
    private EditText edServingTemperature;
    private EditText edGastronomicPartners;
    private EditText edTastingPurchase;
    private EditText edNotes;

    // Переменные экрана для оценки
    private ImageView imageStar1;
    private ImageView imageStar2;
    private ImageView imageStar3;
    private ImageView imageStar4;
    private ImageView imageStar5;

    // Параметры, переданные в интент:
    //      - Константа EDIT_STATE = "edit_state"
    // Смысл переменной isEditState
    //      - Значение true (т.е. передаем интент для редактирования вина)
    //      - Значение false (т.е. передаем интент для создания нового  вина)
    // Значение по умолчанию - false: новое вино
    private boolean isEditState = false;

    // Фото-файл (для нового фото)
    private File mPhotoFile;
    // Абсолютный путь к фото-файлу
    private String mCurrentPhotoPath;

    // endregion

    //===========================================================================================
    // region: Методы
    //===========================================================================================

    //====================================================================
    // Создание EditActivity
    // Инициализация различных переменных
    // Анализируем интент, с которого началось активити EditActivity
    //====================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Log.d(TAG,"EditActivity: onCreate");

        // Инициализируем:
        //      - панель инструментов Toolbar
        initToolbar();

        // Инициализируем переменные экрана для ввода характеристик вина
        initVariablesForEditWine();

        // Инициализируем спиннер для выбора сорта вина
        initSpinnerSort();

        // Инициализируем горизонтальный RecyclerView для показа фото
        initPhotoRecyclerView();

        // Анализируем интент, с которого началось активити EditActivity
        //      Если в интенте передавалось вино, то заполняем данные на экране
        //      Если ничего не передавалось, то вызываем пустой экран
        getMyIntent();

        // По переданному в интенте номеру вина wineId
        // формирует массив из базы данных фотографий для вина (List<Photo>) listPhoto
        // Обновляет фотоадаптер photoAdapter данными из listPhoto
        getMyPhoto();

        // Если это НЕ ПРОФЕССИОНАЛЬНАЯ версия
        // Есть реклама на карточке вина
        if (!isTheProfessionalVersion) {

            //TODO: КОГДА ЗАКОНЧИТСЯ ... И РАЗРЕШАТ РЕКЛАМУ - включить здесь показ рекламы

            // Инициализируем рекламу на карточке вина
//            initAds();
        }

    }

    //====================================================================================
    // Инициализируем:
    //      - панель инструментов Toolbar
    //====================================================================================
    private void initToolbar() {

        Log.d(TAG,"EditActivity: init");

        // toolbar - Находим панель инструментов Toolbar в activity_edit.xml
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Устанавливаем панель инструментов в качестве панели действий (ActionBar) для этого Activity
        setSupportActionBar(toolbar);

        // toolBarLayout - CollapsingToolbarLayout - это оболочка для панели инструментов,
        // которая реализует сворачивающуюся панель приложения.
        // Он предназначен для использования в качестве прямого потомка AppBarLayout
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        // Устанавливает заголовок, который будет отображаться в этом представлении, если он включен
        // В данном случае, в качестве заголовка устанавливаем заголовок Activity
//        toolBarLayout.setTitle(getTitle());
//        toolBarLayout.setTitle("Описание вина");

        // Выключаем расширяющийся заголовок
        toolBarLayout.setTitleEnabled(false);
//        toolBarLayout.setTitleEnabled(true);
//        toolBarLayout.setTitle("Описание вина");


        // Заголовок toolbar
//        toolbar.setTitle("Описание вина");
//        toolbar.setSubtitle("Описание вина");

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
    // Инициализируем переменные экрана
    //====================================================================================
    @SuppressLint("ClickableViewAccessibility")
    private void initVariablesForEditWine() {

        Log.d(TAG,"EditActivity: initVariablesForEditWine");

        // Кнопка микрофон
        fabMic = findViewById(R.id.fabMic);
        // Если нужно включить голосовой ввод
        if (global_enable_voice_input) {
            fabMic.setVisibility(View.VISIBLE);
        }
        // Если нужно выключить голосовой ввод
        else {
            fabMic.setVisibility(View.GONE);
        }

        // Сообщения экрана на экране редактирования
        tvMessagePhoto = findViewById(R.id.tvMessagePhoto);

        // Для скроллирование экрана, чтобы поле ввода было над клавиатурой
        mNestedScrollView = findViewById(R.id.nsvNestedScrollView);

        // Основной контейнер окна activity_edit.xml
        coordinator_layout = findViewById(R.id.coordinator_layout);

        edNameWine = findViewById(R.id.edNameWine);
        edNameWine.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edNameWine);
                }
            }
        });

        edDateWine = findViewById(R.id.edDateWine);

        edTastingPlace = findViewById(R.id.edTastingPlace);
        edTastingPlace.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edTastingPlace);
                }
            }
        });

        edCountry = findViewById(R.id.edCountry);
        edCountry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edCountry);
                }
            }
        });

        edRegion = findViewById(R.id.edRegion);
        edRegion.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edRegion);
                }
            }
        });

        edGrapeSort = findViewById(R.id.edGrapeSort);
        edGrapeSort.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edGrapeSort);
                }
            }
        });

        edYear = findViewById(R.id.edYear);
        edYear.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edYear);
                }
            }
        });

        edStrength = findViewById(R.id.edStrength);
        edStrength.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edStrength);
                }
            }
        });

        edPrice = findViewById(R.id.edPrice);
        edPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edPrice);
                }
            }
        });

        edProducer = findViewById(R.id.edProducer);
        edProducer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edProducer);
                }
            }
        });

        edDistributor = findViewById(R.id.edDistributor);
        edDistributor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edDistributor);
                }
            }
        });

        edAppearance = findViewById(R.id.edAppearance);
        // Делаем поле прокручиваемым внутри NestedScrollView
        edAppearance.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Запретить ScrollView перехватывать события касания.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    // Разрешить ScrollView перехватывать события касания.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            // Обработка событий касания ListView.
            v.onTouchEvent(event);
            return true;
        });
        edAppearance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edAppearance);
                }
            }
        });

        edAroma = findViewById(R.id.edAroma);
        // Делаем поле прокручиваемым внутри NestedScrollView
        edAroma.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Запретить ScrollView перехватывать события касания.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    // Разрешить ScrollView перехватывать события касания.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            // Обработка событий касания ListView.
            v.onTouchEvent(event);
            return true;
        });
        edAroma.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edAroma);
                }
            }
        });

        edTaste = findViewById(R.id.edTaste);
        // Делаем поле прокручиваемым внутри NestedScrollView
        edTaste.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Запретить ScrollView перехватывать события касания.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    // Разрешить ScrollView перехватывать события касания.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            // Обработка событий касания ListView.
            v.onTouchEvent(event);
            return true;
        });
        edTaste.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edTaste);
                }
            }
        });

        edStoragePotential = findViewById(R.id.edStoragePotential);
        edStoragePotential.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edStoragePotential);
                }
            }
        });

        edServingTemperature = findViewById(R.id.edServingTemperature);
        edServingTemperature.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edServingTemperature);
                }
            }
        });

        edGastronomicPartners = findViewById(R.id.edGastronomicPartners);
        edGastronomicPartners.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edGastronomicPartners);
                }
            }
        });

        edTastingPurchase = findViewById(R.id.edTastingPurchase);
        edTastingPurchase.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edTastingPurchase);
                }
            }
        });

        edNotes = findViewById(R.id.edNotes);
        // Делаем поле прокручиваемым внутри NestedScrollView
        edNotes.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Запретить ScrollView перехватывать события касания.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    // Разрешить ScrollView перехватывать события касания.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            // Обработка событий касания ListView.
            v.onTouchEvent(event);
            return true;
        });
        edNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
                    moveUpView(edNotes);
                }
            }
        });

        // Инициализируем Переменные экрана для оценки
        imageStar1 = findViewById(R.id.imageStar1);
        imageStar2 = findViewById(R.id.imageStar2);
        imageStar3 = findViewById(R.id.imageStar3);
        imageStar4 = findViewById(R.id.imageStar4);
        imageStar5 = findViewById(R.id.imageStar5);

        // Инициализируем Переменная экрана для спиннера
        spinnerSort = (Spinner) findViewById(R.id.spinnerSort);

        // Инициация Поставщика высоты клавиатуры
        keyboardHeightProvider = new KeyboardHeightProvider(this);

        // не забудьте запустить поставщик высоты клавиатуры после onResume этого действия.
        // Это связано с тем, что всплывающее окно должно быть инициализировано и прикреплено
        // к корневому представлению активности.
        coordinator_layout.post(new Runnable() {
            public void run() {
                keyboardHeightProvider.start();
            }
        });

    }

    //====================================================================================
    // Инициализируем спиннер для выбора сорта вина
    //====================================================================================
    private void initSpinnerSort() {

        Log.d(TAG,"EditActivity: initSpinnerSort");

        // Создайте ArrayAdapter, используя массив строк для спиннера
        ArrayAdapter<CharSequence> adapter =
                new ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        SPINNER_ARRAY_SORT);
        // Укажите макет, который будет использоваться при появлении списка вариантов спиннера.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Установите адаптер на спиннер.
        spinnerSort.setAdapter(adapter);
        //Зарегистрируйте обратный вызов, который будет вызываться при выборе элемента
        // в этом спиннере (AdapterView). - обработчиком выступает Активити
        spinnerSort.setOnItemSelectedListener(this);
    }

    //====================================================================================
    // Инициализируем горизонтальный RecyclerView для показа фото
    //====================================================================================
    private void initPhotoRecyclerView() {

        Log.d(TAG,"EditActivity: initPhotoRecyclerView");

        rcPhotoView = findViewById(R.id.rcPhotoView);
        LinearLayoutManager horizontalLayoutManager =
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false);
        rcPhotoView.setLayoutManager(horizontalLayoutManager);
        photoAdapter = new PhotoAdapter(this, rcPhotoView);
        rcPhotoView.setAdapter(photoAdapter);
    }


    //====================================================================================
    // Анализируем интент, с которого началось активити EditActivity
    //      Если в интенте передавалось вино, то заполняем данные на экране
    //      Если ничего не передавалось, то вызываем пустой экран
    //====================================================================================
    private void getMyIntent(){

        Log.d(TAG,"EditActivity: getMyIntent");

        // Вернуть намерение, с которого началось это активити
        Intent i = getIntent();
        // Если намерение не равно null (т.е. оно есть)
        if (i != null) {

            Log.d(TAG,"EditActivity: getMyIntent: i != null");

            // Получаем расширенные данные из намерения
            // Используем что класс Wine сериализуем Serializable, поэтому можно в интенте
            // передавать весть класс и брать из интента используя getSerializableExtra
            // Обязательно указываем (Wine), что бы знать, что возвращаем именно (Wine)
            wine = (Wine) i.getSerializableExtra(GlobalConstants.WINE_INTENT);
            // Параметры, переданные в интент:
            //      - Константа EDIT_STATE = "edit_state"
            //      - Значение true (т.е. передаем интент для редактирования вина)
            //      - Значение false (т.е. передаем интент для создания нового  вина)
            // Получитаем расширенные данные из намерения по параметру GlobalConstants.EDIT_STATE
            //      если значение желаемого типа не сохранено с заданным именем,
            //      то берется значение по умолчанию (т.е. false - для создания нового вина)
            isEditState = i.getBooleanExtra(GlobalConstants.EDIT_STATE, false);

            // Если был передан интент для редактирования вина,
            // то заполняем переменные экрана переданными значениями
            if (isEditState) {

                Log.d(TAG,"EditActivity: getMyIntent: !isEditState");

                // Запоминаем номер переданного вина
                wineId = wine.getId();
                // shelfId: Номер в базе данных винной полки position
                shelfId = wine.getShelfId();

                // Заполняем экранные переменные для отображения
                edNameWine.setText(wine.getNameWine());

                edTastingPlace.setText(wine.getTastingPlace());
                edCountry.setText(wine.getCountry());
                edRegion.setText(wine.getRegion());
                spinnerSort.setSelection(wine.getSort());
                edGrapeSort.setText(wine.getGrapeSort());
                // Т.к. поля имеют тип int, то для отображения нужно int привести к String
                // с помощью String.valueOf()
                edYear.setText(String.valueOf(wine.getYear()));
                edStrength.setText(String.valueOf(wine.getStrength()));
                edPrice.setText(String.valueOf(wine.getPrice()));
                edProducer.setText(wine.getProducer());
                edDistributor.setText(wine.getDistributor());
                edAppearance.setText(wine.getAppearance());
                edAroma.setText(wine.getAroma());
                edTaste.setText(wine.getTaste());
                edStoragePotential.setText(wine.getStoragePotential());
                edServingTemperature.setText(wine.getServingTemperature());
                edGastronomicPartners.setText(wine.getGastronomicPartners());
                edTastingPurchase.setText(wine.getTastingPurchase());
                edNotes.setText(wine.getNotes());

            }
            // Если был передан интент для нового вина,
            // то заполняем переменные экрана значениями по умолчанию
            else {
                // Создаем новое вино
                wine = new Wine();

                // При передаче интента для нового вина в экран редактирования, значения,
                // передаваемые через констранту:
                //      - shelfId: Номер в базе данных винной полки position
                shelfId = i.getLongExtra(GlobalConstants.WINE_INTENT_NEW_WINE,0);
                wine.setShelfId(shelfId);

                // Использовать текущую дату как дату по умолчанию в средстве выбора
                final Calendar c = Calendar.getInstance();
                wine.setDateWineYear(c.get(Calendar.YEAR));
                wine.setDateWineMonth(c.get(Calendar.MONTH) + 1);
                wine.setDateWineDay(c.get(Calendar.DAY_OF_MONTH));

            }

            // Инициализируем спиннер, беря значения из интента для сорта вина
            spinnerSort.setSelection(wine.getSort());
            // Рисуем на экране оценку
            drawingRating(wine.getRating());

            // Стоим строку даты в формате ДД.ММ.ГГГГ
            String mGetDateWine = Utilities.getDateWine(
                    1,
                    wine.getDateWineDay(),
                    wine.getDateWineMonth(),
                    wine.getDateWineYear());
            // Выводим дату на экран
            edDateWine.setText(mGetDateWine);
            mDateWineDay = wine.getDateWineDay();
            mDateWineMonth = wine.getDateWineMonth();
            mDateWineYear = wine.getDateWineYear();

            // Запоминаем начальное состояние вина
            rememberInitialState();

        }
    }

    //====================================================================================
    // По переданному в интенте номеру вина wineId
    // формирует начальный массив из базы данных фотографий для вина (List<Photo>) listPhoto
    // Обновляет фотоадаптер photoAdapter данными из listPhoto
    //====================================================================================
    private void getMyPhoto() {

        Log.d(TAG,"EditActivity: getMyPhoto");

        // Получаем массив фотографий по номеру вина wineId во втором потоке
        AppExecuter.getInstance().getSubIO().execute(new Runnable() {
            @Override
            public void run() {
                MainActivity.myDbManager.getListPhotoByWineId_Executer(wineId, EditActivity.this);
            }
        });
        // ВАЖНО - продолжение метода после отработки второго потока
        // в интерфейсе onReceivedListPhoto

    }

    //==============================================================================
    // Интерфейс - действия, когда мы считали массив фотографий
    // Обновление адаптера в ОСНОВНОМ потоке
    //==============================================================================
    @Override
    public void onReceivedListPhoto(List<Photo> list) {
        AppExecuter.getInstance().getMainIO().execute(new Runnable() {
            @Override
            public void run() {
                // Обновляем адаптер
                // updateAdapter - Вызываем обновить адатер (очищаем, заполняем, уведомляем)
                photoAdapter.updateAdapter(list);

                // Обновляем сообщения на главной странице для фото:
                // - Управляет видимостью/невидимостью "Создайте фото или выберите из Галереи"
                updateMessagesPhoto();
            }
        });
    }

    //====================================================================================
    // Настроиваем меню
    // Создаем меню - раздуваем из R.menu.menu_scrolling
    //====================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.d(TAG,"EditActivity: onCreateOptionsMenu");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_edit_menu_scrolling, menu);
        return true;
    }

    //====================================================================
    // Здесь обрабатываются щелчки по элементам панели действий (меню).
    // Панель действий будет автоматически обрабатывать нажатия кнопки «Домой / Вверх»,
    // если вы укажете родительское действие в AndroidManifest.xml.
    //====================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG,"EditActivity: onOptionsItemSelected");

        // Вернуть идентификатор этого пункта меню.
        // Идентификатор нельзя изменить после создания меню
        int id = item.getItemId();

        Intent intent;
        switch (id) {

            //Действие на нажание "Настойки"
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            //Действие на нажание "FAQ"
            case R.id.action_faq:
                intent = new Intent(this, Faq.class);
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //====================================================================
    // Рисуем на экране оценку
    //====================================================================
    public void drawingRating(int mRating) {

        Log.d(TAG,"EditActivity: drawingRating");

        switch (global_type_of_assessmen) {
            case "type_of_assessmen_wineglass":     // Если в качестве оценки показывать бокальчики
                switch (mRating) {
                    case 0:
                        imageStar1.setImageResource(R.drawable.star_not_highlighted_1);
                        imageStar2.setImageResource(R.drawable.star_not_highlighted_1);
                        imageStar3.setImageResource(R.drawable.star_not_highlighted_1);
                        imageStar4.setImageResource(R.drawable.star_not_highlighted_1);
                        imageStar5.setImageResource(R.drawable.star_not_highlighted_1);
                        break;
                    case 1:
                        imageStar1.setImageResource(R.drawable.star_highlighted_1);
                        imageStar2.setImageResource(R.drawable.star_not_highlighted_1);
                        imageStar3.setImageResource(R.drawable.star_not_highlighted_1);
                        imageStar4.setImageResource(R.drawable.star_not_highlighted_1);
                        imageStar5.setImageResource(R.drawable.star_not_highlighted_1);
                        break;
                    case 2:
                        imageStar1.setImageResource(R.drawable.star_highlighted_1);
                        imageStar2.setImageResource(R.drawable.star_highlighted_1);
                        imageStar3.setImageResource(R.drawable.star_not_highlighted_1);
                        imageStar4.setImageResource(R.drawable.star_not_highlighted_1);
                        imageStar5.setImageResource(R.drawable.star_not_highlighted_1);
                        break;
                    case 3:
                        imageStar1.setImageResource(R.drawable.star_highlighted_1);
                        imageStar2.setImageResource(R.drawable.star_highlighted_1);
                        imageStar3.setImageResource(R.drawable.star_highlighted_1);
                        imageStar4.setImageResource(R.drawable.star_not_highlighted_1);
                        imageStar5.setImageResource(R.drawable.star_not_highlighted_1);
                        break;
                    case 4:
                        imageStar1.setImageResource(R.drawable.star_highlighted_1);
                        imageStar2.setImageResource(R.drawable.star_highlighted_1);
                        imageStar3.setImageResource(R.drawable.star_highlighted_1);
                        imageStar4.setImageResource(R.drawable.star_highlighted_1);
                        imageStar5.setImageResource(R.drawable.star_not_highlighted_1);
                        break;
                    case 5:
                        imageStar1.setImageResource(R.drawable.star_highlighted_1);
                        imageStar2.setImageResource(R.drawable.star_highlighted_1);
                        imageStar3.setImageResource(R.drawable.star_highlighted_1);
                        imageStar4.setImageResource(R.drawable.star_highlighted_1);
                        imageStar5.setImageResource(R.drawable.star_highlighted_1);
                        break;
                }
                break;

            case "type_of_assessmen_star":          // Если в качестве оценки показывать звездочки
                switch (mRating) {
                    case 0:
                        imageStar1.setImageResource(R.drawable.star_not_highlighted);
                        imageStar2.setImageResource(R.drawable.star_not_highlighted);
                        imageStar3.setImageResource(R.drawable.star_not_highlighted);
                        imageStar4.setImageResource(R.drawable.star_not_highlighted);
                        imageStar5.setImageResource(R.drawable.star_not_highlighted);
                        break;
                    case 1:
                        imageStar1.setImageResource(R.drawable.star_highlighted);
                        imageStar2.setImageResource(R.drawable.star_not_highlighted);
                        imageStar3.setImageResource(R.drawable.star_not_highlighted);
                        imageStar4.setImageResource(R.drawable.star_not_highlighted);
                        imageStar5.setImageResource(R.drawable.star_not_highlighted);
                        break;
                    case 2:
                        imageStar1.setImageResource(R.drawable.star_highlighted);
                        imageStar2.setImageResource(R.drawable.star_highlighted);
                        imageStar3.setImageResource(R.drawable.star_not_highlighted);
                        imageStar4.setImageResource(R.drawable.star_not_highlighted);
                        imageStar5.setImageResource(R.drawable.star_not_highlighted);
                        break;
                    case 3:
                        imageStar1.setImageResource(R.drawable.star_highlighted);
                        imageStar2.setImageResource(R.drawable.star_highlighted);
                        imageStar3.setImageResource(R.drawable.star_highlighted);
                        imageStar4.setImageResource(R.drawable.star_not_highlighted);
                        imageStar5.setImageResource(R.drawable.star_not_highlighted);
                        break;
                    case 4:
                        imageStar1.setImageResource(R.drawable.star_highlighted);
                        imageStar2.setImageResource(R.drawable.star_highlighted);
                        imageStar3.setImageResource(R.drawable.star_highlighted);
                        imageStar4.setImageResource(R.drawable.star_highlighted);
                        imageStar5.setImageResource(R.drawable.star_not_highlighted);
                        break;
                    case 5:
                        imageStar1.setImageResource(R.drawable.star_highlighted);
                        imageStar2.setImageResource(R.drawable.star_highlighted);
                        imageStar3.setImageResource(R.drawable.star_highlighted);
                        imageStar4.setImageResource(R.drawable.star_highlighted);
                        imageStar5.setImageResource(R.drawable.star_highlighted);
                        break;
                }
                break;
        }

    }

    //====================================================================
    // Определяем действия, при нажатии на звездочки (оценка)
    //====================================================================
    public void onClickRating(View view) {

        Log.d(TAG,"EditActivity: onClickRating");

        // по id определеяем кнопку, вызвавшую этот обработчик
        switch (view.getId()) {
            case R.id.imageStar1:
                // Звездочка 1
                wine.setRating(1);
                break;
            case R.id.imageStar2:
                // Звездочка 2
                wine.setRating(2);
                break;
            case R.id.imageStar3:
                // Звездочка 3
                wine.setRating(3);
                break;
            case R.id.imageStar4:
                // Звездочка 4
                wine.setRating(4);
                break;
            case R.id.imageStar5:
                // Звездочка 5
                wine.setRating(5);
                break;
        }
        // Рисуем на экране оценку
        drawingRating(wine.getRating());

    }

    //====================================================================
    //Это методы для спиннера при выборе сортов вина
    // Фиксируем выбранную позицию спиннера
    //====================================================================
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Log.d(TAG,"EditActivity: onItemSelected");

        // TODO: лучше бы передавать через переменную, как в демо проекте com.example.spinner
        //  через mSelectedNotification
        // Фиксируем выбранную позицию спиннера
        wine.sort = position;
    }

    //===================================================================================
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Required - ничего не делаем
    }

    //===================================================================================
    // Действие на вызов интента startActivityForResult
    //===================================================================================
    //TODO: почему версия Build.VERSION_CODES.KITKAT?
//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Photo photo;

        Log.d(TAG,"EditActivity: onActivityResult");

        // результат RESULT_OK и
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                // Если был запущен интент для выбора фото из галереи
                case PICK_IMAGE_CODE:

                    //TODO: Обработка фото должна выполняться во втором потоке

                    // возвращенные данные не null
                    if (data != null) {
                        // Документ, выбранный пользователем, не будет возвращен в намерении.
                        // Вместо этого URI этого документа будет содержаться в намерении возврата,
                        // предоставленном этому методу в качестве параметра.
                        // Вытяните этот URI с помощью "data.getData ()"
                        Uri uri = null;

                        // Вытяните этот URI с помощью "resultData.getData ()"
                        // Это URI файла-источника
                        uri = data.getData();

                        // необработанный дескриптор файла-источника
                        ParcelFileDescriptor parcelFileDescriptor = null;

                        try {
                            // openFileDescriptor - Откройте дескриптор необработанного файла для
                            // доступа к данным по URI
                            parcelFileDescriptor =
                                    getContentResolver().openFileDescriptor(uri, "r");
                            // Получите фактический FileDescriptor, связанный с этим объектом
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                            // Поток - источник
                            InputStream is = new FileInputStream(fileDescriptor);
                            // Размер потока-источника
                            int ss = is.available();

                            // Это файл-получатель
                            // Обнуляем Фото-файл и абсолютный путь к нему
                            mPhotoFile = null;
                            mCurrentPhotoPath = "";
                            // Генерируем новый фото-файл
                            // (при этом в mCurrentPhotoPath запоминается абсолютный путь к нему)
                            try {
                                mPhotoFile = createImageFile();
                            } catch (IOException ex) {
                                // Ошибка при создании файла
                                Toast.makeText(this, "Файл не был создан", Toast.LENGTH_LONG).show();
                            }
                            // Продолжить, только если файл был успешно создан
                            if (mCurrentPhotoPath != null) {

                                // (Очень простой код для копирования изображения из ресурса приложения во
                                // внешний файл. Обратите внимание, что этот код не проверяет ошибки и предполагает,
                                // что изображение маленькое (не пытается копировать его по частям).
                                // Обратите внимание, что если внешнее хранилище в настоящее время не подключено,
                                // это не сработает.)

                                // Поток - получатель
                                OutputStream os = new FileOutputStream(mPhotoFile);
                                // массив байтов - по размеру потока-источника
                                byte[] dataForTransfer = new byte[ss];
                                // Читает
                                is.read(dataForTransfer);
                                // Записываем
                                os.write(dataForTransfer);
                                // Закрываем
                                is.close();
                                os.close();

                                // Создаем новое photo
                                photo = new Photo();
                                // В запись photo присваиваем номер вина
                                photo.setWineId(wineId);
                                // В запись photo присваиваем абсолютный путь нового фото
                                photo.setPhoto(mCurrentPhotoPath);

                                // Добавить фото в галерею
                                galleryAddPic(mCurrentPhotoPath);

                                // Обновляем адаптер, добавляя в данные адаптера photo и используя notifyItemInserted
                                // Новый элемент еще не в базе данных
                                photoAdapter.updateAdapterPhotoInserted(photo);

                                // Обновляем сообщения на главной странице для фото:
                                // - Управляет видимостью/невидимостью "Создайте фото или выберите из Галереи"
                                updateMessagesPhoto();
                            }

                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "Failed to load image.", e);
                            return;

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (parcelFileDescriptor != null) {
                                    parcelFileDescriptor.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "Error closing ParcelFile Descriptor");
                            }
                        }
                    }
                    break;

                // Если был запущен интент для создания нового фото
                case REQUEST_LARGE_PHOTO:
                    // TODO: Пересмотреть создание фото
                    try {
                        //TODO: Обработка фото должна выполняться во втором потоке (особенно поворот фото)

                        // Если в настойках стоит - при фотографировании нужно поворачивать фото
                        if (!global_rotation.equals("rotation_no")) {

                            int degreeOfRotation;
                            if (global_rotation.equals("Повернуть влево")) {
                                degreeOfRotation = 3;
                            } else {
                                degreeOfRotation = 1;
                            }

                            //==============================
                            // Определяем имя временного файла для сохранения

                            Log.d(TAG, "MyDbManager: photoFileRotation_Executer Создайте имя файла изображения");
                            // Создайте имя файла изображения
                            @SuppressLint("SimpleDateFormat") String timeStamp =
                                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = "JPEG_" + timeStamp + "_";

    /*        // В документации сказано - Правильный каталог для общих фотографий предоставляется
            // getExternalStoragePublicDirectory()с DIRECTORY_PICTURES аргументом.
            // Каталог, предоставленный этим методом, является общим для всех приложений.
            // На Android 9 (уровень 28 API) и ниже, чтение и запись в этот каталог требует
            // READ_EXTERNAL_STORAGE и WRITE_EXTERNAL_STORAGE разрешений, соответственно
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);*/
                            // там же в документации - Однако, если вы хотите, чтобы фотографии оставались
                            // доступными только для вашего приложения, вы можете вместо этого использовать каталог,
                            // предоставленный getExternalFilesDir()
                            // TODO: Что использовать? getExternalFilesDir или getExternalStoragePublicDirectory
    //        File storageDir = ((Context)onDataPhotoFileRotation).getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                            // Генерируем имя фото-файла
                            Log.d(TAG, "MyDbManager: photoFileRotation_Executer Генерируем имя фото-файла");
                            File newFile = null;
                            try {
                                newFile = File.createTempFile(
                                        imageFileName,  /* prefix */
                                        ".jpg",         /* suffix */
                                        storageDir      /* directory */
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //==============================
                            // Только поворачивает фото-изображение
                            // Параметры:
                            //      - mOldPhotoPath - Абсолютный путь к страрому фото-файлу
                            //      - File newFile - Новый файл
                            //      - degreeOfRotation - Степень поворота
                            //              0 - поворот на 0 градусов
                            //              1 - поворот на 90 градусов
                            //              2 - поворот на 180 градусов
                            //              3 - поворот на 270 градусов
                            // Действия:
                            //      - Берет старый фото-фойл
                            //      - Порорачивает его и помещает в новой фото-файле
                            MainActivity.myDbManager.photoFileRotation(
                                    mCurrentPhotoPath,
                                    newFile,
                                    degreeOfRotation);


                            // Заменяем абсолютный путь к файлу на результат поворота
                            mCurrentPhotoPath = newFile.getAbsolutePath();

                        }

                        // Создаем новое photo
                        photo = new Photo();
                        // В запись photo присваиваем номер вина
                        photo.setWineId(wineId);
                        // В запись photo присваиваем абсолютный путь нового фото
                        photo.setPhoto(mCurrentPhotoPath);

                        // Добавить фото в галерею
                        galleryAddPic(mCurrentPhotoPath);

                        // Обновляем адаптер, добавляя в данные адаптера photo и используя notifyItemInserted
                        // Новый элемент еще не в базе данных
                        photoAdapter.updateAdapterPhotoInserted(photo);

                        // Обновляем сообщения на главной странице для фото:
                        // - Управляет видимостью/невидимостью "Создайте фото или выберите из Галереи"
                        updateMessagesPhoto();
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.error_occurred_while_trying_to_take_photo, Toast.LENGTH_LONG).show();
//                        e.printStackTrace();
                    }
                    break;

                // Если был запущен интент для распознавания речи
                // считываем из интента распознанную речь
                case REQUEST_CODE_RECOGNIZER_INTENT:
                    // возвращенные данные не null
                    if(data != null) {
                        ArrayList<String> text =
                                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        // Вызываем обработчик голосовых команд, передаем распознанную речь
                        textCommand(text.get(0));
                    }
                    break;
            }

        }
    }

    //===================================================================================
    // Работа с фотографиями
    // Вызываем диалог "Сделать фото или взять из галереи?"
    //===================================================================================
    public void onClickNewPhoto(View view) {
        // Используйте класс Builder для удобного построения диалогов
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogTheme);
        builder
                .setTitle(R.string.dialog_new_photo)    // Заголовок диалога
                // Позитивная кнопка - сделать фото
                .setPositiveButton(R.string.dialog_to_make_a_photo, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {
                        // Делаем новую фотографию
                        makePhoto();
                    }
                })
                // Нейтральная кнопка - взять из галереи
                .setNeutralButton(R.string.dialog_take_from_gallery, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Загружаем фото из галереи
                        takePhotoFromGallery();
                    }
                });
        // Создаем диалог
        AlertDialog alert = builder.create();
        // Показываем диалог
        alert.show();

    }

    //============================================================================
    // Загружаем фото из галереи
    //============================================================================
    public void takePhotoFromGallery() {

        Intent chooserPhoto = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        chooserPhoto.setType("image/*");

        // TODO: Заменить устаревший вызов startActivityForResult на более современное
        // Запускаем интент, ожидая результата
        startActivityForResult(chooserPhoto, PICK_IMAGE_CODE);

    }

    //============================================================================
    // Делаем новую фотографию
    //============================================================================
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("QueryPermissionsNeeded")
    public void makePhoto() {

        // Проверяем, есть ли разрешение на камеру
        int cameraPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (cameraPermission == PackageManager.PERMISSION_GRANTED) {    // Если есть разрешение на камеру
            try {
                // Создайте неявное намерение для захвата изображения.
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Убедитесь, что есть активность камеры для обработки намерения
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
    /*            // Запоминаем старый фото-файл и абсолютный путь к нему
                mOldPhotoFile = mPhotoFile;
                mOldCurrentPhotoPath = mCurrentPhotoPath;*/
                    // Обнуляем Фото-файл и абсолютный путь к нему
                    mPhotoFile = null;
                    mCurrentPhotoPath = "";
                    // Генерируем новый фото-файл
                    // (при этом в mCurrentPhotoPath запоминается абсолютный путь к нему)
                    try {
                        mPhotoFile = createImageFile();
                    } catch (IOException ex) {
                        // Ошибка при создании файла
                        Toast.makeText(this, R.string.file_was_not_created, Toast.LENGTH_LONG).show();
                    }
                    // Продолжить, только если файл был успешно создан
                    if (mCurrentPhotoPath != null) {
                        // Возвращает URI-содержимого для данного файла mPhotoFile
                        // FileProvider может возвращать Uri содержимого только для путей к файлам,
                        // определенных в их элементе метаданных <paths>
                        Uri mPhotoURI = FileProvider.getUriForFile(this,
                                "com.apushnikov.sommelier_notebook.fileprovider",
                                mPhotoFile);
                        // В неявное намерение для захвата изображения кладем URI-содержимого
                        // для файла mPhotoFile
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);

                        // TODO: Заменить устаревший вызов startActivityForResult на более современное
                        // Вызываем интент и ожидаем результат
                        startActivityForResult(takePictureIntent, REQUEST_LARGE_PHOTO);
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, R.string.error_occurred_while_trying_to_take_photo, Toast.LENGTH_LONG).show();
//                e.printStackTrace();
            }
        }
        else                // Если нет разрешения на камеру, запрашиваем его
        {
            this.requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    GlobalConstants.REQUEST_ID_CAMERA_PERMISSION);
        }
    }

    //============================================================================
    // Когда у вас есть результаты запроса на разрешения
    //============================================================================
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case GlobalConstants.REQUEST_ID_CAMERA_PERMISSION: {
                // Примечание. Если запрос отменен, массивы результатов пусты.
                // Предоставленные разрешения (чтение / запись)
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, R.string.camera_permission_GOT, Toast.LENGTH_LONG).show();
                }
                // Cancelled or denied.
                else {
                    Toast.makeText(this, R.string.access_to_the_camera_is_DENIED, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    // ============================================================================
    // Генерация имени фото-файла
    // TODO: Этот метод нужно перенести, вместе с файловыми операциями в класс MyDbManager
    // ============================================================================
    private File createImageFile() throws IOException {
        // Создайте имя файла изображения
        @SuppressLint("SimpleDateFormat") String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

/*        // В документации сказано - Правильный каталог для общих фотографий предоставляется
        // getExternalStoragePublicDirectory()с DIRECTORY_PICTURES аргументом.
        // Каталог, предоставленный этим методом, является общим для всех приложений.
        // На Android 9 (уровень 28 API) и ниже, чтение и запись в этот каталог требует
        // READ_EXTERNAL_STORAGE и WRITE_EXTERNAL_STORAGE разрешений, соответственно
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);*/
        // там же в документации - Однако, если вы хотите, чтобы фотографии оставались
        // доступными только для вашего приложения, вы можете вместо этого использовать каталог,
        // предоставленный getExternalFilesDir()
        // TODO: Что использовать? getExternalFilesDir или getExternalStoragePublicDirectory
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Генерируем имя фото-файла
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Сохранить абсолютный путь к файлу
        mCurrentPhotoPath = image.getAbsolutePath();
        // Возвращаем имя фото-файла
        return image;
    }

    // ============================================================================
    // Добавить фото в галерею
    // Парамерт: String currentPhotoPath - Абсолютный путь
    // TODO: Этот метод нужно перенести, вместе с файловыми операциями в класс MyDbManager
    // ============================================================================
    private void galleryAddPic(String currentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    //============================================================================
    // Кнопка - Сохранение вина
    //============================================================================
    public void onClickSave(View view) {

        Log.d(TAG,"EditActivity: onClickSave");

        // Берем экранные переменные и размещаем их в wine для последующего сохранения
        getScreenVariablesToWine();
        // Сохранить и выйти
        saveAndExitWineCard();

    }

    //============================================================================
    // Берем экранные переменные и размещаем их в wine для последующего сохранения
    //============================================================================
    private void getScreenVariablesToWine() {

        Log.d(TAG,"EditActivity: getScreenVariablesToWine");

        wine.setNameWine(edNameWine.getText().toString());
        wine.setDateWineDay(mDateWineDay);
        wine.setDateWineMonth(mDateWineMonth);
        wine.setDateWineYear(mDateWineYear);
        wine.setTastingPlace(edTastingPlace.getText().toString());
        wine.setCountry(edCountry.getText().toString());
        wine.setRegion(edRegion.getText().toString());
        // Позиция спиннера для выбора сорта сразу фиксируется в методе onItemSelected:
        // wine.sort = position;
//        wine.setSort((int)spinnerSort.getSelectedItemId());
        wine.setGrapeSort(edGrapeSort.getText().toString());
        if (!edYear.getText().toString().equals("")) {
            wine.setYear(Integer.parseInt(edYear.getText().toString()));
        }
        if (!edStrength.getText().toString().equals("")) {
            wine.setStrength(Float.parseFloat(edStrength.getText().toString()));
        }
        if (!edPrice.getText().toString().equals("")) {
            wine.setPrice(Float.parseFloat(edPrice.getText().toString()));
        }
        wine.setProducer(edProducer.getText().toString());
        wine.setDistributor(edDistributor.getText().toString());
        wine.setAppearance(edAppearance.getText().toString());
        wine.setAroma(edAroma.getText().toString());
        wine.setTaste(edTaste.getText().toString());
        wine.setStoragePotential(edStoragePotential.getText().toString());
        wine.setServingTemperature(edServingTemperature.getText().toString());
        wine.setGastronomicPartners(edGastronomicPartners.getText().toString());
        wine.setTastingPurchase(edTastingPurchase.getText().toString());
        wine.setNotes(edNotes.getText().toString());
        // Оценка сразу фиксируется в методе onClickRating:
        // wine.setRating(1);

    }

    //================================================================================
    // Обработчик голосовых команд
    // Действия на нажати кнопки "Mic" (микрофон)
    //================================================================================

    // Действия на нажати кнопки "Mic" (микрофон)
    public void onClickMic(View view) {

        //==========================================================================
        // Для отладки
/*        String ttt = "оценка 10";

        textCommand(ttt);*/
        //==========================================================================

        // Хотим запустить распознавание голоса
        // ACTION_RECOGNIZE_SPEECH - Запускает действие, которое запрашивает у пользователя речь
        // и отправляет ее через распознаватель речи
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Модель языка - свободная форма
        //      EXTRA_LANGUAGE_MODEL - Информирует распознаватель, какую модель речи выбрать
        //                              при выполнении ACTION_RECOGNIZE_SPEECH
        //      LANGUAGE_MODEL_FREE_FORM - Используйте языковую модель, основанную на распознавании
        //                              речи произвольной формы. Это значение, которое следует
        //                              использовать для EXTRA_LANGUAGE_MODEL
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Будет считывать язык, который установлен на смартфоне
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //В процессе распознования выводим сообщение
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,this.getString(R.string.say_KEY_word_followed_by_text));

        try {
            // TODO: Заменить устаревший вызов startActivityForResult на более современное
            // Запускаем интернт и ожидаем результата
            startActivityForResult(intent, REQUEST_CODE_RECOGNIZER_INTENT);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(coordinator_layout,
                    R.string.missing_or_not_configured_Google_Voice_Typing,
                    Snackbar.LENGTH_LONG).show();
        } catch (Exception e) {
            Snackbar.make(coordinator_layout,
                    R.string.error_occurred_while_trying_to_invoke_speech_recognizer,
                    Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    // ============================================================================
    // Обработка распознанной речи
    // ============================================================================
    private void textCommand(String text) {

        myLog = myLog + "\n ПРОДИКТОВАНО: " + text;


        // Первое ключевое слово
        GlobalKeyWord.ResultKeyWord firstResultKeyWord;
        firstResultKeyWord = new GlobalKeyWord.ResultKeyWord();
        // Второе ключевое слово
        GlobalKeyWord.ResultKeyWord secondResultKeyWord;
        secondResultKeyWord = new GlobalKeyWord.ResultKeyWord();

        String textOld = "";

        // Если строка не пуста
        if (!text.equals("")) {
            // Берем первое ключевое слово (если оно есть)
            firstResultKeyWord = GlobalKeyWord.thereIsKeyWord(text);
            //Анализ ключевых слов
            switch (firstResultKeyWord.keyWord) {

                case GlobalKeyWord.KEY_WORD_NOTHING : // ключевых слов не найдено
                    // Делаем первую букву заглавной
                    text = text.substring(0,1).toUpperCase()+ text.substring(1);
                    // Проверяем, есть ли уже что-то в поле edNotes
                    textOld = edNotes.getText().toString();
                    if (!textOld.equals("")) {
                        // Если уже есть текс, то добавляем новую строку и вставляем текст
                        text = textOld + "\n" + text;
                    }
                    edNotes.setText(text);
                    // Устанавливаем на фокус на поле
                    edNotes.requestFocus();
                    break;

                case GlobalKeyWord.KEY_WORD_NAME_WINE : // Наименование вина
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edNameWine.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edNameWine.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_TASTINGPLACE : // Место дегустации
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edTastingPlace.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edTastingPlace.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_COUNTRY : // Страна
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edCountry.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edCountry.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_REGION : // Регион
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edRegion.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edRegion.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_GRAPE_SORT : // Сорт винограда
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edGrapeSort.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edGrapeSort.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_SORT : // Цвет
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Берем второе ключевое слово (если оно есть)
                        secondResultKeyWord = GlobalKeyWord.thereIsKeyWord(firstResultKeyWord.rest);
                        // TODO: Это хард-код. Нужно подумать, где фиксировать сорт вина (красное, белое...)
                        //Анализ ключевых слов
                        switch (secondResultKeyWord.keyWord) {
                            case GlobalKeyWord.KEY_WORD_SORT_RED: //Красное
                                // Присваиваем вину - красное
                                wine.setSort(0);
                                // Устанавливаем спиннер
                                spinnerSort.setSelection(wine.getSort());
                                // Устанавливаем на фокус на поле
                                spinnerSort.requestFocus();
                                break;
                            case GlobalKeyWord.KEY_WORD_SORT_WHITE: //Белое
                                // Присваиваем вину - белое
                                wine.setSort(1);
                                // Устанавливаем спиннер
                                spinnerSort.setSelection(wine.getSort());
                                // Устанавливаем на фокус на поле
                                spinnerSort.requestFocus();
                                break;
                            case GlobalKeyWord.KEY_WORD_SORT_ROSE: //Розовое
                                // Присваиваем вину - розовое
                                wine.setSort(2);
                                // Устанавливаем спиннер
                                spinnerSort.setSelection(wine.getSort());
                                // Устанавливаем на фокус на поле
                                spinnerSort.requestFocus();
                                break;
                            case GlobalKeyWord.KEY_WORD_SORT_ORANGE: //Оранжевое
                                // Присваиваем вину - оранжевое
                                wine.setSort(3);
                                // Устанавливаем спиннер
                                spinnerSort.setSelection(wine.getSort());
                                // Устанавливаем на фокус на поле
                                spinnerSort.requestFocus();
                                break;                            default:
                                Snackbar.make(coordinator_layout,
                                        R.string.sort_not_recognized + " \"" +
                                                firstResultKeyWord.rest + "\"",
                                        Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_YEAR : // Год урожая
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Остаток строки преобразум в int (отбрасывая нечисловые символы)
                        int i = 0;
                        String temp = firstResultKeyWord.rest.replaceAll("[\\D]", "");

                        try {
                            i = Integer.parseInt(temp);
                        } catch (NumberFormatException e) {
                            Snackbar.make(coordinator_layout,
                                    R.string.year_not_recognized + " \"" +
                                            firstResultKeyWord.rest + "\"",
                                    Snackbar.LENGTH_LONG).show();
                        }
                        // Выводим на экран
                        edYear.setText(String.valueOf(i));
                        // Устанавливаем на фокус на поле
                        edYear.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_STRENGTH : // Крепость
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Преобразует голосовую строку в строку типа float
                        // Распознает в качестве разделителя точку или запятую
                        String strFloat = null;
                        try {
                            strFloat = voiceLineToLineFloat(firstResultKeyWord.rest);
                        } catch (Exception e) {
                            Snackbar.make(coordinator_layout,
                                    R.string.strength_not_recognized + " \"" +
                                            firstResultKeyWord.rest + "\"",
                                    Snackbar.LENGTH_LONG).show();
                        }
                        // Выводим на экран
                        if (strFloat != null) edStrength.setText(strFloat);
                        // Устанавливаем на фокус на поле
                        edStrength.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_PRICE : // Цена
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Преобразует голосовую строку в строку типа float
                        // Распознает в качестве разделителя точку или запятую
                        String strFloat = null;
                        try {
                            strFloat = voiceLineToLineFloat(firstResultKeyWord.rest);
                        } catch (Exception e) {
                            Snackbar.make(coordinator_layout,
                                    R.string.price_not_recognized + " \"" +
                                            firstResultKeyWord.rest + "\"",
                                    Snackbar.LENGTH_LONG).show();
                        }
                        // Выводим на экран
                        if (strFloat != null) edPrice.setText(strFloat);
                        // Устанавливаем на фокус на поле
                        edPrice.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_PRODUCER : // Производитель
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edProducer.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edProducer.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_DISTRIBUTOR : // Дистрибьютор
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edDistributor.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edDistributor.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_APPEARANCE: // Внешний вид
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Делаем первую букву заглавной
                        firstResultKeyWord.rest = firstResultKeyWord.rest.
                                substring(0, 1).toUpperCase() +
                                firstResultKeyWord.rest.substring(1);
                        // Проверяем, есть ли уже что-то в поле edAppearance
                        textOld = edAppearance.getText().toString();
                        if (!textOld.equals("")) {
                            // Если уже есть текст, то добавляем новую строку и вставляем текст
                            firstResultKeyWord.rest = textOld + "\n" + firstResultKeyWord.rest;
                        }
                        // Выводим на экран
                        edAppearance.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edAppearance.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_AROMA : // Аромат (характеристики)
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Делаем первую букву заглавной
                        firstResultKeyWord.rest = firstResultKeyWord.rest.
                                substring(0, 1).toUpperCase() +
                                firstResultKeyWord.rest.substring(1);
                        // Проверяем, есть ли уже что-то в поле edAroma
                        textOld = edAroma.getText().toString();
                        if (!textOld.equals("")) {
                            // Если уже есть текст, то добавляем новую строку и вставляем текст
                            firstResultKeyWord.rest = textOld + "\n" + firstResultKeyWord.rest;
                        }
                        // Выводим на экран
                        edAroma.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edAroma.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_TASTE : // Вкусовые характеристики
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Делаем первую букву заглавной
                        firstResultKeyWord.rest = firstResultKeyWord.rest.
                                substring(0, 1).toUpperCase() +
                                firstResultKeyWord.rest.substring(1);
                        // Проверяем, есть ли уже что-то в поле edTaste
                        textOld = edTaste.getText().toString();
                        if (!textOld.equals("")) {
                            // Если уже есть текст, то добавляем новую строку и вставляем текст
                            firstResultKeyWord.rest = textOld + "\n" + firstResultKeyWord.rest;
                        }
                        // Выводим на экран
                        edTaste.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edTaste.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_STORAGE_POTENTIAL : // Потенциал хранения
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edStoragePotential.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edStoragePotential.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_SERVING_TEMPERATURE : // Температура подачи
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edServingTemperature.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edServingTemperature.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_GASTRONOMIC_PARTNERS : // Гастропары (гастрономические компаньоны)
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edGastronomicPartners.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edGastronomicPartners.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_TASTINGPURCHASE : // Место покупки (где куплено)
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Выводим на экран
                        edTastingPurchase.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edTastingPurchase.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                case GlobalKeyWord.KEY_WORD_NOTES : // Заметки/Впечатления
                    // Если остаток строки не пуст
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Делаем первую букву заглавной
                        firstResultKeyWord.rest = firstResultKeyWord.rest.
                                substring(0,1).toUpperCase()+
                                firstResultKeyWord.rest.substring(1);
                        // Проверяем, есть ли уже что-то в поле edNotes
                        textOld = edNotes.getText().toString();
                        if (!textOld.equals("")) {
                            // Если уже есть текс, то добавляем новую строку и вставляем текст
                            firstResultKeyWord.rest = textOld + "\n" + firstResultKeyWord.rest;
                        }
                        // Выводим на экран
                        edNotes.setText(firstResultKeyWord.rest);
                        // Устанавливаем на фокус на поле
                        edNotes.requestFocus();
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                    //TODO: Как передать фокус на оценку? чтобы диктовке оценки сразу ее видеть
                case GlobalKeyWord.KEY_WORD_RATING : // Моя оценка (пятибальная шкала) – набор звездочек
                    // Если остаток строки не пуст

                    // Пытаемся выделить ключевое слово (не в виде цифры, а слова)
                    if (!firstResultKeyWord.rest.equals("")) {
                        // Берем второе ключевое слово (если оно есть)
                        secondResultKeyWord = GlobalKeyWord.thereIsKeyWord(firstResultKeyWord.rest);
                        //Анализ ключевых слов
                        switch (secondResultKeyWord.keyWord) {
                            case GlobalKeyWord.KEY_WORD_ZERO: //Значения оценки - ноль
                                // Звездочка 0
                                wine.setRating(0);
                                // Рисуем на экране оценку
                                drawingRating(wine.getRating());
                                break;
                            case GlobalKeyWord.KEY_WORD_ONE: //Значения оценки - один
                                // Звездочка 1
                                wine.setRating(1);
                                // Рисуем на экране оценку
                                drawingRating(wine.getRating());
                                break;
                            case GlobalKeyWord.KEY_WORD_TWO: //Значения оценки - два
                                // Звездочка 2
                                wine.setRating(2);
                                // Рисуем на экране оценку
                                drawingRating(wine.getRating());
                                break;
                            case GlobalKeyWord.KEY_WORD_THREE: //Значения оценки - три
                                // Звездочка 3
                                wine.setRating(3);
                                // Рисуем на экране оценку
                                drawingRating(wine.getRating());
                                break;
                            case GlobalKeyWord.KEY_WORD_FOUR: //Значения оценки - четыре
                                // Звездочка 4
                                wine.setRating(4);
                                // Рисуем на экране оценку
                                drawingRating(wine.getRating());
                                break;
                            case GlobalKeyWord.KEY_WORD_FIVE: //Значения оценки - пять
                                // Звездочка 5
                                wine.setRating(5);
                                // Рисуем на экране оценку
                                drawingRating(wine.getRating());
                                break;
                            default:
                                // Ключевое ключевое слово (не в виде цифры, а слова) не обнаружено
                                //=============================
                                // Остаток строки преобразум в int (отбрасывая нечисловые символы)
                                int i = 0;
                                String temp = firstResultKeyWord.rest.replaceAll("[\\D]", "");
                                try {
                                    i = Integer.parseInt(temp);
                                } catch (NumberFormatException e) {
                                    Snackbar.make(coordinator_layout,
                                            R.string.rating_not_recognized + " \"" +
                                                    firstResultKeyWord.rest + "\"",
                                            Snackbar.LENGTH_LONG).show();
                                }

                                switch (i) {
                                    case 0: //Значения оценки - ноль
                                        // Звездочка 0
                                        wine.setRating(0);
                                        // Рисуем на экране оценку
                                        drawingRating(wine.getRating());
                                        break;
                                    case 1: //Значения оценки - один
                                        // Звездочка 1
                                        wine.setRating(1);
                                        // Рисуем на экране оценку
                                        drawingRating(wine.getRating());
                                        break;
                                    case 2: //Значения оценки - два
                                        // Звездочка 2
                                        wine.setRating(2);
                                        // Рисуем на экране оценку
                                        drawingRating(wine.getRating());
                                        break;
                                    case 3: //Значения оценки - три
                                        // Звездочка 3
                                        wine.setRating(3);
                                        // Рисуем на экране оценку
                                        drawingRating(wine.getRating());
                                        break;
                                    case 4: //Значения оценки - четыре
                                        // Звездочка 4
                                        wine.setRating(4);
                                        // Рисуем на экране оценку
                                        drawingRating(wine.getRating());
                                        break;
                                    case 5: //Значения оценки - пять
                                        // Звездочка 5
                                        wine.setRating(5);
                                        // Рисуем на экране оценку
                                        drawingRating(wine.getRating());
                                        break;
                                    default:
                                        Snackbar.make(coordinator_layout,
                                                R.string.rating_not_recognized + " \"" +
                                                        firstResultKeyWord.rest + "\"",
                                                Snackbar.LENGTH_LONG).show();
                                }
                                //=============================
                        }
                    } else {
                        showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
                    }
                    break;

                default:
                    showSnackbar_JUST_A_KEYWORD(firstResultKeyWord.keyWord);
            }
        }
    }

    // ============================================================================
    public void showSnackbar_JUST_A_KEYWORD(String myString) {
        Snackbar.make(coordinator_layout,
                R.string.just_key_word + " \"" +
                        myString + "\"",
                Snackbar.LENGTH_LONG).show();
    }

    //============================================================================
    // Вызов и обработка выбора даты
    //============================================================================
    public void onClickDatePickerFragment(View view) {
        DialogFragment newFragment = new DatePickerFragment(
                this,
                mDateWineYear,
                mDateWineMonth,
                mDateWineDay);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    //============================================================================
    // интерфейс onReturnDateListener для передачи выбранной в диалоге даты
    //============================================================================
    @Override
    public void returnDate(DatePickerFragment.DateDDMMYYYY dateDDMMYYYY) {
        // Запоминаем выбранную дату
        mDateWineDay = dateDDMMYYYY.mDay;
        mDateWineMonth = dateDDMMYYYY.mMonth;
        mDateWineYear = dateDDMMYYYY.mYear;
        // Стоим строку даты в формате ДД.ММ.ГГГГ
        String mGetDateWine = Utilities.getDateWine(
                1,
                mDateWineDay,
                mDateWineMonth,
                mDateWineYear);
        // Выводим дату на экран
        edDateWine.setText(mGetDateWine);
    }

    //============================================================================
    // Скроллирование экрана, чтобы поле view ввода было над клавиатурой
    //============================================================================
    public void moveUpView(EditText view) {

        // Вычисляет координаты этого представления в его окне.
        // Аргумент должен быть массивом из двух целых чисел.
        // После возврата из метода массив содержит координаты x и y в указанном порядке.
        int[] outLocation1 = new int[2];
        view.getLocationInWindow(outLocation1);

        // Определяем размер экрана
        int outScreen = Resources.getSystem().getDisplayMetrics().heightPixels;

        // Определяем верхнюю позицию клавиатуры полюс допуск на плавающие кнопки
        int posKeyboard = outScreen - (global_KeyboardHeight + global_HeightOfFloatingButtons);

        // Определяем, на сколько нужно поднять поле ввода
        int delta = outLocation1[1] - posKeyboard;

        // Поднимаем поле ввода
        if (delta > 0) {
            moveUp(delta);
        }

    }

    //============================================================================
    // Скроллирование экрана, на delta
    //============================================================================
    public void moveUp(int delta) {

        Log.d(TAG,"EditActivity: centerView");

        int targetScroll = mNestedScrollView.getScrollY() + delta;
        mNestedScrollView.scrollTo(0,targetScroll);
        mNestedScrollView.setSmoothScrollingEnabled(true);
        ViewCompat.setNestedScrollingEnabled(mNestedScrollView, false);
        final int currentScrollY = mNestedScrollView.getScrollY();
        ViewCompat.postOnAnimationDelayed(mNestedScrollView, new Runnable() {
            int currentY = currentScrollY;
            @Override
            public void run() {
                if(currentScrollY == mNestedScrollView.getScrollY()){
                    ViewCompat.setNestedScrollingEnabled(mNestedScrollView, true);
                    return;
                }
                currentY = mNestedScrollView.getScrollY();
                ViewCompat.postOnAnimation(mNestedScrollView, this);
            }
        }, 10);

        Log.d(TAG,"EditActivity: centerView Конец");

    }

    //============================================================================
    // Действие по нажатию стрелочки "Назад"
    //============================================================================
    @Override
    public void onBackPressed () {

        // Берем экранные переменные и размещаем их в wine для последующего сохранения
        getScreenVariablesToWine();

        if (wineHasBeenChanged())     // Если вино было изменено
        {
            // Используйте класс Builder для удобного построения диалогов
            AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogTheme);
            builder
                    .setTitle(R.string.exit_editing_wine)    // Заголовок диалога
                    // Позитивная кнопка - Сохранить и выйти
                    .setPositiveButton(R.string.dialog_save_and_exit, new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        public void onClick(DialogInterface dialog, int id) {
                            // Сохранить и выйти
                            saveAndExitWineCard();
                        }
                    })
                    // Отрицательная кнопка - Выйти без сохранения
                    .setNegativeButton(R.string.dialog_exit_without_saving, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Выйти без сохранения
                            finish();
                        }
                    })
                    // Нейтральная кнопка - Остаться
                    .setNeutralButton(R.string.dialog_stay_here, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Остаться
                        }
                    });
            // Создаем диалог
            AlertDialog alert = builder.create();
            // Показываем диалог
            alert.show();
        }
        else                        // Если вино НЕ было изменено
        {
            finish();
        }
    }

    //============================================================================
    // Сохранить и выйти из карточки вина
    //============================================================================
    private void saveAndExitWineCard() {

        // проверям наименование вина
        String mNameWine = edNameWine.getText().toString();

        // Если наименование вина пусто
        if (mNameWine.equals("")) {
            Snackbar.make(coordinator_layout,
                    R.string.the_name_of_the_wine_is_not_filled,
                    Snackbar.LENGTH_LONG).show();
        }
        // Если наименование вина НЕ пусто
        else {
            // Вставляем или обновляем вино во втором потоке
            AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                @Override
                public void run() {
                    MainActivity.myDbManager.insertOrUpdateWine_Executer(
                            isEditState,
                            wine,
                            photoAdapter,
                            EditActivity.this);
                }
            });
            // ВАЖНО - продолжение метода после отработки второго потока
            // в интерфейсе onInsertOrUpdateWine

            // TODO: Проверить, верно ли, что - Обратный вызов происходит медленно.
            //  Возможно Распараллеливание неправильно


        }
    }

    //==============================================================================
    // Интерфейс - действия, когда мы обновили или вставили вино
    // Обновление адаптера в ОСНОВНОМ потоке
    //==============================================================================
    @Override
    public void onInsertOrUpdateWine() {

        Snackbar.make(coordinator_layout,
                R.string.saved,
                Snackbar.LENGTH_LONG).show();
        finish();
    }

    //==============================================================================
    // Запоминаем начальное состояние вина
    //==============================================================================
    private void rememberInitialState() {

        oldWine = new Wine();

        if (wine.getNameWine() == null) oldWine.setNameWine("");
        else oldWine.setNameWine(wine.getNameWine());

        oldWine.setDateWineDay(wine.getDateWineDay());
        oldWine.setDateWineMonth(wine.getDateWineMonth());
        oldWine.setDateWineYear(wine.getDateWineYear());

        if (wine.getTastingPlace() == null) oldWine.setTastingPlace("");
        else oldWine.setTastingPlace(wine.getTastingPlace());

        if (wine.getCountry() == null) oldWine.setCountry("");
        else oldWine.setCountry(wine.getCountry());

        if (wine.getRegion() == null) oldWine.setRegion("");
        else oldWine.setRegion(wine.getRegion());

        oldWine.setSort(wine.getSort());

        if (wine.getGrapeSort() == null) oldWine.setGrapeSort("");
        else oldWine.setGrapeSort(wine.getGrapeSort());

        oldWine.setYear(wine.getYear());

        oldWine.setStrength(wine.getStrength());

        oldWine.setPrice(wine.getPrice());

        if (wine.getProducer() == null) oldWine.setProducer("");
        else oldWine.setProducer(wine.getProducer());

        if (wine.getDistributor() == null) oldWine.setDistributor("");
        else oldWine.setDistributor(wine.getDistributor());

        if (wine.getAppearance() == null) oldWine.setAppearance("");
        else oldWine.setAppearance(wine.getAppearance());

        if (wine.getAroma() == null) oldWine.setAroma("");
        else oldWine.setAroma(wine.getAroma());

        if (wine.getTaste() == null) oldWine.setTaste("");
        else oldWine.setTaste(wine.getTaste());

        if (wine.getStoragePotential() == null) oldWine.setStoragePotential("");
        else oldWine.setStoragePotential(wine.getStoragePotential());

        if (wine.getServingTemperature() == null) oldWine.setServingTemperature("");
        else oldWine.setServingTemperature(wine.getServingTemperature());

        if (wine.getGastronomicPartners() == null) oldWine.setGastronomicPartners("");
        else oldWine.setGastronomicPartners(wine.getGastronomicPartners());

        if (wine.getTastingPurchase() == null) oldWine.setTastingPurchase("");
        else oldWine.setTastingPurchase(wine.getTastingPurchase());

        if (wine.getNotes() == null) oldWine.setNotes("");
        else oldWine.setNotes(wine.getNotes());

        oldWine.setRating(wine.getRating());
    }

    //==============================================================================
    // Сравнение старой и новой карточки
    // Возвращает:
    //      true - если карточка изменилась
    //      false - если карточка НЕ изменилась
    //==============================================================================
    private boolean wineHasBeenChanged() {

        if (photoAdapter.getPhotoArrayHasBeenChanged()) return true;

        if (!wine.getNameWine().equals(oldWine.getNameWine())) return true;
        if (!(wine.getDateWineDay() == oldWine.getDateWineDay())) return true;
        if (!(wine.getDateWineMonth() == oldWine.getDateWineMonth())) return true;
        if (!(wine.getDateWineYear() == oldWine.getDateWineYear())) return true;
        if (!wine.getTastingPlace().equals(oldWine.getTastingPlace())) return true;
        if (!wine.getCountry().equals(oldWine.getCountry())) return true;
        if (!wine.getRegion().equals(oldWine.getRegion())) return true;
        if (!(wine.getSort() == oldWine.getSort())) return true;
        if (!wine.getGrapeSort().equals(oldWine.getGrapeSort())) return true;
        if (!(wine.getYear() == oldWine.getYear())) return true;
        if (!(wine.getStrength() == oldWine.getStrength())) return true;
        if (!(wine.getPrice() == oldWine.getPrice())) return true;
        if (!wine.getProducer().equals(oldWine.getProducer())) return true;
        if (!wine.getDistributor().equals(oldWine.getDistributor())) return true;
        if (!wine.getAppearance().equals(oldWine.getAppearance())) return true;
        if (!wine.getAroma().equals(oldWine.getAroma())) return true;
        if (!wine.getTaste().equals(oldWine.getTaste())) return true;
        if (!wine.getStoragePotential().equals(oldWine.getStoragePotential())) return true;
        if (!wine.getServingTemperature().equals(oldWine.getServingTemperature())) return true;
        if (!wine.getGastronomicPartners().equals(oldWine.getGastronomicPartners())) return true;
        if (!wine.getTastingPurchase().equals(oldWine.getTastingPurchase())) return true;
        if (!wine.getNotes().equals(oldWine.getNotes())) return true;
        if (!(wine.getRating() == oldWine.getRating())) return true;

        return false;
    }

    //===================================================================================
    @Override
    public void onResume() {
        super.onResume();

        // Берем настройки из Preferences
        getMyPreferences();

        // Обнуляем высоту клавиатуры
        global_KeyboardHeight = 0;

        // Установите наблюдателя высоты клавиатуры для этого провайдера.
        // Наблюдатель будет уведомлен об изменении высоты клавиатуры.
        // Например, когда клавиатура открыта или закрыта.
        // Сейчас onResume() - устанавливаем наблюдателя в текущую активность
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    // ============================================================================
    // Берем настройки из Preferences
    // ============================================================================
    private void getMyPreferences() {

        //TODO: Это копия метода из MainActivity. Устранить дублирование

        // Получает экземпляр SharedPreferences, указывающий на файл по умолчанию,
        // который используется платформой предпочтений в данном контексте.
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Берем Настройки поворота фото и помещаем их в глобальную переменную
        global_rotation = prefs.getString("rotation", "rotation_no");

        // Берем Настройки темы и помещаем их в глобальную переменную
        global_theme = prefs.getString("theme", "theme_system");
        switch (global_theme) {
            case "theme_light":     // Устанавливает режим - Светлая
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "theme_dark":      // Устанавливает режим - Темная
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "theme_system":    // Устанавливает режим - Как в системе
                // Если версия Андроид >= 9.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // Устанавливает режим - как в системе
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                break;
            case "theme_battery":   // Устанавливает режим - Тема экономии заряда батареи
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
            default:                // Тема по умолчанию - Светлая
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Берем Настройки сохранения винных полок и помещаем их в глобальную переменную
        global_wine_shelf_save_last = prefs.getBoolean("wine_shelf_save_last", true);
        // Если нужно сохранять последнюю винную полку, то в настойки кладем текущую винную полку
        if (global_wine_shelf_save_last) {
            // Создаем едитор для записи настроек
            SharedPreferences.Editor editor = prefs.edit();
            // Помещаем в едитор текущую винную полку
            editor.putLong(wine_shelf_save_last_value, wine.getShelfId());
            // Сохраняем изменения
            editor.apply();
        }

        // Берем Настройки голосового ввода и помещаем их в глобальную переменную
        global_enable_voice_input = prefs.getBoolean("enable_voice_input", true);
        // Если нужно включить голосовой ввод
        if (global_enable_voice_input) {
            fabMic.setVisibility(View.VISIBLE);
        }
        // Если нужно выключить голосовой ввод
        else {
            fabMic.setVisibility(View.GONE);
        }

        // Берем Настройки внешнего вида - вид оценки: бокалы, звездочки и помещаем их в глобальную переменную
        global_type_of_assessmen = prefs.getString("type_of_assessmen", "type_of_assessmen_wineglass");
        drawingRating(wine.getRating());

    }

    // ============================================================================
    // Обновляем сообщения на главной странице для фото:
    // - Управляет видимостью/невидимостью "Создайте фото или выберите из Галереи"
    // ============================================================================
    private void updateMessagesPhoto() {

        // Если есть фотографии
        if (photoAdapter.getItemCount() > 0) {
            // Скрываем сообщение "Создайте фото или выберите из Галереи"
            tvMessagePhoto.setVisibility(View.INVISIBLE);
        }
        // Если нет фотографий
        else {
            // Выводим сообщение "Создайте фото или выберите из Галереи"
            tvMessagePhoto.setVisibility(View.VISIBLE);
        }
    }

    //====================================================================================
    // Была удалена фотография
    // В DeletePhotoInterfaceListener метод deletePhotoInterface
    //====================================================================================
    @Override
    public void deletePhotoInterface() {

        // Обновляем сообщения на главной странице для фото:
        // - Управляет видимостью/невидимостью "Создайте фото или выберите из Галереи"
        updateMessagesPhoto();
    }


    //====================================================================================
    // ЭТО РАЗДЕЛ ДЛЯ ПОКАЗА РЕКЛАМЫ
    //====================================================================================

    /** ====================================================================================
     * initAds - Инициализируем рекламу на карточке вина
     * ====================================================================================
     */
    private void initAds() {

        // Инициализируем SDK мобильной рекламы.
        // MobileAds - Класс содержит логику, которая применяется к Google Mobile Ads SDK в целом.
        // На данный момент для инициализации используются единственные в нем методы

        //TODO: может, лучше вызывать инициализацию контексной рекламы поближе к началу
        // инициализация вызывается какждый раз, когда открывает карточку

        // Инициализирует Google Mobile Ads SDK.
        // Вызовите этот метод как можно раньше после запуска приложения,
        // чтобы уменьшить задержку при первом запросе объявления в сеансе.

        // Если этот метод не вызывается, первый запрос объявления автоматически
        // инициализирует Google Mobile Ads SDK.
        // Параметры:
        //      - context Контекст действия, в котором выполняется SDK.
        //      - слушатель Обратный вызов, который будет вызываться после завершения инициализации.
                MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    // InitializationStatus - Статус инициализации SDK
                    @Override
                    public void onInitializationComplete(InitializationStatus status) {
                    }
                });

/*        // Находим экранные объекты
        refresh = findViewById(R.id.btn_refresh);
        requestNativeAds = findViewById(R.id.cb_nativeads);
        requestCustomTemplateAds = findViewById(R.id.cb_customtemplate);
        startVideoAdsMuted = findViewById(R.id.cb_start_muted);
        videoStatus = findViewById(R.id.tv_video_status);*/

/*        // На кнопку "Обновить" вешаем реакицию на нажатие
        refresh.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Создает запрос нового нативного объявления
                        refreshAd(requestNativeAds.isChecked(), requestCustomTemplateAds.isChecked());
                    }
                });*/

        // Создает запрос нового нативного объявления
        refreshAd(true, false);

    }

    /** ===============================================================================
     * refreshAd - Создает запрос НОВОГО нативного объявления на основе логических параметров и
     * вызывает соответствующий метод "заполнить", когда он успешно возвращен.
     *
     * @param requestNativeAds         указывает, следует ли запрашивать унифицированные нативные объявления
     * @param requestCustomTemplateAds указывает, следует ли запрашивать объявления на основе пользовательских шаблонов
     * ===============================================================================
     */
    private void refreshAd(boolean requestNativeAds, boolean requestCustomTemplateAds) {
        // Если не нужно запрашивать унифицированные нативные объявления И
        // не нужно запрашивать объявления на основе пользовательских шаблонов
        // то выходим
        if (!requestNativeAds && !requestCustomTemplateAds) {
            // Выводим сообщение и выходим
            Toast.makeText(
//              this, "At least one ad format must be checked to request an ad.", Toast.LENGTH_SHORT)
                    this, R.string.to_request_ad_you_must_check_at_least_one_ad_format, Toast.LENGTH_SHORT)
                    .show();
            return;
        }


        // Для БОЕВОЙ реламы влючить это
//        String adInitID = AD_MANAGER_AD_UNIT_ID;

        // Для ТЕСТОВОЙ рекламы влючить это
        String adInitID = "";
        // Если требуется запускать видео рекламу (запускаем Native Advanced Video)
        if (launchVideoAd) {
            // Меняем флаг запуска рекламы для следующего раза
            launchVideoAd = false;
            // Готовимся запускать видео рекламу
            adInitID = AD_MANAGER_AD_UNIT_ID_VIDEO;
        }
        // Если требуется запускать обычную рекламу (запускаем Native Advanced)
        else {
            // Меняем флаг запуска рекламы для следующего раза
            launchVideoAd = true;
            // Готовимся запускать обычную рекламу
            adInitID = AD_MANAGER_AD_UNIT_ID;
        }

        // AdLoader - Объект для запроса рекламы
        // Создает новый AdLoader.Builder для создания AdLoader.
        // Параметры:
        //  - Контекст
        //  - Идентификатор рекламного блока
        //  (в данном случаем AD_MANAGER_AD_UNIT_ID или AD_MANAGER_AD_UNIT_ID_VIDEO)
        AdLoader.Builder builder = new AdLoader.Builder(this, adInitID);

        // Если следует запрашивать унифицированные нативные объявления
        if (requestNativeAds) {
            // forNativeAd - Регистрирует слушателя для обработки загрузки NativeAd
            builder.forNativeAd(
                    new NativeAd.OnNativeAdLoadedListener() {
                        // Слушатель для обработки загрузки NativeAd
                        @Override
                        public void onNativeAdLoaded(NativeAd nativeAd) {
                            // Если этот обратный вызов происходит после уничтожения активности,
                            // вы должны вызвать destroy и return, иначе вы можете получить утечку памяти.
                            boolean isDestroyed = false;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                // isDestroyed() - Метод Activity.
                                // Возвращает истину, если последний вызов onDestroy () был
                                // выполнен для Activity, поэтому этот экземпляр теперь мертв
                                isDestroyed = isDestroyed();
                            }
                            // Если Активити уничтожена (isDestroyed) ИЛИ
                            // для Активити был вызван finish () ИЛИ
                            // Активити находится в процессе уничтожения, чтобы его можно было
                            // воссоздать с новой конфигурацией
                            if (isDestroyed || isFinishing() || isChangingConfigurations()) {
                                // Уничтожает рекламный объект.
                                // Никакие другие методы не должны
                                // вызываться для объекта объявления после того, как было
                                // вызвано уничтожение.
                                nativeAd.destroy();
                                return;
                            }
                            // Вы должны вызвать уничтожение старых объявлений,
                            // когда закончите с ними, иначе у вас будет утечка памяти.
                            // Если СТАРАЯ реклама не равна null
                            if (EditActivity.this.nativeAd != null) {
                                // Уничножаем СТАРУЮ рекламу
                                EditActivity.this.nativeAd.destroy();
                            }
                            // Присваиваем новую рекламу
                            EditActivity.this.nativeAd = nativeAd;
                            // Находим в activity_main.xml место для рекламы FrameLayout
                            FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
                            // NativeAdView - общедоступный конечный класс NativeAdView расширяет FrameLayout
                            // Корневой вид для NativeAd. Файлы макета XML для нативной рекламы
                            // должны использовать NativeAdView в качестве корневого элемента
                            // для своих собственных ресурсов, а динамически создаваемые
                            // представления, которые будут содержать ресурсы нативной рекламы,
                            // должны использовать экземпляр этого класса в качестве своей
                            // корневой ViewGroup.
                            // Раздуваем рекламу из макета
                            NativeAdView adView =
                                    (NativeAdView) getLayoutInflater().inflate(
                                            R.layout.ad_unified,
                                            null);

                            // Заполняет файлы макета XML для нативной рекламы данными
                            populateNativeAdView(nativeAd, adView);
                            // Удаляет все дочерние представления из ViewGroup
                            frameLayout.removeAllViews();
                            // Добавляет дочернее представление с параметрами макета по умолчанию
                            // данной ViewGroup и заданной шириной и высотой
                            frameLayout.addView(adView);
                        }
                    });
        }

        // Если следует запрашивать объявления на основе пользовательских шаблонов
        if (requestCustomTemplateAds) {
            // forCustomFormatAd - Позволяет AdLoader загружать объявления произвольного формата
            builder.forCustomFormatAd(
                    SIMPLE_TEMPLATE_ID,     // Идентификатор пользовательского формата,
                    // определенный в пользовательском интерфейсе
                    // Менеджера рекламы.
                    // В данном случае "10104090"
                    // Слушатель, вызываемый при загрузке NativeCustomFormatAd.
                    new NativeCustomFormatAd.OnCustomFormatAdLoadedListener() {
                        @Override
                        public void onCustomFormatAdLoaded(NativeCustomFormatAd ad) {
                            // Если этот обратный вызов происходит после уничтожения активности,
                            // вы должны вызвать destroy и return, иначе вы можете получить утечку памяти.
                            boolean isDestroyed = false;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                // isDestroyed() - Метод Activity.
                                // Возвращает истину, если последний вызов onDestroy () был
                                // выполнен для Activity, поэтому этот экземпляр теперь мертв
                                isDestroyed = isDestroyed();
                            }
                            // Если Активити уничтожена (isDestroyed) ИЛИ
                            // для Активити был вызван finish () ИЛИ
                            // Активити находится в процессе уничтожения, чтобы его можно было
                            // воссоздать с новой конфигурацией
                            if (isDestroyed || isFinishing() || isChangingConfigurations()) {
                                // Уничтожает рекламный объект.
                                // Никакие другие методы не должны
                                // вызываться для объекта объявления после того, как было
                                // вызвано уничтожение.
                                ad.destroy();
                                return;
                            }
                            // Вы должны вызвать destroy для старых объявлений, когда вы
                            // закончите с ними, иначе у вас будет утечка памяти.
                            if (nativeCustomFormatAd != null) {
                                nativeCustomFormatAd.destroy();
                            }
                            // Присваиваем новую рекламу
                            nativeCustomFormatAd = ad;
                            // Находим в activity_main.xml место для рекламы FrameLayout
                            FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
                            View adView =
                                    getLayoutInflater().inflate(
                                            R.layout.ad_simple_custom_template,
                                            null);
                            // Заполняет файлы макета XML для нативной рекламы данными
                            // Этот метод обрабатывает особый «простой» пользовательский формат нативной рекламы
                            populateSimpleTemplateAdView(ad, adView);
                            // Удаляет все дочерние представления из ViewGroup
                            frameLayout.removeAllViews();
                            // Добавляет дочернее представление с параметрами макета по умолчанию
                            // данной ViewGroup и заданной шириной и высотой
                            frameLayout.addView(adView);
                        }
                    }
                    ,
                    // Необязательный прослушиватель для определения пользовательской логики
                    // кликов для кликов по объявлениям. Если этот параметр задан,
                    // он переопределяет стандартное для объявления поведение перехода по клику,
                    // заключающееся в переходе по URL-адресу клика, определенному в
                    // пользовательском интерфейсе Менеджера рекламы.
                    new NativeCustomFormatAd.OnCustomClickListener() {
                        @Override
                        public void onCustomClick(NativeCustomFormatAd ad, String s) {
                            Toast.makeText(
                                    EditActivity.this,
                                    R.string.user_click_occurred_in_a_simple_template,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                    );
        }

        // VideoOptions - Публичный финальный класс VideoOptions расширяет Object
        // Параметры для управления воспроизведением видео в поддерживаемых форматах
        // рекламы (например, Native Express).
        VideoOptions videoOptions =
                new VideoOptions.
                        Builder().

//                        setStartMuted(startVideoAdsMuted.isChecked()).  // Устанавливает начальное
                        setStartMuted(true).  // Устанавливает начальное

                        // состояние отключения звука видео
                                build();

        // NativeAdOptions - общедоступный конечный класс NativeAdOptions расширяет объект
        // Используется для настройки запросов нативной рекламы.
        NativeAdOptions adOptions =
                new NativeAdOptions.
                        Builder().
                        setVideoOptions(videoOptions).
                        build();

        // public AdLoader.Builder withNativeAdOptions (параметры NativeAdOptions)
        // Устанавливает параметры нативной рекламы в конструктор загрузчика рекламы.
        // Параметры:
        //      - опции - Объект, определяющий различные параметры нативной рекламы.
        builder.withNativeAdOptions(adOptions);

        // Открытый класс AdLoader расширяет Object
        // Объект для запроса рекламы.
        AdLoader adLoader =
                builder
                        .withAdListener(    // общедоступный AdLoader.Builder withAdListener
                                // (прослушиватель AdListener)
                                // Параметры:
                                //      - listener - объект, обрабатывающий ошибки,
                                //      возникающие при получении нативной рекламы.
                                new AdListener() {
                                    @Override
                                    public void onAdFailedToLoad(LoadAdError loadAdError) {

                                        String error =
                                                String.format(
                                                        "domain: %s, code: %d, message: %s",
                                                        loadAdError.getDomain(),
                                                        loadAdError.getCode(),
                                                        loadAdError.getMessage());
                                        Toast.makeText(
                                                EditActivity.this,
                                                EditActivity.this.getString(R.string.failed_to_load_custom_ad) + error,
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                })
                        .build();

        // public void loadAd (AdManagerAdRequest adManagerAdRequest)
        // Загружает объявление.
        // Параметры:
        //      - adManagerAdRequest - запрос объявления Менеджера рекламы.
        adLoader.loadAd(new AdManagerAdRequest.Builder().build());

    }

    /** ===============================================================================
     * populateNativeAdView - Заполняет файлы макета XML для нативной рекламы данными
     * <p>
     * Заполняет {@link NativeAdView} объект с данными из заданного {@link NativeAd}.
     *
     * @param nativeAd объект, содержащий активы объявления
     * @param adView   вид, который будет заселен
     * ===============================================================================
     */
    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {

        // Находим LinearLayout для рекламы (что бы закрывать рекламу)
        llNativeAdView = adView.findViewById(R.id.llNativeAdView);

        // Установите вид мультимедиа
        // public void setMediaView (представление MediaView)
        // Устанавливает вид мультимедиа для мультимедийного содержимого.
        // Параметры:
        //      - view - MediaView для установки
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Установите другие рекламные объекты:
        // Устанавливает вид объекта заголовка
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        // Устанавливает вид для объекта body
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        // Устанавливает вид объекта призыва к действию
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        // Устанавливает вид для ресурса значка
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        // Устанавливает вид для ценового актива
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        // Устанавливает вид объекта с рейтингом в звездах
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        // Устанавливает вид для ресурса магазина
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        // Устанавливает представление для объекта рекламодателя
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // Заголовок и mediaContent гарантированно присутствуют в каждом NativeAd
        ((TextView) adView.
                getHeadlineView()).             // Возвращает представление, связанное с активом заголовка
                setText(nativeAd.getHeadline());    // Возвращает основной текстовый заголовок.
        // Для отображения этого ресурса требуются приложения
        adView.
                getMediaView().                 // Возвращает представление, связанное с медиа-контентом.
                setMediaContent(nativeAd.getMediaContent());

        // Не гарантируется, что эти активы присутствуют в каждом NativeAd,
        // поэтому важно проверить их, прежде чем пытаться их отобразить.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Этот метод сообщает SDK Google Mobile Ads, что вы завершили заполнение представления
        // нативного объявления этим нативным объявлением.
        // public void setNativeAd (объявление NativeAd)
        // Устанавливает NativeAd, которое в данный момент отображается в этом представлении.
        // Параметры:
        //      - ad - нативное объявление, которое в данный момент отображается в представлении
        adView.setNativeAd(nativeAd);

        // Получаем видеоконтроллер для рекламы.
        // Один будет всегда, даже если в объявлении нет видеоресурса
        // VideoController - Объект, обеспечивающий управление воспроизведением видеообъявлений
        VideoController vc = nativeAd.getMediaContent().getVideoController();

        // Обновляет пользовательский интерфейс, чтобы узнать, есть ли у этого объявления видеоресурс.
        if (vc.hasVideoContent()) {

            // // Создаем новый объект VideoLifecycleCallbacks и передаем его VideoController.
            // VideoController будет вызывать методы этого объекта при возникновении событий
            // в жизненном цикле видео
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Издатели должны разрешить нативным объявлениям завершить воспроизведение
                    // видео, прежде чем обновлять или заменять их другим объявлением в том
                    // же месте пользовательского интерфейса







/*                    refresh.setEnabled(true);
                    videoStatus.setText("Статус видео: воспроизведение видео завершено.");*/





                    super.onVideoEnd();
                }
            });
        } else {








/*            videoStatus.setText("Статус видео: объявление не содержит видеоресурса..");
            refresh.setEnabled(true);*/




        }
    }

    /** ===============================================================================
     * populateSimpleTemplateAdView - Заполняет файлы макета XML для нативной рекламы данными
     * <p>
     * Этот метод обрабатывает особый «простой» пользовательский формат нативной рекламы
     * <p>
     * Заполняет {@link View} объект с данными из {@link NativeCustomFormatAd}. Этот метод
     * обрабатывает особый «простой» пользовательский формат нативной рекламы.
     *
     * @param nativeCustomFormatAd объект, содержащий активы объявления
     * @param adView               вид, который будет заселен
     * ===============================================================================
     */
    private void populateSimpleTemplateAdView(
            final NativeCustomFormatAd nativeCustomFormatAd, View adView) {



        // Находим LinearLayout для рекламы (что бы закрывать рекламу)
        llAdCustom = adView.findViewById(R.id.llAdCustom);



        // Находим рекламные объекты
        TextView headline = adView.findViewById(R.id.simplecustom_headline);
        TextView caption = adView.findViewById(R.id.simplecustom_caption);

        headline.setText(nativeCustomFormatAd.getText("Заголовок"));
        caption.setText(nativeCustomFormatAd.getText("Подпись"));

        FrameLayout mediaPlaceholder = adView.findViewById(R.id.simplecustom_media_placeholder);

        // Получаем видеоконтроллер для рекламы. Один будет всегда, даже если в объявлении
        // нет видеоресурса
        VideoController vc = nativeCustomFormatAd.getVideoController();

        // Приложения могут проверять свойство hasVideoContent VideoController, чтобы определить,
        // есть ли у NativeCustomFormatAd видеоресурс
        // Если есть видеоконтент
        if (vc.hasVideoContent()) {
            mediaPlaceholder.addView(nativeCustomFormatAd.getVideoMediaView());







/*            videoStatus.setText(
                    String.format(
                            Locale.getDefault(),
                            "Статус видео: объявление содержит %.2f:1 видеоресурс.",
                            nativeAd.getMediaContent().getAspectRatio()));*/






            // Создаем новый объект VideoLifecycleCallbacks и передаем его VideoController.
            // VideoController будет вызывать методы этого объекта при возникновении событий
            // в жизненном цикле видео.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                public void onVideoEnd() {
                    // Издатели должны разрешить нативным объявлениям завершать воспроизведение
                    // видео, прежде чем обновлять или заменять их другим объявлением в том же
                    // месте пользовательского интерфейса.






/*                    refresh.setEnabled(true);
                    videoStatus.setText("Статус видео: воспроизведение видео завершено.");*/





                    super.onVideoEnd();
                }
            });
        }
        // Если НЕТ видеоконтента
        else {
            ImageView mainImage = new ImageView(this);
            mainImage.setAdjustViewBounds(true);
            // nativeCustomFormatAd.getImage - Возвращает ресурс изображения.
            // Параметры:
            //      - assetName - имя извлекаемого актива.
            //      - Возвраты - значение актива с именем актива или null, если такого имени актива нет.
            mainImage.setImageDrawable(nativeCustomFormatAd.getImage("MainImage").getDrawable());

            mainImage.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // public abstract void performClick (String assetName)
                            // Вызывается, когда пользователь нажимает на объявление.
                            // Параметры:
                            //  - assetName - название актива, по которому был выполнен клик.
                            nativeCustomFormatAd.performClick("MainImage");
                        }
                    });
            mediaPlaceholder.addView(mainImage);







/*            refresh.setEnabled(true);
            videoStatus.setText("Статус видео: объявление не содержит видеоресурса..");*/



            //TODO: Навести порядок с рекламными методами

        }
    }

    //===============================================================================
    // Действия на кнопку закрытия наитивной рекламы
    //===============================================================================
    public void onCloseAd(View view) {

        if (llNativeAdView != null) {
            llNativeAdView.setVisibility(View.GONE);
        }

    }

    //===============================================================================
    // Действия на кноку закрытия кастомной рекламы
    //===============================================================================
    public void onCloseAdCustom(View view) {

        if (llAdCustom != null) {
            llAdCustom.setVisibility(View.GONE);
        }

    }

    //===============================================================================
    // onDestroy()
    //===============================================================================
    @Override
    protected void onDestroy() {

        // Уничтожает рекламный объект.
        if (nativeAd != null) {
            // Уничтожает рекламный объект.
            // Никакие другие методы не должны вызываться для объекта объявления
            // после вызова destroy ().
            nativeAd.destroy();
        }
        if (nativeCustomFormatAd != null) {
            // Уничтожает рекламный объект.
            // Никакие другие методы не должны вызываться для объекта объявления
            // после вызова destroy ().
            nativeCustomFormatAd.destroy();
        }

        // Закройте поставщик высоты клавиатуры, этот поставщик больше не будет использоваться.
        keyboardHeightProvider.close();

        super.onDestroy();
    }


    //===========================================================================
    // onPause()
    //===========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        // Установите наблюдателя высоты клавиатуры для этого провайдера.
        // Наблюдатель будет уведомлен об изменении высоты клавиатуры.
        // Например, когда клавиатура открыта или закрыта.
        // Сейчас onPause() - устанавливаем наблюдателя в null
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }

    //===========================================================================
    // Вызывается при изменении высоты клавиатуры, 0 означает, что клавиатура закрыта,
    // > = 1 означает, что клавиатура открыта.
    //===========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {

        // TODO: Это костыль. Это костыль для высоты клавиатуры
        // При увеличении высоты клавиатуры белее, чем на 50%,
        // константу global_KeyboardHeight не увеличиваем.

        // Если высота клавиатуры больше, чем запомненная в глобальных константах
        if (height > global_KeyboardHeight) {
            // Если запомненное значение == 0 (это при первом присвоении)
            if (global_KeyboardHeight == 0) {
                // При первом присвоении проверяем высоту. Если высота больше 900, то не присваиваем
                if (height < 900) {
                    // Запоминаем новую высоту в в глобальных константах
                    global_KeyboardHeight = height;


                }
            }
            else {
                // При увеличении высоты клавиатуры белее, чем на maxIncrease,
                // константу global_KeyboardHeight не увеличиваем.
                float increase = 0;
                try {
                    increase = ((float) height)/global_KeyboardHeight;
                    if (increase <= maxIncrease) {
                        // Запоминаем новую высоту в в глобальных константах
                        global_KeyboardHeight = height;
                    }
                } catch (Exception e) {
                    Toast.makeText(this, R.string.division_error, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }

    // endregion

}