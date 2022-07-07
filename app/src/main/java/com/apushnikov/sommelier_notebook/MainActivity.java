package com.apushnikov.sommelier_notebook;

import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.CHOOSE_FROM_PICTURES_INDEX;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.CHOOSE_FROM_PICTURES_POSITION_FROM;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.CHOOSE_FROM_PICTURES_WINE_SHELF_FROM;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PICK_IMAGE_CODE_WINE_SHELF;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.REQUEST_CHOOSE_FROM_PICTURES;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.REQUEST_LARGE_PHOTO_WINE_SHELF;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.globalPhotoAbsolutePathShelf;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.globalPosition;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.globalWineShelf;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_enable_voice_input;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_rotation;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_theme;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_type_of_assessmen;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_wine_shelf_save_last;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.isTheProfessionalVersion;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.restrictionsAmountWineShelfs;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.restrictionsAmountWines;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.theSecondThreadIsRunning;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.wine_shelf_save_last_value;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apushnikov.sommelier_notebook.Global.GlobalConstants;
import com.apushnikov.sommelier_notebook.ui.aboutTheApplication.AboutTheApplication;
//import com.apushnikov.sommelier_notebook.databinding.ActivityMainBinding;
import com.apushnikov.sommelier_notebook.billing.makePurchase.MakePurchase;
import com.apushnikov.sommelier_notebook.db.AppDatabase;
import com.apushnikov.sommelier_notebook.db.AppExecuter;
import com.apushnikov.sommelier_notebook.db.MyDbManager;
import com.apushnikov.sommelier_notebook.db.OnDataInsertedWineShelf;
import com.apushnikov.sommelier_notebook.db.OnDataReceivedListWine;
import com.apushnikov.sommelier_notebook.db.OnDataReceivedListWineShelf;
import com.apushnikov.sommelier_notebook.db.OnDataUpdatedWineShelfPicture;
import com.apushnikov.sommelier_notebook.db.PhotoDao;
import com.apushnikov.sommelier_notebook.db.Wine;
import com.apushnikov.sommelier_notebook.db.WineDao;
import com.apushnikov.sommelier_notebook.db.WineShelf;
import com.apushnikov.sommelier_notebook.db.WineShelfDao;
import com.apushnikov.sommelier_notebook.log_file.LogFile;
import com.apushnikov.sommelier_notebook.myApplication.MyApplication;
import com.apushnikov.sommelier_notebook.ui.adapterWineList.WineAdapter;
import com.apushnikov.sommelier_notebook.ui.adapterWineShelfList.WineShelfAdapter;
import com.apushnikov.sommelier_notebook.ui.faq.Faq;
import com.apushnikov.sommelier_notebook.ui.screenRateAndReview.ScreenRateAndReview;
import com.apushnikov.sommelier_notebook.ui.splashScreen.SplashScreen;
import com.apushnikov.sommelier_notebook.ui.whatsNew.WhatsNew;
import com.apushnikov.sommelier_notebook.utilities.NewWineShelfDialogFragment;
import com.apushnikov.sommelier_notebook.utilities.SettingsActivity;
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
import java.util.Date;
import java.util.List;

//import com.apushnikov.sommelier_notebook.db.App;

public class MainActivity extends AppCompatActivity implements
        WineShelfAdapter.onClickWineShelfListener,
        WineAdapter.onDeleteWineInterfaceListener,
        NewWineShelfDialogFragment.InputDialogListener,
        OnDataReceivedListWine,
        OnDataReceivedListWineShelf,
        OnDataInsertedWineShelf,
        OnDataUpdatedWineShelfPicture {


    //===========================================================================================
    // region: Поля и константы в этом модуле
    //===========================================================================================

    private static final String TAG = "myLogs";     //Для логов


    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    private LogFile logFile;


    /**mainActivityViewModel - Это используется для любой бизнес-логики, а также для
     * вывода LiveData из BillingRepository*/
    private MainActivityViewModel mainActivityViewModel;



    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ТУТ ПЫТАЕМСЯ ОБОЙТИСЬ БЕЗ биндинг
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    /**activityMainBinding - биндинг, привязка (Binding) к activity_main.xml*/


    // Для системной кнопки назад
    private long backPressedTime;
    private Toast backToast;

    // экземпляр SharedPreferences
    SharedPreferences prefs = null;

    // Основной контейнер окна activity_edit.xml
    private CoordinatorLayout coordinator_layoutMain;
    // Геттер для coordinator_layoutMain
    public CoordinatorLayout getCoordinator_layoutMain() {
        return coordinator_layoutMain;
    }

    // Сообщения экрана на главном экране
    private TextView tvMessageShelfWine;
    private TextView tvMessageWine;

    // Плавающая кнопка - "Новое вино"
    private FloatingActionButton fabNewWine;

    //====================================================================
    // Переменная, адаптер, массив - для экрана для RecyclerView для показа винных полок
    //====================================================================
    // Горизонтальный RecyclerView для показа винных полок
    private RecyclerView recyclerViewListWineShelf;
    // Адаптер для горизонтального RecyclerView для показа винных полок
    private WineShelfAdapter wineShelfAdapter;

    // Текущая винная полка
    //      Если значение > 0, то это настоящая винная полка
    //      Если значение -1, то винных полок нет
    private long currentShelfId;

    //====================================================================
    // Переменная, адаптер, массив - для экрана для RecyclerView для показа вина
    //====================================================================
    // Вертикальный RecyclerView для показа вина
    private RecyclerView recyclerViewListWine;
    // Адаптер для вертикального RecyclerView для показа вина
    private WineAdapter wineAdapter;

/*
    // ЗАПРОСИТЕ РАЗРЕШЕНИЕ - код возврата
    private static final int REQUEST_ACCESS_PERMISSION = 200;
    // Результат запроса разрешения на RECORD_AUDIO. По умолчанию ЛОЖЬ
    private boolean permissionToRecordAccepted = false;
    //    private String [] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private String [] permissions1 = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private String [] permissions2 = {Manifest.permission.ACCESS_MEDIA_LOCATION};

*/

    // TODO: было так - private MyDbManager myDbManager;
//    public static MyDbManager myDbManager;
    public static MyDbManager myDbManager = new MyDbManager();

    private AppDatabase db;

    // TODO: Перенести использование Dao в отдельный класс
    // Переменные public, потому что Dao мы используем в других классах
    public static WineDao wineDao;
    //    public WineDao wineDao;
    public static WineShelfDao wineShelfDao;
    //    public WineShelfDao wineShelfDao;
    public static PhotoDao photoDao;
//    public PhotoDao photoDao;

    //TODO: Использование mPhotoFile и mCurrentPhotoPath - это временное решение

    // Фото-файл (для нового фото)
    private File mPhotoFile;
    // Абсолютный путь к фото-файлу
    private String mCurrentPhotoPath;

    // endregion

    //===========================================================================================
    // region: Методы
    //===========================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
        this.logFile = ((MyApplication) getApplication()).appContainer.
                repository.logFile;

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ТУТ ПЫТАЕМСЯ ОБОЙТИСЬ БЕЗ биндинга
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
/*        logFile.writeLogFile("   MainActivity: onCreate: activityMainBinding = DataBindingUtil.setContentView");

        // setContentView - Установите представление содержимого Activity в заданный макет и
        // верните связанную привязку. Данный ресурс макета не должен быть макетом слияния.
        // Параметры:
        //       - activity -    Действие, представление содержимого которого должно измениться.
        //       - layoutId -    идентификатор ресурса макета, который будет увеличен, привязан и
        //                       установлен в качестве содержимого Activity.
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);*/

        // Создайте свою Activity ViewModel, которая существует для обработки глобальных
        // сообщений Snackbar
        MainActivityViewModel.MainActivityViewModelFactory mainActivityViewModelFactory = new
                MainActivityViewModel.MainActivityViewModelFactory(
                ((MyApplication) getApplication()).appContainer.repository,
                logFile);

        // ViewModelProvider - Создает ViewModelProvider, который будет создавать ViewModels
        // через данную Factory и сохранять их в хранилище данного ViewModelStoreOwner.
        mainActivityViewModel = new ViewModelProvider(this, mainActivityViewModelFactory)
                .get(MainActivityViewModel.class);


        // Запрос разрешений
        askPermissionAndCaptureVideo();

        // Инициализируем:
        //      - панель инструментов Toolbar
        initToolbar();

        // Инициализируем переменные экрана
        initVariables();

        // observe - Добавляет данного наблюдателя в список наблюдателей в течение срока жизни
        // данного владельца. События отправляются в основном потоке. Если LiveData уже имеет набор
        // данных, он будет доставлен наблюдателю.
        // Наблюдатель будет получать события, только если владелец находится в состоянии
        // Lifecycle.State.STARTED или Lifecycle.State.RESUMED (активен).
        // Если владелец переходит в состояние Lifecycle.State.DESTROYED, наблюдатель будет автоматически удален.
        // Когда данные изменяются, пока владелец не активен, он не будет получать никаких обновлений.
        // Если он снова станет активным, он автоматически получит последние доступные данные.
        // LiveData сохраняет четкую ссылку на наблюдателя и владельца до тех пор, пока данный
        // LifecycleOwner не уничтожен. Когда он уничтожается, LiveData удаляет ссылки на наблюдателя и владельца.
        // Если данный владелец уже находится в состоянии Lifecycle.State.DESTROYED, LiveData игнорирует вызов.
        // Если данный владелец, кортеж наблюдателя уже есть в списке, вызов игнорируется.
        // Если наблюдатель уже находится в списке с другим владельцем, LiveData выдает исключение
        // IllegalArgumentException.
        mainActivityViewModel.getMessages().observe(this, resId -> {
            Snackbar snackbar = Snackbar.make(coordinator_layoutMain, getString(resId),
                    Snackbar.LENGTH_LONG);
            snackbar.show();
        });

        // Смотрим за наблюдаемой isPremium()
        // В зависимости от изменения, выставляем Флаг - является ли версия ПРОФЕССИОНАЛЬНОЙ
        //      - true - является
        //      - false - НЕ является
        mainActivityViewModel.isPremium().observe(this, aBoolean -> {
            if (aBoolean!=null) {
                if (aBoolean == true) {
                    // Флаг - является ли версия ПРОФЕССИОНАЛЬНОЙ
                    //      - true - является
                    //      - false - НЕ является
                    isTheProfessionalVersion = true;
                } else {
                    // Флаг - является ли версия ПРОФЕССИОНАЛЬНОЙ
                    //      - true - является
                    //      - false - НЕ является
                    isTheProfessionalVersion = false;
                }
            } else {
                // Флаг - является ли версия ПРОФЕССИОНАЛЬНОЙ
                //      - true - является
                //      - false - НЕ является
                isTheProfessionalVersion = false;
            }
        });

        // Позволяет выставлять счета для обновления покупок во время onResume
        // getLifecycle() - Возвращает жизненный цикл поставщика.
        // Переопределение этого метода больше не поддерживается, и этот метод станет окончательным
        // в будущей версии ComponentActivity. Если вы переопределите этот метод, вы должны:
        //      1. Верните экземпляр LifecycleRegistry.
        //      2. Лениво инициализируйте свой объект LifecycleRegistry при его первом вызове.
        //      Обратите внимание, что этот метод будет вызываться в конструкторе суперклассов до
        //      завершения инициализации любого поля или создания состояния объекта.
        // addObserver - Добавляет LifecycleObserver, который будет уведомлен при изменении
        // состояния LifecycleOwner.
        // Данный наблюдатель будет переведен в текущее состояние LifecycleOwner.
        // Например, если LifecycleOwner находится в состоянии Lifecycle.State.STARTED,
        // данный наблюдатель получит события Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_START.
        getLifecycle().addObserver(mainActivityViewModel.getBillingLifecycleObserver());

        // Полезный совет, чтобы избежать путаницы, когда транзакции выставления
        // счетов завершаются молча.
        // A helpful hint to prevent confusion when billing transactions silently fail
        if ( BuildConfig.BASE64_ENCODED_PUBLIC_KEY.equals("null")) {
            if ( getSupportFragmentManager()
                    .findFragmentByTag(PublicKeyNotSetDialog.DIALOG_TAG) == null ) {
                new PublicKeyNotSetDialog()
                        .show(getSupportFragmentManager(), PublicKeyNotSetDialog.DIALOG_TAG);
            }
        }

        // Инициализация базы данных
        initDb();

        // Берем значение номера винной полки:
        //      - Если в настойках установлены сохранения винных полок = true,
        //      то берем сохраненные в настройках
        //      - Если в настойках установлены сохранения винных полок = true,
        //      Берем минимальное значение номера винной полки
        currentShelfId = getStartShelfId();

//        Log.d(TAG, "MainActivity: currentShelfId = " + currentShelfId);

        // Инициализируем горизонтальный RecyclerView для винных полок
        initWineShelfRecyclerView(currentShelfId);

        // Инициализируем вертикальный RecyclerView для показа вина
        initWineRecyclerView();

        // Формирует начальный массив из базы данных винных полок (List<WineShelf>) wineShelfArray
        // Обновляет адаптер винных полок wineShelfAdapter данными из wineShelfArray
        getMyWineShelf();

    }

    //====================================================================================
    // Инициализация
    //====================================================================================

    // Инициализируем:
    //      - панель инструментов Toolbar
    private void initToolbar() {

//        Log.d(TAG,"MainActivity: init");

        // toolbar - Находим панель инструментов Toolbar в activity_main.xml
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        // Устанавливаем панель инструментов в качестве панели действий (ActionBar) для этого Activity
        setSupportActionBar(toolbar);

        // toolBarLayout - CollapsingToolbarLayout - это оболочка для панели инструментов,
        // которая реализует сворачивающуюся панель приложения.
        // Он предназначен для использования в качестве прямого потомка AppBarLayout
        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.main_collapsing);
        // Устанавливает заголовок, который будет отображаться в этом представлении,
        // если он включен
        // В данном случае, в качестве заголовка устанавливаем заголовок Activity
//        toolBarLayout.setTitle(getTitle());
//        toolBarLayout.setTitle("ggg");
        // Выключаем расширяющийся заголовок
        toolBarLayout.setTitleEnabled(false);

    }

    //====================================================================
    // Инициализируем переменные экрана
    //====================================================================
    private void initVariables() {
        // Основной контейнер окна activity_main.xml
        coordinator_layoutMain = findViewById(R.id.coordinator_layoutMain);
        // Сообщения экрана на главном экране
        tvMessageShelfWine = findViewById(R.id.tvMessageShelfWine);
        tvMessageWine = findViewById(R.id.tvMessageWine);
        // Плавающая кнопка - "Новое вино"
        fabNewWine = findViewById(R.id.fabNewWine);
    }


    //====================================================================
    // Инициализация базы данных
    //====================================================================
    private void initDb() {

//        Log.d(TAG, "MainActivity: initDb");

        // Получаем базу
        // База данных и dao для работы с ней
//        db = App.getInstance().getDatabase();
//        db = ((MyApplication) getApplication()).appContainer.getDatabase();
        db = ((MyApplication) getApplication()).getDatabase();




        // Из Database объекта получаем Dao.
        wineDao = db.wineDao();
        wineShelfDao = db.wineShelfDao();
        photoDao = db.photoDao();

//        myDbManager = new MyDbManager();

    }

    //====================================================================
    // Инициализируем горизонтальный RecyclerView для показа винных полок
    //====================================================================
    private void initWineShelfRecyclerView(long currentShelfId) {

//        Log.d(TAG, "MainActivity: initWineShelfRecyclerView");

        recyclerViewListWineShelf = findViewById(R.id.recyclerViewListWineShelf);
        LinearLayoutManager horizontalLayoutManager =
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false);
        recyclerViewListWineShelf.setLayoutManager(horizontalLayoutManager);
        wineShelfAdapter = new WineShelfAdapter(this, currentShelfId, recyclerViewListWineShelf);
        recyclerViewListWineShelf.setAdapter(wineShelfAdapter);

//        Log.d(TAG, "MainActivity: initWineShelfRecyclerView Конец");

    }

    //====================================================================
    // Инициализируем вертикальный RecyclerView для показа вина
    //====================================================================
    private void initWineRecyclerView() {

//        Log.d(TAG, "MainActivity: initWineRecyclerView");

        recyclerViewListWine = findViewById(R.id.recyclerViewListWine);
        // Устанавливаем обратный порядок следования элементов - reversLayout: true
        LinearLayoutManager verticalLayoutManager =
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        true);
        recyclerViewListWine.setLayoutManager(verticalLayoutManager);
        wineAdapter = new WineAdapter(this);
        recyclerViewListWine.setAdapter(wineAdapter);

//        Log.d(TAG, "MainActivity: initWineRecyclerView Конец");

    }

    // ============================================================================
    // Формирует начальный массив из базы данных винных полок (List<WineShelf>) wineShelfArray
    // Потом через интерфейс Обновляет адаптер винных полок WineShelfAdapter данными из wineShelfArray
    // Считывает данные во ВТОРОСТЕПЕННОМ потоке
    // ============================================================================
    private void getMyWineShelf() {

//        Log.d(TAG, "MainActivity: getMyWineShelf");

        // Получаем список винных полок во втором потоке
        AppExecuter.getInstance().getSubIO().execute(() -> myDbManager.getListWineShelf_Executer(
                MainActivity.this));
        // ВАЖНО - продолжение метода после отработки второго потока
        // в интерфейсе onReceivedListWineShelf

//        Log.d(TAG, "MainActivity: getMyWineShelf Конец");

    }

    //==============================================================================
    // Интерфейс - действия, когда мы считали массив винных полок
    // Обновление адаптера в ОСНОВНОМ потоке
    //==============================================================================
    @Override
    public void onReceivedListWineShelf(List<WineShelf> list) {
        AppExecuter.getInstance().getMainIO().execute(() -> {
            // Обновляем адаптер
            // updateAdapter - Вызываем обновить адатер (очищаем, заполняем, уведомляем)
            wineShelfAdapter.updateAdapter(list);

            // По номеру текущей винной полки currentShelfId определяем позицию адаптера
            int currentPosition = wineShelfAdapter.positionForShelfId(currentShelfId);
            // Прокручиваем recyclerView до новой винной полки
            recyclerViewListWineShelf.scrollToPosition(currentPosition);

            // Обновляем сообщения на главной странице для винной полки:
            // - Управляет видимостью/невидимостью "Сначала создайте винную полку"
            updateMessagesShelfWine();
        });


    }

    // ============================================================================
    // По номеру винной полки shelfId:
    //      - Формирует массив из базы данных вина (List<Wine>) wineArray
    //      - Потом через интерфейс Обновляет адаптер вина WineAdapter данными из wineArray
    // Считывает данные во ВТОРОСТЕПЕННОМ потоке
    // ============================================================================
    private void getWineFromShelf(long shelfId) {

//        Log.d(TAG, "MainActivity: getWineFromShelf");

        // Получаем список вин по номеру винной полки shelfId во втором потоке
        AppExecuter.getInstance().getSubIO().execute(() -> myDbManager.getListWineByShelfId_Executer(
                shelfId, MainActivity.this));
        // ВАЖНО - продолжение метода после отработки второго потока
        // в интерфейсе onReceivedListWine

//        Log.d(TAG, "MainActivity: getWineFromShelf Конец");

    }

    //==============================================================================
    // Интерфейс - действия, когда мы считали массив вин
    // Обновление адаптера в ОСНОВНОМ потоке
    //==============================================================================
    @Override
    public void onReceivedListWine(List<Wine> list) {
        AppExecuter.getInstance().getMainIO().execute(new Runnable() {
            @Override
            public void run() {
                // Обновляем адаптер
                // updateAdapter - Вызываем обновить адатер (очищаем, заполняем, уведомляем)
                wineAdapter.updateAdapter(list);

                // Обновляем сообщения на главной странице для вина:
                // - "А теперь добавьте на полку вина"
                // - и меняем, при необходимости, состояние кнопок FloatingActionButton
                updateMessagesWine();
            }
        });
    }


    // ============================================================================
    // Запрос разрешений
    // ============================================================================

    private void askPermissionAndCaptureVideo() {

        // TODO: Аккуратно пересмотреть фунции запросов на разрешение

        // android.os.Build.VERSION.SDK_INT - Версия SDK программного обеспечения, которое в
        // настоящее время работает на этом аппаратном устройстве
        // С Android Level> = 23 вы должны запросить у пользователя разрешение на
        // чтение / запись данных на устройстве.
        if (Build.VERSION.SDK_INT >= 23) {

            // Проверьте, есть ли у нас разрешение на чтение / запись
            int readPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            int record_audioPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO);

            // Если
            //      нет разрешения на запись ИЛИ
            //      нет разрешения на чтение ИЛИ
            //      нет разрешения для доступа к камере
            if (writePermission != PackageManager.PERMISSION_GRANTED ||
                    readPermission != PackageManager.PERMISSION_GRANTED ||
                    cameraPermission != PackageManager.PERMISSION_GRANTED ||
                    record_audioPermission != PackageManager.PERMISSION_GRANTED) {
                // Если у вас нет разрешения, попросите пользователя.
                this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA},
                        GlobalConstants.REQUEST_ID_READ_WRITE_PERMISSION
                );
                return;
            }
        }
    }

    //====================================================================================
    // Когда у вас есть результаты запроса на разрешения
    //====================================================================================
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case GlobalConstants.REQUEST_ID_READ_WRITE_PERMISSION: {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (read/write).
                // Примечание. Если запрос отменен, массивы результатов пусты.
                // Предоставленные разрешения (чтение / запись)
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, R.string.permission_received, Toast.LENGTH_LONG).show();
                }
                // Cancelled or denied.
                else {
                    Toast.makeText(this, R.string.access_denied, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    //====================================================================================
    // Была нажата винная полка
    // В onClickWineShelfListener метод clickWineShelf, который получает на вход номер винной полки
    //====================================================================================
    @Override
    public void clickWineShelf(long mShelfId) {

//        Log.d(TAG, "MainActivity: clickWineShelf");

        // Запоминаем текущую винную полку
        currentShelfId = mShelfId;
        // По номеру винной полки currentShelfId:
        //      - Формирует массив из базы данных вина (List<Wine>) wineArray
        //      - Обновляет адаптер вина WineAdapter данными из wineArray
        getWineFromShelf(currentShelfId);

        // Обновляем сообщения на главной странице для винной полки:
        // - Управляет видимостью/невидимостью "Сначала создайте винную полку"
        updateMessagesShelfWine();

        // Если настойках установлены сохранения винных полок = true
        // то в настойки кладем текущую винную полку
        if (global_wine_shelf_save_last) {
            // TODO: операцию prefs = PreferenceManager.getDefaultSharedPreferences(this);
            //  нужно делать один раз. А то слишком много

            // Получает экземпляр SharedPreferences, указывающий на файл по умолчанию,
            // который используется платформой предпочтений в данном контексте.
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            // Создаем едитор для записи настроек
            SharedPreferences.Editor editor = prefs.edit();
            // Помещаем в едитор текущую винную полку
            editor.putLong(wine_shelf_save_last_value, mShelfId);
            // Сохраняем изменения
            editor.apply();
        }

//        Log.d(TAG, "MainActivity: clickWineShelf Конец");

    }

    //===================================================================================
    // Диалог - Создание новой винной полки
    //===================================================================================
    // На нажатие fabNewWineShelf - создание новой винной полки
    public void onClickNewWineShelf(View view) {

        // Если это ПРОФЕССИОНАЛЬНАЯ версия
        if (isTheProfessionalVersion) {
            // Создаем винную полку
            // создаем диалог - введите название винной полки
            DialogFragment dialog = new NewWineShelfDialogFragment();
            // показываем диалог
            dialog.show(getSupportFragmentManager(), "NewWineShelfDialogFragment");
        }
        // Если это НЕ ПРОФЕССИОНАЛЬНАЯ версия
        else {
            // Если количество винных полог не превысило ограниние
            if (wineShelfAdapter.getItemCount() < restrictionsAmountWineShelfs) {
                // Создаем винную полку
                // создаем диалог - введите название винной полки
                DialogFragment dialog = new NewWineShelfDialogFragment();
                // показываем диалог
                dialog.show(getSupportFragmentManager(), "NewWineShelfDialogFragment");
            }
            // Если количество винных полог превысило ограниние
            else {
                // Запрашиваем переход на профессинальную версию
                // Подготовка сообщения о премиум версии
                String mMessage;
//                mMessage = "Для этой версии ограничения: не более " + restrictionsAmountWineShelfs +" винных полок.\n\n" +
                mMessage = getResources().getString(R.string.dialog_RestrictionsForThisVersion) + " " +
                        restrictionsAmountWineShelfs + " " +
                        getResources().getString(R.string.dialog_WineShelves) +
                        getResources().getString(R.string.tvGoPremiumText);
                // Используйте класс Builder для удобного построения диалогов
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        this,
                        R.style.AlertDialogTheme);
                builder
                        .setTitle(R.string.dialog_GoPremium)    // Заголовок диалога
                        .setMessage(mMessage)    // Заголовок диалога
                        // Позитивная кнопка - Перейти на Premium
                        .setPositiveButton(R.string.dialog_GoPremium_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Перейти на Premium
                                goPremium();
                            }
                        })
                        // Отрицательная кнопка - Отказ
                        .setNegativeButton(R.string.dialog_GoPremium_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Отказ от перехода.
                                // Ничего не делаем

                            }
                        });
                // Создаем диалог
                AlertDialog alert = builder.create();
                // Показываем диалог
                alert.show();
            }
        }

        //TODO: Нужно поправить макет ввода винной полки - заголовок, рисунок

    }

    //============================================================================
    //Реакция на положительное или отрицательное нажание клавиш
    // Фрагмент диалогового окна получает ссылку на это действие через обратный
    // вызов Fragment.onAttach (), который он использует для вызова следующих методов,
    // определенных интерфейсом NoticeDialogFragment.NoticeDialogListener
    //============================================================================
    // Пользователь коснулся положительной кнопки диалога - создание новой винной полки
    // Вставляем данные во ВТОРОСТЕПЕННОМ потоке
    @Override
    public void onDialogPositiveClickNewWineShelf(NewWineShelfDialogFragment dialog) {

//        Log.d(TAG, "MainActivity: onDialogPositiveClickNewWineShelf");

        // Проверка, что введенное значение не пусто
        String nameShelf = dialog.getNameShelf();
        if (!nameShelf.isEmpty()) // если введенное наименование не пусто
        {
            // Вставляем новую винную полку
            // Создаем новую винную полку
            WineShelf wineShelf = new WineShelf();
            // помещаем в винную полку название
            wineShelf.setNameShelf(nameShelf);

            // Вставляем винную полку во втором потоке
            AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        myDbManager.insertWineShelf_Executer(
                                MainActivity.this,
                                wineShelf,
                                MainActivity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            // ВАЖНО - продолжение метода после отработки второго потока
            // в интерфейсе onInsertedWineShelf

        }
        else // если введенное наименование пусто
        {
            Snackbar.make(coordinator_layoutMain,
                    R.string.shelf_name_must_not_be_empty,
                    Snackbar.LENGTH_LONG).show();
        }

//        Log.d(TAG, "MainActivity: onDialogPositiveClickNewWineShelf Конец");

    }

    //==============================================================================
    // Интерфейс - действия, когда мы вставили винную полку
    // Обновление адаптера в ОСНОВНОМ потоке
    //==============================================================================
    @Override
    public void onInsertedWineShelf(long ShelfId, WineShelf wineShelf) {
        AppExecuter.getInstance().getMainIO().execute(new Runnable() {
            @Override
            public void run() {
                // Запоминаем текущий номер винной полки
                currentShelfId = ShelfId;

                // помещаем в винную полку новый номер винной полки
                wineShelf.setId(currentShelfId);

                // Обновляем адаптер, добавляя в данные адаптера wineShelf и используя notifyItemInserted
                wineShelfAdapter.updateAdapterWineShelfInserted(wineShelf);

                // Прокручиваем recyclerView до новой винной полки
                recyclerViewListWineShelf.scrollToPosition(wineShelfAdapter.getItemCount() - 1);

                // По номеру винной полки currentShelfId:
                //      - Формирует массив из базы данных вина (List<Wine>) wineArray
                //      - Обновляет адаптер вина WineAdapter данными из wineArray
                getWineFromShelf(currentShelfId);

                // Обновляем сообщения на главной странице для винной полки:
                // - Управляет видимостью/невидимостью "Сначала создайте винную полку"
                updateMessagesShelfWine();
            }
        });
    }

    //====================================================================================
    // Пользователь коснулся отрицательной кнопки диалога - создание новой винной полки
    //====================================================================================
    @Override
    public void onDialogNegativeClickNewWineShelf(NewWineShelfDialogFragment dialog) {

        // Пока ничего не делаем

    }

    //===================================================================================
    // Создание нового вина
    //===================================================================================
    // На нажатие fabNewWine - создание нового вина
    public void onClickNewWine(View view) {

        if (currentShelfId > 0) // Винная полка есть
        {
            // Если это ПРОФЕССИОНАЛЬНАЯ версия
            if (isTheProfessionalVersion) {
                // Создаем новое вино
                // Создаем новый интент
                Intent i = new Intent(this, EditActivity.class);
                // Передаем в интенте Номер в базе данных винной полки position
                i.putExtra(GlobalConstants.WINE_INTENT_NEW_WINE, currentShelfId);
                // Запускаем интент
                startActivity(i);
            }
            // Если это НЕ ПРОФЕССИОНАЛЬНАЯ версия
            else {
                // Если количество вина на винной полке не превысило ограниние
                if (wineAdapter.getItemCount() < restrictionsAmountWines) {
                    // Создаем новое вино
                    // Создаем новый интент
                    Intent i = new Intent(this, EditActivity.class);
                    // Передаем в интенте Номер в базе данных винной полки position
                    i.putExtra(GlobalConstants.WINE_INTENT_NEW_WINE, currentShelfId);
                    // Запускаем интент
                    startActivity(i);
                }
                // Если количество вина на винной полке превысило ограниние
                else {
                    // Запрашиваем переход на профессинальную версию
                    // Подготовка сообщения о премиум версии
                    String mMessage;
//                    mMessage = "Для этой версии ограничения: не более " + restrictionsAmountWines +" вин на одной винной полке.\n\n" +
//                    mMessage = R.string.dialog_RestrictionsForThisVersion + restrictionsAmountWines + R.string.dialog_WinesOnOneWineShelf +
                    mMessage = getResources().getString(R.string.dialog_RestrictionsForThisVersion) + " " +
                            restrictionsAmountWines + " " +
                            getResources().getString(R.string.dialog_WinesOnOneWineShelf) +
                            getResources().getString(R.string.tvGoPremiumText);
                    // Используйте класс Builder для удобного построения диалогов
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            this,
                            R.style.AlertDialogTheme);
                    builder
                            .setTitle(R.string.dialog_GoPremium)    // Заголовок диалога
                            .setMessage(mMessage)    // Заголовок диалога
                            // Позитивная кнопка - Перейти на Premium
                            .setPositiveButton(R.string.dialog_GoPremium_yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Перейти на Premium
                                    goPremium();
                                }
                            })
                            // Отрицательная кнопка - Отказ
                            .setNegativeButton(R.string.dialog_GoPremium_no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Отказ от перехода.
                                    // Ничего не делаем

                                }
                            });
                    // Создаем диалог
                    AlertDialog alert = builder.create();
                    // Показываем диалог
                    alert.show();
                }
            }
        }
        else // Винной полки нет
        {
            Snackbar.make(coordinator_layoutMain,
                    R.string.first_create_WineShelf,
                    Snackbar.LENGTH_LONG).show();
        }

    }

    //====================================================================================
    // Берем значение номера винной полки:
    //      - Если в настойках установлены сохранения винных полок = true,
    //      то берем сохраненные в настройках
    //      - Если в настойках установлены сохранения винных полок = false,
    //      Берем минимальное значение номера винной полки
    //====================================================================================
    public long getStartShelfId() {

        long returnNinShelfId = -1;

        // Если настойках установлены сохранения винных полок = true
        // то берем сохраненные в настройках
        if (global_wine_shelf_save_last) {

            // TODO: операцию prefs = PreferenceManager.getDefaultSharedPreferences(this);
            //  нужно делать один раз. А то слишком много

            // Получает экземпляр SharedPreferences, указывающий на файл по умолчанию,
            // который используется платформой предпочтений в данном контексте.
            prefs = PreferenceManager.getDefaultSharedPreferences(this);

            // Берем Значение, которое сохранено в настойках
            returnNinShelfId = prefs.getLong(wine_shelf_save_last_value, -1);

        }

        // Если в настойках установлены сохранения винных полок = false
        // ИЛИ не удалось взять из настроек, то
        // Берем минимальное значение номера винной полки
        if (!global_wine_shelf_save_last || returnNinShelfId <= 0) {
            final long[] minShelfId = new long[1];

            theSecondThreadIsRunning = true;
//            Log.d(TAG, "Начало выполнения 2 потока");

            // Создаем новый поток
            new Thread(new Runnable() {
                // У интерфейса Runnable() необходимо переопределить метод run (в котором будут вычисления)
                public void run() {
                    // Берем минимальное значение номера винной полки
                    minShelfId[0] = MainActivity.wineShelfDao.getMinShelfId();

                    theSecondThreadIsRunning = false;
//                    Log.d(TAG,"Конец выполнения 2 потока");

                }
            }).start();

            while (theSecondThreadIsRunning) { // линия 1
                // TODO: Здесь вынуждены ждать, пока не отработает 2 поток и не предоставит данные,
                //  плохай стиль 1) передача через глобальную переменную, 2) задержка потока IU
                // Ждем - выполняется второй поток
            }

            // Номер есть в базе
            if (minShelfId[0] > 0)
            {
                returnNinShelfId = minShelfId[0];
            }

        }
        return returnNinShelfId;

    }


    //===================================================================================
    @Override
    public void onResume() {
        super.onResume();

//        Log.d(TAG,"MainActivity: onResume");

        // Берем настройки из Preferences
        getMyPreferences();

        // По номеру винной полки currentShelfId:
        //      - Формирует массив из базы данных вина (List<Wine>) wineArray
        //      - Обновляет адаптер вина WineAdapter данными из wineArray
        getWineFromShelf(currentShelfId);

//        Log.d(TAG,"MainActivity: onResume Конец");

    }

    //====================================================================================
    // Настраиваем меню
    // Создаем меню - раздуваем из R.menu.menu_scrolling
    //====================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

//        Log.d(TAG,"MainActivity: onCreateOptionsMenu");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_menu_scrolling, menu);
        return true;
    }

    //====================================================================================
    // Здесь обрабатываются щелчки по элементам панели действий (меню).
    // Панель действий будет автоматически обрабатывать нажатия кнопки «Домой / Вверх»,
    // если вы укажете родительское действие в AndroidManifest.xml.
    //====================================================================================
    public boolean onOptionsItemSelected(MenuItem item) {

//        Log.d(TAG,"MainActivity: onOptionsItemSelected");

        // Вернуть идентификатор этого пункта меню.
        // Идентификатор нельзя изменить после создания меню
        int id = item.getItemId();

        Intent intent;
        switch (id) {

            //Действие на нажание "Как начать?"
            case R.id.how_to_start:
                intent = new Intent(this, SplashScreen.class);
                startActivity(intent);
                break;

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

            //Действие на нажание "О приложении"
            case R.id.action_about_the_application:
                intent = new Intent(this, AboutTheApplication.class);
                startActivity(intent);
                break;

            //Действие на нажание "Что нового"
            case R.id.action_whats_new:
                intent = new Intent(this, WhatsNew.class);
                startActivity(intent);
                break;

            //Действие на нажание "Оценка и отзыв"
            case R.id.action_rate_and_review:
                intent = new Intent(this, ScreenRateAndReview.class);
                startActivity(intent);
                break;

            //Действие на нажание "Переход на премиум"
            case R.id.action_go_premium:
                goPremium();
                break;

/*            //Действие на нажание "Потребляйте Премиум (Для тестирования)"
            case R.id.action_consume_premium:
                mainActivityViewModel.debugConsumePremium();
                break;*/

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;


/*
        //Действие на нажание Настойки
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);*/
    }

    //===================================================================================
    // Действие на вызов интента startActivityForResult
    //===================================================================================
    //TODO: почему версия Build.VERSION_CODES.KITKAT?
//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // результат RESULT_OK и
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                // Вернулся интента для выбора картинки для винной полки
                case REQUEST_CHOOSE_FROM_PICTURES:

                    // возвращенные данные не null
                    if (data != null) {

                        // Возвращенный индекс картини
                        int indexPicrure = data.getIntExtra(CHOOSE_FROM_PICTURES_INDEX, 1);
                        // Возвращенная винная полка
                        // Получаем расширенные данные из намерения
                        // Используем что класс WineShelf сериализуем Serializable, поэтому можно в интенте
                        // передавать весть класс и брать из интента используя getSerializableExtra
                        // Обязательно указываем (WineShelf), что бы знать, что возвращаем именно (WineShelf)
                        WineShelf wineShelf = (WineShelf) data.getSerializableExtra(CHOOSE_FROM_PICTURES_WINE_SHELF_FROM);
                        // Возвращенная позиция адаптера
                        int position = data.getIntExtra(CHOOSE_FROM_PICTURES_POSITION_FROM, 0);

                        // Обновляем винную полку во втором потоке
                        AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    myDbManager.updateWineShelfPicture_Executer(
                                            MainActivity.this,
                                            wineShelf,
                                            indexPicrure,
                                            position,
                                            MainActivity.this);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        // ВАЖНО - продолжение метода после отработки второго потока
                        // в интерфейсе onUpdatedWineShelfPicture

                    }
                    break;

                // Вернулся интента для загрузки нового фото из галереи. Вызывается из WineShelfAdapter
                case PICK_IMAGE_CODE_WINE_SHELF:

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
                                Toast.makeText(this, R.string.file_was_not_created, Toast.LENGTH_LONG).show();
                            }
                            // Продолжить, только если файл был успешно создан
                            if (mPhotoFile != null) {

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

                                // Добавить фото в галерею
                                galleryAddPic(mCurrentPhotoPath);

/*
                                // Возвращенная винная полка
                                // Получаем расширенные данные из намерения
                                // Используем что класс WineShelf сериализуем Serializable, поэтому можно в интенте
                                // передавать весть класс и брать из интента используя getSerializableExtra
                                // Обязательно указываем (WineShelf), что бы знать, что возвращаем именно (WineShelf)
                                WineShelf wineShelf = (WineShelf) data.getSerializableExtra(CHOOSE_FROM_PICTURES_WINE_SHELF_FROM);
                                // Возвращенная позиция адаптера
                                int position = data.getIntExtra(CHOOSE_FROM_PICTURES_POSITION_FROM, 0);
*/
                                //TODO: Не удалось передать в интенте chooserPhoto винную полку и позицию в адаптере.
                                // Это временное решение
                                // Приходится передавать через глобальную переменую

                                // Возвращенная винная полка
                                WineShelf wineShelf = globalWineShelf;
                                // Возвращенная позиция адаптера
                                int position = globalPosition;

                                // Обновляем винную полку во втором потоке
                                AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            myDbManager.updateWineShelfAbsolutePath_Executer(
                                                    MainActivity.this,
                                                    wineShelf,
                                                    mCurrentPhotoPath,
                                                    position,
                                                    MainActivity.this);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                // ВАЖНО - продолжение метода после отработки второго потока
                                // в интерфейсе onUpdatedWineShelfPicture
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

                // Вернулся интент для создания нового фото. Вызывается из WineShelfAdapter
                case REQUEST_LARGE_PHOTO_WINE_SHELF:
                    // TODO: Пересмотреть создание фото
                    try {
                        //TODO: Обработка фото должна выполняться во втором потоке (особенно, поворот фото)

                        // Если в настойка стоит - при фотографировании нужно поворачивать фото
                        if (!global_rotation.equals("rotation_no")) {

                            int degreeOfRotation;
                            if (global_rotation.equals("Повернуть влево")) {
                                degreeOfRotation = 3;
                            } else {
                                degreeOfRotation = 1;
                            }

                            //==============================
                            // Определяем имя временного файла для сохранения
                            //TODO: этот фрагмен постоянно копируется - перенести в библиотеку

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
                                    globalPhotoAbsolutePathShelf,
                                    newFile,
                                    degreeOfRotation);


                            // Заменяем абсолютный путь к файлу на результат поворота
                            globalPhotoAbsolutePathShelf = newFile.getAbsolutePath();

                        }

                        //TODO: Не удалось передать в интенте chooserPhoto винную полку и позицию в адаптере.
                        // Это временное решение
                        // Приходится передавать через глобальную переменую

                        // Возвращенная винная полка
                        WineShelf wineShelf = globalWineShelf;
                        // Возвращенная позиция адаптера
                        int position = globalPosition;
                        // // Возвращенная Ссылка на фото
                        String mCurrentPhotoPath = globalPhotoAbsolutePathShelf;

                        // Обновляем винную полку во втором потоке
                        AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    myDbManager.updateWineShelfAbsolutePath_Executer(
                                            MainActivity.this,
                                            wineShelf,
                                            mCurrentPhotoPath,
                                            position,
                                            MainActivity.this);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        // ВАЖНО - продолжение метода после отработки второго потока
                        // в интерфейсе onUpdatedWineShelfPicture
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.error_occurred_while_trying_to_take_photo, Toast.LENGTH_LONG).show();
//                        e.printStackTrace();
                    }
                    break;
            }

        }
    }

    //==============================================================================
    // Интерфейс - действия, когда мы обновили винную полку
    // Обновление адаптера в ОСНОВНОМ потоке
    //==============================================================================
    @Override
    public void onUpdatedWineShelfPicture(int position, String photoAbsolutePathShelf) {
        AppExecuter.getInstance().getMainIO().execute(new Runnable() {
            @Override
            public void run() {
                // Обновляем элемент массива wineShelfArray в позици position
                //  - заменяет photoAbsolutePathShelf
                wineShelfAdapter.updateWineShelfArrayWithPhotoAbsolutePathShelf(
                        position,
                        photoAbsolutePathShelf);
                // Обновляем адаптер, обновляя один элемент адаптера wineShelf и используя notifyItemChanged
                wineShelfAdapter.updateAdapterWineShelfUpdated(position);
            }
        });
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
//        File storageDir = ((AppCompatActivity)context).getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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

    // ============================================================================
    // Берем настройки из Preferences
    // ============================================================================
    private void getMyPreferences() {

        // TODO: операцию prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //  нужно делать один раз. А то слишком много

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
            editor.putLong(wine_shelf_save_last_value,currentShelfId);
            // Сохраняем изменения
            editor.apply();
        }

        // Берем Настройки голосового ввода и помещаем их в глобальную переменную
        global_enable_voice_input = prefs.getBoolean("enable_voice_input", true);

        // Берем Настройки внешнего вида - вид оценки: бокалы, звездочки и помещаем их в глобальную переменную
        global_type_of_assessmen = prefs.getString("type_of_assessmen", "type_of_assessmen_wineglass");
    }

    // ============================================================================
    // Обновляем сообщения на главной странице для винной полки:
    // - Управляет видимостью/невидимостью "Сначала создайте винную полку"
    // ============================================================================
    private void updateMessagesShelfWine() {

        // Если есть винные полки
        if (wineShelfAdapter.getItemCount() > 0) {
            // Скрываем сообщение "Сначала создайте винную полку"
            tvMessageShelfWine.setVisibility(View.INVISIBLE);
        }
        // Если нет винных полок
        else {
            // Выводим сообщение "Сначала создайте винную полку"
            tvMessageShelfWine.setVisibility(View.VISIBLE);
        }
    }


    // ============================================================================
    // Обновляем сообщения на главной странице для вина:
    // - "А теперь добавьте на полку вина"
    // - и меняем, при необходимости, состояние кнопок FloatingActionButton
    // ============================================================================
    private void updateMessagesWine() {

        // Если винных полок нет
        if (currentShelfId < 0) {
            // Скрываем сообщение "А теперь добавьте на полку вина"
            tvMessageWine.setVisibility(View.INVISIBLE);
            // Скрываем плавающую кнопку "Новое вино"
            fabNewWine.setVisibility(View.GONE);
        }
        // Если имеются винные полки
        else {
            // Если есть вина в винной полке
            if (wineAdapter.getItemCount() > 0) {
                // Скрываем сообщение "А теперь добавьте на полку вина"
                tvMessageWine.setVisibility(View.INVISIBLE);
            }
            // Если нет вина в винной полке
            else {
                // Выводим сообщение "А теперь добавьте на полку вина"
                tvMessageWine.setVisibility(View.VISIBLE);
            }
            // Выводим плавающую кнопку "Новое вино"
            fabNewWine.setVisibility(View.VISIBLE);
        }

    }


    //====================================================================================
    // Было удалено вино
    // В DeleteWineInterfaceListener метод deleteWineInterface
    //====================================================================================
    @Override
    public void deleteWineInterface() {

        // Обновляем сообщения на главной странице для вина:
        // - "А теперь добавьте на полку вина"
        // - и меняем, при необходимости, состояние кнопок FloatingActionButton
        updateMessagesWine();
    }

    //====================================================================================
    // Перейти на Premium
    //====================================================================================

    public void goPremium() {
        // Создаем новый интент
        Intent intent = new Intent(this, MakePurchase.class);
        // Запускаем активность
        startActivity(intent);
    }

    //====================================================================================
    // Нажата системная кнопка выхода из приложения
    // Чтобы выйти из приложения, кнопку нужно нажать дважды
    //====================================================================================
    @Override
    public void onBackPressed() {

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        }
        else {
            backToast = Toast.makeText(getBaseContext(), R.string.backPressedText, Toast.LENGTH_SHORT);
            backToast.show();
        }
        // Время нажатия на кнопку назад
        backPressedTime = System.currentTimeMillis();

    }

    // endregion

    //=============================================================================
    // region: Внутренний статический класс: PublicKeyNotSetDialog
    //=============================================================================

    /**======================================================================================
     * Диалог - открытый ключ не установлен
     * ======================================================================================
     */
    public static class PublicKeyNotSetDialog extends DialogFragment {
        static final String DIALOG_TAG = "PublicKeyNotSetDialog";
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.alert_error_title_encoded_public_key_not_set)
                    .setMessage(
                            R.string.alert_error_message_encoded_public_key_not_set)
                    .setPositiveButton(getString(android.R.string.ok),
                            (dialog, which) -> {
                            })
                    .create();
        }
    }

    // endregion

}