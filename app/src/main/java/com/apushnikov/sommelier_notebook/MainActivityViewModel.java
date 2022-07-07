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
package com.apushnikov.sommelier_notebook;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.apushnikov.sommelier_notebook.log_file.LogFile;
import com.apushnikov.sommelier_notebook.repository.Repository;

//import com.apushnikov.other301_mypurchases.log_file.LogFile;
//import com.apushnikov.other301_mypurchases.repository.Repository;

/**=============================================================================================
 * MainActivityViewModel
 * <p>
 * Это используется для любой бизнес-логики, а также для вывода LiveData из BillingRepository.
 * <p>
 * This is used for any business logic, as well as to echo LiveData from the BillingRepository.
 * =============================================================================================
 */
public class MainActivityViewModel extends ViewModel {

    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    private LogFile logFile;

    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ЗАГОТОВКА ДЛЯ БАЗЫ ДАННЫХ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
/*    static final String TAG = GameViewModel.class.getSimpleName();*/

    /** Репозиторий использует данные из источника данных Billing и модели состояния игры вместе,
     * чтобы предоставить унифицированную версию состояния игры для ViewModel */
    private final Repository repository;

    /**======================================================================================
     * Конструктор MainActivityViewModel
     *
     * @param repository    Репозиторий использует данные из источника данных Billing
     *                                  и модели состояния игры вместе, чтобы предоставить
     *                                  унифицированную версию состояния игры для ViewModel
     * ======================================================================================
     */
    public MainActivityViewModel(Repository repository,
                                 LogFile logFile) {


        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
//        logFile.writeLogFile("MainActivityViewModel: Начало public MainActivityViewModel");
        this.logFile = logFile;

        this.repository = repository;
    }

    /**======================================================================================
     * getMessages() - озвращает allMessages - Наблюдаемая с учетом жизненного цикла,
     * которая отправляет только новые обновления после подписки, используется для таких событий,
     * как навигация и сообщения Snackbar
     *
     * @return
     * ======================================================================================
     */
    public LiveData<Integer> getMessages() {
        return repository.getMessages();
    }

    /**======================================================================================
     * debugConsumePremium() - вызывает
     * billingDataSource.consumeInappPurchase(SKU_PREMIUM)
     * <p>
     * (Потребляет покупки в приложении. Заинтересованные слушатели могут наблюдать за
     * покупкойConsumed LiveEvent. Чтобы упростить задачу, вы можете отправить список SKU,
     * которые автоматически используются BillingDataSource.)
     * ======================================================================================
     */
    public void debugConsumePremium() {
        repository.debugConsumePremium();
    }

    /**======================================================================================
     * getBillingLifecycleObserver() - возвращает billingDataSource (реализует все функции
     * выставления счетов для нашего тестового приложения)
     *
     * @return
     * ======================================================================================
     */
    public LifecycleObserver getBillingLifecycleObserver() {
        return repository.getBillingLifecycleObserver();
    }


    /**=========================================================================================
     * isPremium() - Возвращает true, если куплен премиум
     * (SKU_PREMIUM - SKU для покупок по продуктов (премиум))
     *
     * @return Возвращает LiveData<Boolean>
     * =========================================================================================
     */
    public LiveData<Boolean> isPremium() {
        return repository.isPurchased(Repository.SKU_PREMIUM);
    }


    /**======================================================================================
     * class MainActivityViewModelFactory
     * <p>
     * Factory - Реализации интерфейса Factory отвечают за создание экземпляров ViewModels.
     * ======================================================================================
     */
    public static class MainActivityViewModelFactory implements
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
         * Конструктор MainActivityViewModelFactory
         *
         * @param repository
         * ==========================================================================
         */
        public MainActivityViewModelFactory(Repository repository,
                                            LogFile logFile) {


            /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
             * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             */
//            logFile.writeLogFile("MainActivityViewModel: MainActivityViewModelFactory: Начало public MainActivityViewModelFactory");
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
            if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
                return (T) new MainActivityViewModel(repository,
                        logFile);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
