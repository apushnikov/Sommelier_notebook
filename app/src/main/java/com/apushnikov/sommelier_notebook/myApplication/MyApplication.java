package com.apushnikov.sommelier_notebook.myApplication;

import android.app.Application;

import androidx.room.Room;

//import com.apushnikov.sommelier_notebook.db.App;
import com.apushnikov.sommelier_notebook.billing.BillingDataSource;
import com.apushnikov.sommelier_notebook.db.AppDatabase;
import com.apushnikov.sommelier_notebook.log_file.LogFile;
import com.apushnikov.sommelier_notebook.repository.Repository;

/**========================================================================================
 * MyApplication - Базовый класс для поддержания глобального состояния приложения.
 * <p>
 * Вы можете предоставить свою собственную реализацию, создав подкласс и указав полное имя
 * этого подкласса в качестве атрибута «android: name» в теге <application> вашего
 * AndroidManifest.xml.
 * <p>
 * Класс Application или ваш подкласс класса Application создается перед любым другим классом
 * при создании процесса для вашего приложения / пакета.
 * ========================================================================================
 */
public class MyApplication extends Application {

    /**appContainer - Контейнер объектов, общий для всего приложения*/
    public AppContainer appContainer;


    // TODO: перенести операции с базами данных в GameStateModel
    private AppDatabase database;



    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    private LogFile logFile;

    /**====================================================================================
     * AppContainer - Контейнер объектов, общий для всего приложения
     * <p>
     * Создает объекты:
     * <p>
     * - ???? GameStateModel - Работа с базой данных
     * <p>
     * - BillingDataSource - реализует все функции выставления счетов для нашего тестового приложения
     * <p>
     * - ??? MyRepository - Репозиторий использует данные из источника данных Billing и
     * модели состояния игры вместе, чтобы предоставить унифицированную версию
     * состояния игры для ViewModel
     * ====================================================================================
     */
    public class AppContainer {

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
        private LogFile logFile;

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ЗАГОТОВКА ДЛЯ БАЗЫ ДАННЫХ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
//        final GameStateModel gameStateModel;

        /** billingDataSource - BillingDataSource реализует все функции выставления
         * счетов для нашего тестового приложения */
        final BillingDataSource billingDataSource;

        /** repository - Репозиторий использует данные из источника данных Billing и
         * модели состояния игры вместе, чтобы предоставить унифицированную версию состояния
         * игры для ViewModel
         */
        final public Repository repository;

        /**====================================================================================
         * AppContainer - конструктор
         *
         * @param logFile Это временный параметр для тестирования
         * ====================================================================================
         */
        public AppContainer(LogFile logFile) {

            /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
             * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             */
            this.logFile = logFile;

            /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             * ЭТО ЗАГОТОВКА ДЛЯ БАЗЫ ДАННЫХ
             * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             */
/*            gameStateModel = new GameStateModel(
                    MyApplication.this,
                    logFile);*/

            billingDataSource = BillingDataSource.getInstance(
                    MyApplication.this,
                    Repository.INAPP_SKUS,
                    Repository.SUBSCRIPTION_SKUS,
                    Repository.AUTO_CONSUME_SKUS,
                    logFile);

            /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             * ЭТО ЗАГОТОВКА ДЛЯ БАЗЫ ДАННЫХ
             * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             */
/*            repository = new Repository(
                    billingDataSource,
                    gameStateModel,
                    logFile);*/
            repository = new Repository(
                    billingDataSource,
                    logFile);
        }
    }

    /**====================================================================================
     * Создаем объект AppContainer
     * ====================================================================================
     */
    @Override
    public void onCreate() {
        super.onCreate();

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
        logFile = new LogFile(this);

        appContainer = new AppContainer(logFile);

        // TODO: перенести операции с базами данных в GameStateModel
        // Database объект - это стартовая точка. Его создание выглядит так:
        this.database = Room.databaseBuilder(this, AppDatabase.class, "winedatabase.db")
                .build();

    }

/*    public AppContainer getAppContainer() {
        return appContainer;
    }*/

    public AppDatabase getDatabase() {
        return database;
    }


}
