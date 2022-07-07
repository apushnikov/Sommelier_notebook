/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apushnikov.sommelier_notebook.billing.makePurchase;

import static com.apushnikov.sommelier_notebook.repository.Repository.SKU_PREMIUM;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.log_file.LogFile;
import com.apushnikov.sommelier_notebook.repository.Repository;

import java.util.HashMap;
import java.util.Map;

/**=============================================================================================
 * MakePurchaseViewModel
 * <p>
 * Это используется для любой бизнес-логики, а также для вывода LiveData из BillingRepository.
 * <p>
 * This is used for any business logic, as well as to echo LiveData from the BillingRepository.
 * =============================================================================================
 */
public class MakePurchaseViewModel extends ViewModel {


    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    private LogFile logFile;

    static final String TAG = MakePurchaseViewModel.class.getSimpleName();

    /**skuToResourceIdMap - отображение артикулов на ресурсы*/
    static final private Map<String, Integer> skuToResourceIdMap = new HashMap<>();

    static {
//        skuToResourceIdMap.put(SKU_GAS, R.drawable.buy_gas);
        skuToResourceIdMap.put(SKU_PREMIUM, R.drawable.upgrade_app);
//        skuToResourceIdMap.put(SKU_INFINITE_GAS_MONTHLY, R.drawable.get_infinite_gas);
//        skuToResourceIdMap.put(SKU_INFINITE_GAS_YEARLY, R.drawable.get_infinite_gas);
    }

    /** Репозиторий использует данные из источника данных Billing и модели состояния игры вместе,
     * чтобы предоставить унифицированную версию состояния игры для ViewModel */
    private final Repository repository;

    /**======================================================================================
     * Конструктор MakePurchaseViewModel
     *
     * @param repository    Репозиторий использует данные из источника данных Billing
     *                                  и модели состояния игры вместе, чтобы предоставить
     *                                  унифицированную версию состояния игры для ViewModel
     * ======================================================================================
     */
    public MakePurchaseViewModel(@NonNull Repository repository,
                                 LogFile logFile) {
        super();

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
//        logFile.writeLogFile("MakePurchaseViewModel: Начало public MakePurchaseViewModel");
        this.logFile = logFile;

        this.repository = repository;
    }

    /**======================================================================================
     * getSkuDetails(String sku) - получение деталей артикула
     *
     * @param sku   артикул
     * @return
     * ======================================================================================
     */
    public SkuDetails getSkuDetails(String sku) {
        return new SkuDetails(sku, repository);
    }

    /**======================================================================================
     * canBuySku(String sku) - можем ли купить артикул (Мы можем купить, если у нас есть хотя бы
     * одна единица газа и покупка не ведется.)
     * Для других SKU мы можем приобрести их, если они еще не куплены
     *
     * @param sku   артикул
     * @return
     * ======================================================================================
     */
    public LiveData<Boolean> canBuySku(String sku) {
        return repository.canPurchase(sku);
    }

    /**======================================================================================
     * isPurchased(String sku) - куплен ли в данный момент артикул
     *
     * @param sku   артикул
     * @return
     * ======================================================================================
     */
    public LiveData<Boolean> isPurchased(String sku) {
        return repository.isPurchased(sku);
    }

    /**======================================================================================
     * Запускает платежный поток для покупки газа.
     * <p>
     * Starts a billing flow for purchasing gas.
     *
     * @param activity needed by Billing library to launch the purchase Activity
     * ======================================================================================
     */
    public void buySku(Activity activity, String sku) {
        repository.buySku(activity, sku);
    }

    /**======================================================================================
     * getBillingFlowInProcess() - Возвращает billingDataSource.getBillingFlowInProcess()
     * (Возвращает LiveData, который сообщает, обрабатывается ли процесс выставления счетов,
     * что означает, что launchBillingFlow вернул BillingResponseCode.OK, а onPurchasesUpdated
     * еще не был вызван.)
     *
     * @return
     * ======================================================================================
     */
    public LiveData<Boolean> getBillingFlowInProcess() {
        return repository.getBillingFlowInProcess();
    }

    /**======================================================================================
     * sendMessage(int message) - посылаем сообщение
     *
     * @param message   сообщение
     * ======================================================================================
     */
    public void sendMessage(int message) {
        repository.sendMessage(message);
    }

    /**======================================================================================
     * class SkuDetails - детали артикула (строка, заголовок, описание, цена, иконка)
     * ======================================================================================
     */
    static public class SkuDetails {

        /**sku - строка артикула*/
        final public String sku;
        /**sku - заголовок артикула*/
        final public LiveData<String> title;
        /**sku - описание артикула*/
        final public LiveData<String> description;
        /**sku - цена артикула*/
        final public LiveData<String> price;
        /**sku - иконка артикула*/
        final public int iconDrawableId;

        /**==================================================================================
         * Конструктор класса SkuDetails
         *
         * @param sku
         * @param repository
         * ==================================================================================
         */
        SkuDetails(@NonNull String sku, Repository repository) {
            this.sku = sku;
            title = repository.getSkuTitle(sku);
            description = repository.getSkuDescription(sku);
            price = repository.getSkuPrice(sku);
            iconDrawableId = skuToResourceIdMap.get(sku);
        }
    }

    /**======================================================================================
     * class MakePurchaseViewModelFactory
     * <p>
     * Factory - Реализации интерфейса Factory отвечают за создание экземпляров ViewModels.
     * ======================================================================================
     */
    public static class MakePurchaseViewModelFactory implements
            ViewModelProvider.Factory {


        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
        private LogFile logFile;

        /**Экземпляр TrivialDriveRepository (Репозиторий использует данные из источника данных Billing
         * и модели состояния игры вместе, чтобы предоставить унифицированную версию состояния
         * игры для ViewModel)*/
        private final Repository repository;

        /**==========================================================================
         * Конструктор MakePurchaseViewModelFactory
         *
         * @param repository
         * ==========================================================================
         */
        public MakePurchaseViewModelFactory(Repository repository,
                                            LogFile logFile) {


            /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
             * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             */
//            logFile.writeLogFile("MakePurchaseViewModel: MakePurchaseViewModelFactory: Начало public MakePurchaseViewModelFactory");
            this.logFile = logFile;

            this.repository = repository;
        }

        /**==========================================================================
         * create
         *
         * @param modelClass
         * @param <T>
         * @return
         * ==========================================================================
         */
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MakePurchaseViewModel.class)) {
                return (T) new MakePurchaseViewModel(repository,
                        logFile);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
