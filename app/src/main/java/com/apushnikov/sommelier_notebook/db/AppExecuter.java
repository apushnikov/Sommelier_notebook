package com.apushnikov.sommelier_notebook.db;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**============================================================================================
 * AppExecuter - для запуска во втором и основном потоках
 * <p>
 * Экзекьютор, запускается только один раз
 */
public class AppExecuter {
    private static AppExecuter instance;
    private final Executor mainIO;          // Основной поток
    private final Executor subIO;           // Второй поток

    //=================================================================
    // Конструктор
    //=================================================================
    public AppExecuter(Executor mainIO, Executor subIO) {
        this.mainIO = mainIO;
        this.subIO = subIO;
    }

    //=================================================================
    // Создает инстанс AppExecuter - только один раз
    //=================================================================
    public static AppExecuter getInstance() {

        if (instance == null) instance = new AppExecuter(
                new MainThreadHandler(),
                Executors.newSingleThreadExecutor());
        return instance;
    }

    //=================================================================
    // MainThreadHandler - запускает на основном потоке то, что нам нужно
    // Мост между второстепенным потоком и основным потоком
    //=================================================================
    public static class MainThreadHandler implements Executor {
        private final Handler mainHandler = new Handler(Looper.getMainLooper());


        @Override
        public void execute(Runnable command) {
            mainHandler.post(command);

        }
    }

    //=================================================================
    // Геттеры
    //=================================================================
    public Executor getMainIO() {
        return mainIO;
    }

    public Executor getSubIO() {
        return subIO;
    }
}
