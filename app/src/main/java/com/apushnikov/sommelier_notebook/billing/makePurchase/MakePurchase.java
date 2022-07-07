package com.apushnikov.sommelier_notebook.billing.makePurchase;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.databinding.ActivityMakePurchaseBinding;
import com.apushnikov.sommelier_notebook.log_file.LogFile;
import com.apushnikov.sommelier_notebook.myApplication.MyApplication;
import com.apushnikov.sommelier_notebook.repository.Repository;

import java.util.ArrayList;
import java.util.List;

/**==========================================================================================
 * Этот фрагмент является просто оберткой для инвентаря (т.е. предметов для продажи).
 * <p>
 * Здесь опять же нет сложной биллинговой логики. Вся логика выставления счетов находится
 * внутри [BillingRepository].
 * <p>
 * [BillingRepository] предоставляет так называемый объект [AugmentedSkuDetails], который
 * показывает, что продается, и разрешено ли пользователю покупать товар в данный момент.
 * Например. если у пользователя уже есть полный бак бензина, он не может покупать газ в
 * данный момент.
 * <p>
 * This Fragment is simply a wrapper for the inventory (i.e. items for sale). Here again there is
 * no complicated billing logic. All the billing logic reside inside the [BillingRepository].
 * The [BillingRepository] provides a so-called [AugmentedSkuDetails] object that shows what
 * is for sale and whether the user is allowed to buy the item at this moment. E.g. if the user
 * already has a full tank of gas, then they cannot buy gas at this moment.
 * ==========================================================================================
 */
public class MakePurchase extends AppCompatActivity {

    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    private LogFile logFile;

    private static final String PLAY_STORE_SUBSCRIPTION_DEEPLINK_URL =
            "https://play.google.com/store/account/subscriptions?sku=%s&package=%s";

    /**makePurchaseViewModel - Это используется для любой бизнес-логики, а также для вывода LiveData
     * из BillingRepository*/
    private MakePurchaseViewModel makePurchaseViewModel;

    /**binding - биндинг, привязка (Binding) к fragment_make_purchase.xml*/
//    private MakePurchaseBinding binding;
    private ActivityMakePurchaseBinding binding;

    /**inventoryList - список базовых реализаций адаптера RecyclerView с представлениями
     * заголовка и содержимого*/
    private final List<MakePurchaseAdapter.Item> inventoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_make_purchase);

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
        this.logFile = ((MyApplication) getApplication()).appContainer.
                repository.logFile;

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
//        logFile.writeLogFile("MakePurchase: Начало public void onCreate");


        // setContentView - Установите представление содержимого Activity в заданный макет и
        // верните связанную привязку. Данный ресурс макета не должен быть макетом слияния.
        // Параметры:
        //       - activity -    Действие, представление содержимого которого должно измениться.
        //       - layoutId -    идентификатор ресурса макета, который будет увеличен, привязан и
        //                       установлен в качестве содержимого Activity.
        binding = DataBindingUtil.setContentView(this, R.layout.activity_make_purchase);

        // Это позволяет привязке данных автоматически наблюдать за любыми LiveData, которые мы передаем.
        binding.setLifecycleOwner(this);

        // Создаем спискок
        makeInventoryList();

        MakePurchaseViewModel.MakePurchaseViewModelFactory makePurchaseViewModelFactory =
                new MakePurchaseViewModel.MakePurchaseViewModelFactory(
                        ((MyApplication) getApplication()).appContainer.repository,
                        logFile);

        makePurchaseViewModel = new ViewModelProvider(this, makePurchaseViewModelFactory).
                get(MakePurchaseViewModel.class);

        binding.setMpvm(makePurchaseViewModel);

        // Установите RecyclerView.LayoutManager, который будет использовать этот RecyclerView.
        binding.inappInventory.setLayoutManager(new LinearLayoutManager(this));

        // Установите новый адаптер для предоставления дочерних представлений по запросу.
        binding.inappInventory.setAdapter(
                new MakePurchaseAdapter(
                        inventoryList,
                        makePurchaseViewModel,
                        this,
                        logFile));

    }


    /**=====================================================================================
     * makeInventoryList() - Создаем спискок
     * <p>
     * Хотя этот список здесь жестко запрограммирован, он может
     * также легко поступать с сервера, что позволяет вам добавлять новые SKU в
     * ваше приложение без необходимости обновлять ваше приложение.
     * <p>
     * While this list is hard-coded here, it could just as easily come from a server, allowing
     * you to add new SKUs to your app without having to update your app.
     * =====================================================================================
     */
    void makeInventoryList() {

/*        inventoryList.add(new MakePurchaseAdapter.Item(
                getText(R.string.header_fuel_your_ride), MakePurchaseAdapter.VIEW_TYPE_HEADER
        ));
        inventoryList.add(new MakePurchaseAdapter.Item(
                TrivialDriveRepository.SKU_GAS, MakePurchaseAdapter.VIEW_TYPE_ITEM
        ));*/
        inventoryList.add(new MakePurchaseAdapter.Item(
                getText(R.string.header_go_premium), MakePurchaseAdapter.VIEW_TYPE_HEADER
        ));
        inventoryList.add(new MakePurchaseAdapter.Item(
                Repository.SKU_PREMIUM, MakePurchaseAdapter.VIEW_TYPE_ITEM
        ));
/*        inventoryList.add(new MakePurchaseAdapter.Item(
                getText(R.string.header_subscribe), MakePurchaseAdapter.VIEW_TYPE_HEADER
        ));
        inventoryList.add(new MakePurchaseAdapter.Item(
                TrivialDriveRepository.SKU_INFINITE_GAS_MONTHLY, MakePurchaseAdapter.VIEW_TYPE_ITEM
        ));
        inventoryList.add(new MakePurchaseAdapter.Item(
                TrivialDriveRepository.SKU_INFINITE_GAS_YEARLY, MakePurchaseAdapter.VIEW_TYPE_ITEM
        ));*/

    }

    /**======================================================================================
     * makePurchase(String sku) - Запускает платежный поток для покупки газа.
     *
     * @param sku
     * ======================================================================================
     */
    public void makePurchase(String sku) {
        makePurchaseViewModel.buySku(this, sku);
    }

    /**======================================================================================
     * onDestroy()
     *
     * ======================================================================================
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }


    /**======================================================================================
     * canBuySku(String sku) - можем ли купить артикул
     * <p>
     * (Мы можем купить, если у нас есть хотя бы одна единица газа и покупка не ведется.)
     * Для других SKU мы можем приобрести их, если они еще не куплены
     * ======================================================================================
     */
    public LiveData<Boolean> canBuySku(String sku) {
        return makePurchaseViewModel.canBuySku(sku);
    }

    /**======================================================================================
     * combineTitleSkuAndIsPurchasedData - объединить название артикула и данные о покупках
     * @param result
     * @param skuTitleLiveData
     * @param isPurchasedLiveData
     * @param sku
     * ======================================================================================
     */
    private void combineTitleSkuAndIsPurchasedData(
            MediatorLiveData<CharSequence> result,
            LiveData<String> skuTitleLiveData,
            LiveData<Boolean> isPurchasedLiveData,
            @NonNull String sku) {

        String skuTitle = skuTitleLiveData.getValue();
        Boolean isPurchased = isPurchasedLiveData.getValue();
        // не отправлять, пока у нас не будут все наши данные
        if (null == skuTitle || null == isPurchased) {
            return;
        }
        // Если  КУПЛЕНО и (артикул равен ежемесячной или годовой подписке)
        if ( isPurchased && ( sku.equals(Repository.SKU_INFINITE_GAS_MONTHLY) ||
                sku.equals(Repository.SKU_INFINITE_GAS_YEARLY))) {
            // добавить URL-адрес в Play Store, чтобы пользователь мог отказаться от подписки,
            // если пользователь уже приобрел подписку
            // SpannableString - Это класс для текста, содержимое которого неизменяемо,
            // но к которому можно прикреплять и отсоединять объекты разметки
            SpannableString titleSpannable = new SpannableString(skuTitle);
            // setSpan - Прикрепите указанный объект разметки к началу диапазона… концу текста
            // или переместите объект в этот диапазон, если он уже был прикреплен в другом месте
            // Параметры:
            //      - what  - Объект
            //      - start - Начало
            //      - end   - Конец
            //      - flags - Флаги (SPAN_EXCLUSIVE_EXCLUSIVE - Промежутки типа
            //                      SPAN_EXCLUSIVE_EXCLUSIVE не расширяются, чтобы включить текст,
            //                      вставленный в их начальной или конечной точке. Они никогда не
            //                      могут иметь длину 0 и автоматически удаляются из буфера,
            //                      если удаляется весь покрываемый ими текст.)
            // URLSpan - Реализация ClickableSpan, позволяющая установить строку url.
            // При выборе и нажатии на текст, к которому прикреплен диапазон, URLSpan попытается
            // открыть URL-адрес, запустив действие с намерением Intent # ACTION_VIEW.
            titleSpannable.setSpan(
                    new URLSpan(String.format(
                            PLAY_STORE_SUBSCRIPTION_DEEPLINK_URL,
                            sku,
                            getPackageName())),
                    0,
                    titleSpannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            result.setValue(titleSpannable);
        }
        // Если  НЕ КУПЛЕНО или (артикул НЕ равен ежемесячной и годовой подписке)
        else {
            // пустой SpannableString необходимо использовать для очистки spannables
            SpannableString titleSpannable = new SpannableString(skuTitle);
            result.setValue(titleSpannable);
        }

    }


    /**======================================================================================
     * skuTitle(final @NonNull String sku)
     *
     * @param sku
     * @return
     * ======================================================================================
     */
    public LiveData<CharSequence> skuTitle(final @NonNull String sku) {

        // skuDetails - получение деталей артикула
        MakePurchaseViewModel.SkuDetails skuDetails = makePurchaseViewModel.getSkuDetails(sku);

        // skuTitleLiveData - Из деталей артикула берем title
        final LiveData<String> skuTitleLiveData = skuDetails.title;
        // куплен ли в данный момент артикул
        final LiveData<Boolean> isPurchasedLiveData = makePurchaseViewModel.isPurchased(sku);
        final MediatorLiveData<CharSequence> result = new MediatorLiveData<>();

        // addSource - Начинает прослушивать данный источник LiveData, наблюдатель onChanged
        // будет вызываться при изменении исходного значения.
        // Начинаем прослушивать skuTitleLiveData (заголовок title)
        result.addSource(skuTitleLiveData, title ->
                // объедининяем название артикула и данные о покупках
                combineTitleSkuAndIsPurchasedData(result, skuTitleLiveData, isPurchasedLiveData, sku ));
        // Начинаем прослушивать isPurchasedLiveData (куплен ли в данный момент артикул)
        result.addSource(isPurchasedLiveData, isPurchased ->
                // объедининяем название артикула и данные о покупках
                combineTitleSkuAndIsPurchasedData(result, skuTitleLiveData, isPurchasedLiveData, sku ));

        return result;
    }
}
