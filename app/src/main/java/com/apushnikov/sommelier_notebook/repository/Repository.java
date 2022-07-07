package com.apushnikov.sommelier_notebook.repository;

import android.app.Activity;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;

//import com.apushnikov.other301_mypurchases.R;
//import com.apushnikov.other301_mypurchases.billing.BillingDataSource;
//import com.apushnikov.other301_mypurchases.log_file.LogFile;
//import com.apushnikov.other301_mypurchases.ui.SingleMediatorLiveEvent;
import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.billing.BillingDataSource;
import com.apushnikov.sommelier_notebook.billing.ui.SingleMediatorLiveEvent;
import com.apushnikov.sommelier_notebook.log_file.LogFile;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ??? Репозиторий использует данные из источника данных Billing и модели состояния игры вместе,
 * чтобы предоставить унифицированную версию состояния игры для ViewModel.
 * Он работает в тесном сотрудничестве с BillingDataSource для реализации расходных материалов,
 * предметов премиум-класса и т. Д.
 * <p>
 * The repository uses data from the Billing data source and the game state model together to give a
 * unified version of the state of the game to the ViewModel. It works closely with the
 * BillingDataSource to implement consumable items, premium items, etc.
 */
public class Repository {

    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    public LogFile logFile;

    //==================================================================
    // ВАЖНО ДЛЯ БИЛЛИНГА
    // Следующие строки SKU должны совпадать со строками в консоли разработчика Google Play.
    // Артикулы для покупок без подписки
    // The following SKU strings must match the ones we have in the Google Play developer console.
    // SKUs for non-subscription purchases
    //==================================================================
    /** SKU_PREMIUM - SKU для покупок по продуктов (премиум)
     * <p>
     * Эта строка SKU должна совпадать со строками в консоли разработчика Google Play.*/
    static final public String SKU_PREMIUM = "premium";

    /** SKU_GAS - SKU для покупок по продуктов (газ)
     * <p>
     * Эта строка SKU должна совпадать со строками в консоли разработчика Google Play.*/
    static final public String SKU_GAS = "gas";
    /** SKU_INFINITE_GAS_MONTHLY - SKU для покупок по подписке (бесконечный газ, ежемесячная подписка)
     * <p>
     * Эта строка SKU должна совпадать со строками в консоли разработчика Google Play.*/
    static final public String SKU_INFINITE_GAS_MONTHLY = "infinite_gas_monthly";
    /** SKU_INFINITE_GAS_YEARLY - SKU для покупок по подписке (бесконечный газ, ежегодная подписка)
     * <p>
     * Эта строка SKU должна совпадать со строками в консоли разработчика Google Play.*/
    static final public String SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearly";

//    static final String TAG = "TrivialDrive:" + TrivialDriveRepository.class.getSimpleName();

    /** INAPP_SKUS массив строк - артикулы для продкуктов (премиум, газ)*/
    static final public String[] INAPP_SKUS = new String[]{SKU_PREMIUM, SKU_GAS};
//    static final public String[] INAPP_SKUS = new String[]{SKU_PREMIUM};

    /** SUBSCRIPTION_SKUS массив строк - артикулы для подписок (ежемесячные, ежегодные)*/
    static final public String[] SUBSCRIPTION_SKUS = new String[]{SKU_INFINITE_GAS_MONTHLY,
            SKU_INFINITE_GAS_YEARLY};
//    static final public String[] SUBSCRIPTION_SKUS = new String[]{};

    /** AUTO_CONSUME_SKUS массив строк - артикулы для АВТОМАТИЧЕСКОГО ПОТРЕБЛЕНИЯ (SKU_GAS)*/
    static final public String[] AUTO_CONSUME_SKUS = new String[]{SKU_GAS};
//    static final public String[] AUTO_CONSUME_SKUS = new String[]{};

    /** billingDataSource - BillingDataSource реализует все функции выставления счетов
     * для нашего тестового приложения*/
    final BillingDataSource billingDataSource;

    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ЗАГОТОВКА ДЛЯ БАЗЫ ДАННЫХ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
/*    *//** gameStateModel - Экзампляр Класса GameStateModel
     * (создает базу данных. Огранизует взаимодействие с БД)*//*
    final GameStateModel gameStateModel;*/

    /** gameMessages - Наблюдаемая с учетом жизненного цикла, которая отправляет только новые
     * обновления после подписки, используется для таких событий, как навигация и сообщения Snackbar*/
    final SingleMediatorLiveEvent<Integer> gameMessages;
    /** allMessages - Наблюдаемая с учетом жизненного цикла, которая отправляет только новые
     * обновления после подписки, используется для таких событий, как навигация и сообщения Snackbar*/
    final SingleMediatorLiveEvent<Integer> allMessages = new SingleMediatorLiveEvent<>();

    /** driveExecutor - ExecutorService открытый интерфейс ExecutorService реализует Executor
     Executor, который предоставляет методы для управления завершением и методы, которые могут
     создавать Future для отслеживания хода выполнения одной или нескольких асинхронных задач*/
    final ExecutorService driveExecutor = Executors.newSingleThreadExecutor();

    /**=========================================================================================
     * Конструктор TrivialDriveRepository - Репозиторий использует данные из источника данных
     * Billing и модели состояния игры вместе, чтобы предоставить унифицированную версию состояния
     * игры для ViewModel
     *
     * @param billingDataSource BillingDataSource реализует все функции выставления счетов для
     *                          нашего тестового приложения. Покупки могут происходить как
     *                          В ПРИЛОЖЕНИИ, так и в любое время ВНЕ ПРИЛОЖЕНИЯ, поэтому
     *                          BillingDataSource должен это учитывать.
     *                          Поскольку каждый SKU может иметь индивидуальное состояние,
     *                          все SKU имеют связанные LiveData, позволяющие наблюдать за их состоянием
     * =========================================================================================
     */
    public Repository(
            BillingDataSource billingDataSource,
            LogFile logFile) {
/*     * @param gameStateModel    Класс GameStateModel - создает базу данных.
     *                          Огранизует взаимодействие с БД    */
/*    public Repository(
                BillingDataSource billingDataSource,
                GameStateModel gameStateModel,
                LogFile logFile) {*/

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
//        logFile.writeLogFile("TrivialDriveRepository: Начало public TrivialDriveRepository");

        this.logFile = logFile;



        // billingDataSource - BillingDataSource реализует все функции выставления счетов для
        // нашего тестового приложения
        this.billingDataSource = billingDataSource;

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ЗАГОТОВКА ДЛЯ БАЗЫ ДАННЫХ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
/*        // gameStateModel - Экзампляр Класса GameStateModel (создает базу данных.
        // Огранизует взаимодействие с БД)
        this.gameStateModel = gameStateModel;*/

        // Создаем новую наблюдаемую
        gameMessages = new SingleMediatorLiveEvent<>();

        // Устанавливает событие, которое мы можем использовать для отправки сообщений в
        // пользовательский интерфейс, который будет использоваться в Snackbars.
        // Этот SingleMediatorLiveEvent отслеживает изменения в SingleLiveEvents,
        // исходящие от остальной части игры, и объединяет их в единый источник с новыми
        // событиями покупки из BillingDataSource.
        setupMessagesSingleMediatorLiveEvent();

        // Поскольку оба связаны с жизненным циклом приложения
        // observeForever - Добавляет данного наблюдателя в список наблюдателей. Этот вызов
        // аналогичен наблюдению (LifecycleOwner, Observer) с LifecycleOwner, который всегда активен.
        // Это означает, что данный наблюдатель получит все события и никогда не будет удален
        // автоматически. Вы должны вручную вызвать removeObserver (Observer), чтобы прекратить
        // наблюдение за этими LiveData. Пока у LiveData есть один из таких наблюдателей,
        // он будет считаться активным.
        // Если наблюдатель уже был добавлен с владельцем к этим LiveData, LiveData выдает
        // исключение IllegalArgumentException.
        // Параметры:
        //      - observer - Наблюдатель, который будет получать события
        billingDataSource.observeConsumedPurchases().observeForever(skuList -> {
/*            // Цикл Для артикула в списке артикулов
            for ( String sku: skuList ) {
                // Если артикул совпадает с SKU_GAS (которое = "gas")
                if (sku.equals(SKU_GAS)) {
                    // incrementGas - Обновляет таблицу GameState, увеличивая value на 1 для строк,
                    //где 'key' = GAS_LEVEL и `value` меньше GAS_TANK_MAX
                    gameStateModel.incrementGas(GAS_TANK_MAX);
                }
            }*/
        });
    }


    /**=========================================================================================
     * Устанавливает событие, которое мы можем использовать для отправки сообщений в
     * пользовательский интерфейс, который будет использоваться в Snackbars.
     * Этот SingleMediatorLiveEvent отслеживает изменения в SingleLiveEvents,
     * исходящие от остальной части игры, и объединяет их в единый источник с новыми
     * событиями покупки из BillingDataSource.
     * <p>
     * Поскольку источник данных биллинга не знает о наших SKU, он также преобразует известные
     * строки SKU в полезные сообщения String.
     * <p>
     * Sets up the event that we can use to send messages up to the UI to be used in Snackbars. This
     * SingleMediatorLiveEvent observes changes in SingleLiveEvents coming from the rest of the game
     * and combines them into a single source with new purchase events from the BillingDataSource.
     * Since the billing data source doesn't know about our SKUs, it also transforms the known SKU
     * strings into useful String messages.
     * =========================================================================================
     */
    void setupMessagesSingleMediatorLiveEvent() {

        /**billingMessages - платежные сообщения*/
        final LiveData<List<String>> billingMessages = billingDataSource.observeNewPurchases();
        // addSource - Начинает прослушивать данный источник LiveData, наблюдатель onChanged будет
        // вызываться при изменении исходного значения.
        // Обратный вызов onChanged будет вызываться только тогда, когда этот MediatorLiveData активен
        // Параметры:
        //      - source - LiveData для прослушивания
        //      - onChanged - наблюдатель, который получит события
        allMessages.addSource(gameMessages, allMessages::setValue);
        allMessages.addSource(billingMessages,
                stringList -> {
                    // TODO: Handle multi-line purchases better (Лучше обрабатывать многострочные покупки)
                    for (String s: stringList) {
                        switch (s) {
/*                            case SKU_GAS:       // Приобрел еще газ!
                                allMessages.setValue(R.string.message_more_gas_acquired);
                                break;*/
                            case SKU_PREMIUM:   // Теперь вы водитель премиум-класса!
                                allMessages.setValue(R.string.message_premium);
                                break;
/*                            case SKU_INFINITE_GAS_MONTHLY:  // Спасибо за подписку! У вас бесконечный газ!
                            case SKU_INFINITE_GAS_YEARLY:   // Спасибо за подписку! У вас бесконечный газ!
                                // this makes sure that upgraded and downgraded subscriptions are
                                // reflected correctly in the app UI
                                // это гарантирует, что обновленные и пониженные подписки правильно
                                // отображаются в пользовательском интерфейсе приложения.
                                // refreshPurchasesAsync - асинхронный запрос покупок
                                billingDataSource.refreshPurchasesAsync();
                                allMessages.setValue(R.string.message_subscribed);
                                break;*/
                        }
                    }
                });
    }

    /**=====================================================================================
     * Автоматическая поддержка обновления / понижения подписки.
     * <p>
     * Automatic support for upgrading/downgrading subscription.
     *
     * @param activity  Требуется библиотекой выставления счетов для запуска платежной
     *                 деятельности в Google Play
     *                 (Needed by billing library to start the Google Play billing activity)
     * @param sku       идентификатор продукта для покупки (the product ID to purchase)
     * =====================================================================================
     */
    public void buySku(Activity activity, String sku) {

        String oldSku = null;
/*        switch (sku) {
            case SKU_INFINITE_GAS_MONTHLY:          // SKU для покупок по подписке
                // (бесконечный газ, ежемесячная подписка)
                oldSku = SKU_INFINITE_GAS_YEARLY;
                break;
            case SKU_INFINITE_GAS_YEARLY:           // SKU для покупок по подписке
                // (бесконечный газ, ежегодная подписка)
                oldSku = SKU_INFINITE_GAS_MONTHLY;
                break;
        }*/

        if ( null != oldSku ) {
            // Запустите процесс биллинга. Это запустит внешнее действие для результата,
            // поэтому для него требуется ссылка на действие.
            // Для подписок он поддерживает обновление с одного типа SKU на другой
            // путем передачи обновляемых SKU.
            billingDataSource.launchBillingFlow(activity, sku, oldSku);
        } else {
            // Запустите процесс биллинга. Это запустит внешнее действие для результата,
            // поэтому для него требуется ссылка на действие.
            // Для подписок он поддерживает обновление с одного типа SKU на другой
            // путем передачи обновляемых SKU.
            billingDataSource.launchBillingFlow(activity, sku);
        }

    }

    /**=====================================================================================
     * Вернуть LiveData, который указывает, куплен ли в данный момент артикул.
     * <p>
     * Return LiveData that indicates whether the sku is currently purchased.
     *
     * @param sku   артикул чтобы получить и наблюдать
     *              (the SKU to get and observe the value for)
     * @return  LiveData, возвращающая истину, если артикул куплен.
     *          (LiveData that returns true if the sku is purchased.)
     * =====================================================================================
     */
    public LiveData<Boolean> isPurchased(String sku) {

        // Возвращает, приобрел ли пользователь артикул.
        // Он делает это, возвращая MediatorLiveData, который возвращает истину,
        // если SKU находится в состоянии PURCHASED (КУПЛЕНО) и покупка была подтверждена.
        return billingDataSource.isPurchased(sku);
    }

    /**=====================================================================================
     * Мы можем купить, если у нас есть хотя бы одна единица газа и покупка не ведется.
     * <p>
     * Для других SKU мы можем приобрести их, если они еще не куплены.
     * <p>
     * Для подписок одновременно следует удерживать только одну из двух, хотя это обеспечивается
     * только бизнес-логикой.
     * <p>
     * We can buy if we have at least one unit of gas and a purchase isn't in progress. For other
     * skus, we can purchase them if they aren't already purchased. For subscriptions, only one of
     * the two should be held at a time, although that is only enforced by business logic.
     *
     * @param sku the product ID to get and observe the value for
     * @return LiveData that returns true if the sku can be purchased
     * =====================================================================================
     */
    public LiveData<Boolean> canPurchase(String sku) {

        switch (sku) {
/*            case SKU_GAS:       // SKU для покупок по продуктов (газ)
            {
                // result - Подкласс LiveData, который может наблюдать за другими объектами
                // LiveData и реагировать на их события OnChanged.
                final MediatorLiveData<Boolean> result = new MediatorLiveData<>();
                // gasTankLevel - Объедините результаты нашей подписки LiveData с нашим уровнем бензобака,
                // чтобы получить наш реальный уровень бензина.
                final LiveData<Integer> gasTankLevel = gasTankLevel();
                // canPurchaseSku - Возвращает, может ли пользователь приобрести артикул.
                // Он делает это, возвращая преобразование LiveData, которое возвращает true,
                // если SKU находится в состоянии UNSPECIFIED, а также если у нас есть skuDetails для SKU.
                final LiveData<Boolean> canPurchaseSku = billingDataSource.canPurchase(sku);
                // Начинает прослушивать данный источник LiveData, наблюдатель onChanged будет
                // вызываться при изменении исходного значения.
                result.addSource(gasTankLevel, level ->
                        // объединить данные о газе и о покупке газа
                        combineGasAndCanPurchaseData(result, gasTankLevel, canPurchaseSku));
                // Начинает прослушивать данный источник LiveData, наблюдатель onChanged будет
                // вызываться при изменении исходного значения.
                result.addSource(canPurchaseSku, canPurchase ->
                        // объединить данные о газе и о покупке газа
                        combineGasAndCanPurchaseData(result, gasTankLevel, canPurchaseSku));
                return result;
            }*/

            case SKU_PREMIUM:       // SKU для покупок премиум
                return billingDataSource.canPurchase(sku);

            default:
                return billingDataSource.canPurchase(sku);
        }

    }

    /**========================================================================================
     * refreshPurchases() - вызывает асинхронный запрос покупок
     * ========================================================================================
     */
    public final void refreshPurchases() {
        billingDataSource.refreshPurchasesAsync();
    }

    /**========================================================================================
     * getBillingLifecycleObserver() - возвращает billingDataSource (реализует все функции
     * выставления счетов для нашего тестового приложения)
     *
     * @return
     * ========================================================================================
     */
    public final LifecycleObserver getBillingLifecycleObserver() {
        return billingDataSource;
    }

    // В SkuDetails много информации, но нашему приложению нужно только несколько вещей,
    // поскольку наши товары никогда не поступают в продажу, имеют начальные цены и т. Д.
    // There's lots of information in SkuDetails, but our app only needs a few things, since our
    // goods never go on sale, have introductory pricing, etc.
    /**========================================================================================
     * getSkuTitle(String sku) - Название нашего SKU от SkuDetails.
     * @param sku
     * @return
     * ========================================================================================
     */
    public final LiveData<String> getSkuTitle(String sku) {
        return billingDataSource.getSkuTitle(sku);
    }

    /**========================================================================================
     * getSkuPrice(String sku) - Цена нашего SKU от SkuDetails.
     * @param sku
     * @return
     * ========================================================================================
     */
    public final LiveData<String> getSkuPrice(String sku) {
        return billingDataSource.getSkuPrice(sku);
    }

    /**========================================================================================
     * getSkuDescription(String sku) - Описание нашего SKU от SkuDetails.
     * @param sku
     * @return
     * ========================================================================================
     */
    public final LiveData<String> getSkuDescription(String sku) {
        return billingDataSource.getSkuDescription(sku);
    }

    /**========================================================================================
     * getMessages() - Возвращает allMessages - Наблюдаемая с учетом жизненного цикла, которая
     * отправляет только новые обновления после подписки, используется для таких событий,
     * как навигация и сообщения Snackbar
     *
     * @return
     * ========================================================================================
     */
    public final LiveData<Integer> getMessages() {
        return allMessages;
    }

    /**======================================================================================
     * sendMessage(int resId) - Посылаем сообщение
     *
     * @param resId Id сообщения
     * ======================================================================================
     */
    public final void sendMessage(int resId) {
        gameMessages.postValue(resId);
    }

    /**======================================================================================
     * getBillingFlowInProcess() - Возвращает billingDataSource.getBillingFlowInProcess()
     * (Возвращает LiveData, который сообщает, обрабатывается ли процесс выставления счетов,
     * что означает, что launchBillingFlow вернул BillingResponseCode.OK,
     * а onPurchasesUpdated еще не был вызван.)
     *
     * @return
     * ======================================================================================
     */
    public final LiveData<Boolean> getBillingFlowInProcess() {
        return billingDataSource.getBillingFlowInProcess();
    }

    /**======================================================================================
     * debugConsumePremium() - вызывает billingDataSource.consumeInappPurchase(SKU_PREMIUM)
     * <p>
     * (Потребляет покупки в приложении. Заинтересованные слушатели могут наблюдать за
     * покупкойConsumed LiveEvent.
     * Чтобы упростить задачу, вы можете отправить список SKU, которые автоматически
     * используются BillingDataSource.)
     *
     * ======================================================================================
     */
    public final void debugConsumePremium() {
        billingDataSource.consumeInappPurchase(SKU_PREMIUM);
    }
}