package com.apushnikov.sommelier_notebook.Global;

import com.apushnikov.sommelier_notebook.db.WineShelf;
import com.apushnikov.sommelier_notebook.ui.adapterFhotoList.PhotoAdapter;
import com.apushnikov.sommelier_notebook.ui.adapterWineShelfList.WineShelfAdapter;

public class GlobalConstants {

    //============================================================================
    // Флаг - является ли версия ПРОФЕССИОНАЛЬНОЙ
    //============================================================================
    // Флаг - является ли версия ПРОФЕССИОНАЛЬНОЙ
    //      - true - является
    //      - false - НЕ является

    // Для БОЕВОГО приложения влючить это
//    public static boolean isTheProfessionalVersion = false;
    // Для ТЕСТА приложения влючить это
    public static boolean isTheProfessionalVersion = true;

    // Ограничения на количество винных полок (для версии фри)
    public static int restrictionsAmountWineShelfs = 1000;

    // Ограничения на количество вин в винных полках (для версии фри)
    public static int restrictionsAmountWines = 1000;

    //============================================================================
    // Это для показа рекламы
    //============================================================================

    // Идентификатор рекламного блока

    // Для БОЕВОЙ реламы влючить это

    // Это БОЕВОЙ идентификатор рекламного блока для Native Advanced
//    public static final String AD_MANAGER_AD_UNIT_ID = "ca-app-pub-XXX/XXX";

    // Для ТЕСТОВОЙ рекламы влючить это

    // Это для Native Advanced
    public static final String AD_MANAGER_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";

    // Это для Native Advanced Video
    public static final String AD_MANAGER_AD_UNIT_ID_VIDEO = "ca-app-pub-3940256099942544/1044960115";

    // Флаг, показывающий, что за рекламу запускать:
    //  - launchVideoAd = false; - запускаем Native Advanced AD_MANAGER_AD_UNIT_ID
    //  - launchVideoAd = true; - запускаем Native Advanced Video AD_MANAGER_AD_UNIT_ID_VIDEO
        public static boolean launchVideoAd = false;



    // Идентификатор пользовательского формата,
    // определенный в пользовательском интерфейсе
    // Менеджера рекламы.
    // ЭТО МЫ НЕ ИСПОЛЬЗУЕМ!!!!!
    // Взято из демо-примера - не нашел ссылки в документации, что это такое
    public static final String SIMPLE_TEMPLATE_ID = "10104090";


    //============================================================================
    // Это для вызова класса DebugScreen (для отладки)
    //============================================================================
    // Это для экрана дебаг
    public static final String DEBUG_STRING = "debug_string";
    //============================================================================

    //============================================================================
    // Это для определения высоты клавиатуры
    //============================================================================
    // Высота клавиатуры
    public static int global_KeyboardHeight = 0;

    // Допуск на высоту плавоющих кнопок
//    public static final int global_HeightOfFloatingButtons = 220;
    public static final int global_HeightOfFloatingButtons = 250;

    // Это костыль для высоты клавиатуры
    // При увеличении высоты клавиатуры белее, чем на 50%,
    // константу global_KeyboardHeight не увеличиваем.
    public static final float maxIncrease = 1.5f;

    //============================================================================
    // Это флаг.
    // Выполняется второй поток. Значение по умолчание false (т.е. не выполняется)
    public static volatile boolean theSecondThreadIsRunning = false;

    // Константа для вызова интента для создания/редактирования вина
    public static final String WINE_INTENT = "wine_intent";
    // При передаче интента в экран редактирования, значения, передаваемые через констранту:
    //      - true: передаем интент для редактирования вина
    //      - false: передаем интент для создания нового вина
    //          (При этом через констранту WINE_INTENT должны быть передан List<Wine> из mainArray
    public static final String EDIT_STATE = "edit_state";
    // При передаче интента для нового вина в экран редактирования, значения, передаваемые через констранту:
    //      - shelfId: Номер в базе данных винной полки position
    public static final String WINE_INTENT_NEW_WINE = "wine_intent_new_wine";

    // При передаче интента для обработки фото - для увеличения и просмотра
    public static final String PHOTO_PROCESSING = "photo_processing";
    // При передаче интента для обработки фото - для сигнала, что фото является главным
    public static final String PHOTO_PROCESSING_IS_MAIN = "photo_processing_is_main";
    // При передаче интента для обработки фото - позиция для поворачиваемого фото в PhotoAdapter
    public static final String PHOTO_PROCESSING_POSITION = "photo_processing_position";

    // При передаче интента для обработки фото - контекст для PhotoAdapter для передачи обратногно вызова
    // т.к не получается передать контекст в интенте, приходится его передавать через
    // внешнюю переменную
    // см. PhotoAdapter.java метод public void onClick(View v)
    // Чтобы обеспечить обратный вызов onPhotoFileRotation
    // TODO: не получается передать контекст в интенте, приходится его передавать через внешнюю переменную
    public static PhotoAdapter context_PhotoAdapter;
    public static WineShelfAdapter context_WineShelfAdapter;
    // При передаче интента для обработки фото - тип передаваемого адаптера
    // Передаваемые значения
    //      PHOTO_PROCESSING_TYPE_PHOTO_ADAPTER = 1: передается context_PhotoAdapter
    //      PHOTO_PROCESSING_TYPE_WINE_SHELF_ADAPTER = 2: передается context_WineShelfAdapter
    public static final String PHOTO_PROCESSING_TYPE = "photo_processing_type";
    public static final int PHOTO_PROCESSING_TYPE_PHOTO_ADAPTER = 1;
    public static final int PHOTO_PROCESSING_TYPE_WINE_SHELF_ADAPTER = 2;


    // При передаче интента для выбора картинки для винной полки - класс винной полки
    public static final String CHOOSE_FROM_PICTURES_WINE_SHELF = "choose_from_picrures_wine_shelf";
    // При передаче интента для выбора картинки для винной полки - позиция адаптера
    public static final String CHOOSE_FROM_PICTURES_POSITION = "choose_from_picrures_position";
    // При возврате интента для выбора картинки для винной полки - номер позиции выбранной картинки
    public static final String CHOOSE_FROM_PICTURES_INDEX = "choose_from_picrures_index";
    // При возврате интента для выбора картинки для винной полки - класс винной полки
    public static final String CHOOSE_FROM_PICTURES_WINE_SHELF_FROM = "choose_from_picrures_wine_shelf_from";
    // При возврате интента для выбора картинки для винной полки - позиция адаптера
    public static final String CHOOSE_FROM_PICTURES_POSITION_FROM = "choose_from_picrures_position_from";


    // Код для вызова интента для загрузки нового фото из галереи
    public static final int PICK_IMAGE_CODE = 100;
    // Код для вызова интента для загрузки нового фото из галереи. Вызывается из WineShelfAdapter
    public static final int PICK_IMAGE_CODE_WINE_SHELF = 101;
    // Код для вызова интента для создания нового фото
    public static final int REQUEST_LARGE_PHOTO = 102;
    // Код для вызова интента для создания нового фото. Вызывается из WineShelfAdapter
    public static final int REQUEST_LARGE_PHOTO_WINE_SHELF = 103;
    // Код для вызова интента для распознавания речи
    public static final int REQUEST_CODE_RECOGNIZER_INTENT = 104;
    // Код для вызова интента для выбора картинки для винной полки
    public static final int REQUEST_CHOOSE_FROM_PICTURES = 105;

    //Запрос разрешений
    public static final int REQUEST_ID_READ_WRITE_PERMISSION = 200;  // Разрешение чтение-запись
    public static final int REQUEST_ID_CAMERA_PERMISSION = 201;  // Разрешение на камеру

    // ============================================================================
    //Значения, сохраненные в Preference
    // ============================================================================
    // Настройки поворота фото
    public static String global_rotation = "rotation_no";
    // Настройки темы
    public static String global_theme = "theme_light";
    // Настройки сохранения винных полок
    public static boolean global_wine_shelf_save_last = true;
    // Настройки голосового ввода
    public static boolean global_enable_voice_input = true;

    //TODO: Для настроек нужно сохранять ключи - сейчас использутся текст в "root_preferences.xml"
    // - нужно перенести сюда

    // Ключ, для сохранения в настойках текущей винной полки
    public static String wine_shelf_save_last_value = "wine_shelf_save_last_value";

    // Настройки внешнего вида - вид оценки: бокалы, звездочки
    public static String global_type_of_assessmen = "type_of_assessmen_wineglass";

    // ============================================================================
    //TODO: Не удалось передать в интенте chooserPhoto винную полку и позицию в адаптере.
    // Это временное решение
    // Приходится передавать через глобальную переменую

    // Передаем винную полку через глобальную переменную
    public static WineShelf globalWineShelf = null;
    // Передаем позицию адаптера через глобальную переменную
    public static int globalPosition = 0;
    // Передаем Ссылка на фото через глобальную переменную
    public static String globalPhotoAbsolutePathShelf = "";


}
