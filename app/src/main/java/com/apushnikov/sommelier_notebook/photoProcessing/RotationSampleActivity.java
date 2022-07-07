/*
 Copyright 2011, 2012 Chris Banes.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.apushnikov.sommelier_notebook.photoProcessing;

import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_IS_MAIN;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_POSITION;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_TYPE;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_TYPE_PHOTO_ADAPTER;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_TYPE_WINE_SHELF_ADAPTER;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.context_PhotoAdapter;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.context_WineShelfAdapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.apushnikov.sommelier_notebook.MainActivity;
import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.db.AppExecuter;
import com.apushnikov.sommelier_notebook.db.OnDataPhotoFileRotation;
import com.apushnikov.sommelier_notebook.photoview.PhotoView;

import java.io.IOException;

// Запускается класс - (2) Rotation Sample
public class RotationSampleActivity extends AppCompatActivity {

    private static final String TAG = "myLogs";     //Для логов

    private String mCurrentPhotoPath;       // Абсолютный путь к фото-файлу
    private Class<?> contextPhotoAdapter;            // Переданный контекст PhotoAdapter
    private int position;                   // позиция для поворачиваемого фото в PhotoAdapter

    private PhotoView photo;
    private final Handler handler = new Handler();
    private boolean rotating = false;

    // Степень поворота
    //  0 - поворот на 0 градусов
    //  1 - поворот на 90 градусов
    //  2 - поворот на 180 градусов
    //  3 - поворот на 270 градусов
    private int degreeOfRotation = 0;
    // Показывает, изменено ли фото
    private boolean photoHasBeenChanged = false;

    // Тип передаваемого адаптера
    // Передаваемые значения
    //      PHOTO_PROCESSING_TYPE_PHOTO_ADAPTER = 1: передается context_PhotoAdapter
    //      PHOTO_PROCESSING_TYPE_WINE_SHELF_ADAPTER = 2: передается context_WineShelfAdapter
    private int typeAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation_sample);

        // Инициализация toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
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

/*        // Раздуваем меню
        toolbar.inflateMenu(R.menu.rotation);
        // Настройте слушателя для ответа на события щелчка по пункту меню
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_rotate_10_right:
                        photo.setRotationBy(10);
                        return true;
                    case R.id.action_rotate_10_left:
                        photo.setRotationBy(-10);
                        return true;
                    case R.id.action_toggle_automatic_rotation:
                        toggleRotation();
                        return true;
                    case R.id.action_reset_to_0:
                        photo.setRotationTo(0);
                        return true;
                    case R.id.action_reset_to_90:
                        photo.setRotationTo(90);
                        return true;
                    case R.id.action_reset_to_180:
                        photo.setRotationTo(180);
                        return true;
                    case R.id.action_reset_to_270:
                        photo.setRotationTo(270);
                        return true;
                }
                return false;
            }
        });*/

        // Находим Масштабируемый ImageView
        photo = findViewById(R.id.iv_photo);

        // Вернуть намерение, с которого началось это активити
        Intent i = getIntent();
        // Извлекаем из интента полный абсолютный путь к фото файлу
        mCurrentPhotoPath = i.getStringExtra(PHOTO_PROCESSING);
        // Извлекаем из интента - Является ли фото главным (1-является, 0-нет)
        // Является ли фото главным (1-является, 0-нет)
        int mMainPhoto = i.getIntExtra(PHOTO_PROCESSING_IS_MAIN, 0);
        // Если фото является главным
        if (mMainPhoto == 1) {
            // Заголовок toolbar
            toolbar.setTitle(R.string.setTitle_RotationSampleActivity);
        }
        // Извлекаем из интента - тип передаваемого адаптера
        typeAdapter = i.getIntExtra(PHOTO_PROCESSING_TYPE, 0);

        // Вместо того, чтобы брать готовое изображение из drawable, берем из интента
//        photo.setImageResource(R.drawable.wallpaper);
        setPic();

/*        // При передаче интента для обработки фото - контекст для PhotoAdapter для передачи обратногно вызова
        String canName = i.getStringExtra(PHOTO_PROCESSING_CONTEXT);
        try {
            contextPhotoAdapter = Class.forName(canName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/

//        contextPhotoAdapter = (Context) i.getParcelableExtra(PHOTO_PROCESSING_CONTEXT);

        // При передаче интента для обработки фото -  позиция для поворачиваемого фото в PhotoAdapter
        position = i.getIntExtra(PHOTO_PROCESSING_POSITION,0);

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    private void toggleRotation() {
        if (rotating) {
            handler.removeCallbacksAndMessages(null);
        } else {
            rotateLoop();
        }
        rotating = !rotating;
    }

    private void rotateLoop() {
        handler.postDelayed(() -> {
            photo.setRotationBy(1);
            rotateLoop();
        }, 15);
    }

    // ============================================================================
    // Масштабированное изображение и показ во View
    // ============================================================================
    private void setPic() {

        // Получите размеры представления
/*        int targetW = photo.getWidth();
        int targetH = photo.getHeight();*/
/*        int targetW = 1500;
        int targetH = 1500;*/

        // Получить размеры растрового изображения
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        // Если установлено значение true, декодер вернет null (без растрового изображения),
        // но поля out ... будут по-прежнему установлены, позволяя вызывающей стороне запрашивать
        // растровое изображение без необходимости выделять память для его пикселей.
        bmOptions.inJustDecodeBounds = true;

        // Возвращаем без растрового изображения
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

/*        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;*/

        // Определите, насколько уменьшить изображение
//        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));
        int scaleFactor = 2;

        // Декодируйте файл изображения в растровое изображение размером, чтобы заполнить вид
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        // Возвращаем с растровым изображением
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        photo.setImageBitmap(bitmap);

    }

    // ============================================================================
    // Вращение фото вправо
    // ============================================================================
    public void image_Rotate_Right(View view) {

        degreeOfRotation++;
        degreeOfRotation = degreeOfRotation % 4;
        photo.setRotationTo(90*degreeOfRotation);
        if (degreeOfRotation == 0)
        {
            photoHasBeenChanged = false;
        } else
        {
            photoHasBeenChanged = true;
        }

    }

    // ============================================================================
    // Вращение фото влево
    // ============================================================================
    public void image_Rotate_Left(View view) {

        degreeOfRotation--;
        degreeOfRotation = degreeOfRotation % 4;
        photo.setRotationTo(90*degreeOfRotation);
        if (degreeOfRotation == 0)
        {
            photoHasBeenChanged = false;
        } else
        {
            photoHasBeenChanged = true;
        }

    }

    // ============================================================================
    // Сохранение фото
    // ============================================================================
    public void image_Save(View view) {

        saveAndExitPhoto();
    }

    //============================================================================
    // Сохранить и выйти из просмотра фото
    //============================================================================
    public void saveAndExitPhoto() {

        switch (typeAdapter) {

            case PHOTO_PROCESSING_TYPE_PHOTO_ADAPTER: // передается context_PhotoAdapter
                // Поворачивает фото-изображение во втором потоке
                AppExecuter.getInstance().getSubIO().execute(() -> {
                    try {
                        // Устанавливаем, что массив фото по сравнению с начальным состоянием изменился
                        context_PhotoAdapter.setPhotoArrayHasBeenChanged(true);

                        MainActivity.myDbManager.photoFileRotation_Executer(
                                mCurrentPhotoPath,
                                degreeOfRotation,
                                position,
                                (OnDataPhotoFileRotation) context_PhotoAdapter,
                                this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;

            case PHOTO_PROCESSING_TYPE_WINE_SHELF_ADAPTER: // передается context_WineShelfAdapter
                // Поворачивает фото-изображение во втором потоке
                AppExecuter.getInstance().getSubIO().execute(() -> {
                    try {
                        MainActivity.myDbManager.photoFileRotation_Executer(
                                mCurrentPhotoPath,
                                degreeOfRotation,
                                position,
                                (OnDataPhotoFileRotation) context_WineShelfAdapter,
                                this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                break;

            default:
                throw new IllegalStateException("Unexpected value: " + typeAdapter);
        }

        // ВАЖНО - продолжение метода после отработки второго потока
        // в интерфейсе OnDataPhotoFileRotation

        finish();

    }

    //============================================================================
    // Действие по нажатию стрелочки "Назад"
    //============================================================================
    @Override
    public void onBackPressed () {

        if (photoHasBeenChanged)     // Если фото было изменено
        {
            // Используйте класс Builder для удобного построения диалогов
            AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogTheme);
            builder
                    .setTitle(R.string.dialog_exit_editing_photo)    // Заголовок диалога
                    // Позитивная кнопка - Сохранить и выйти
                    .setPositiveButton(R.string.dialog_save_and_exit, (dialog, id) -> {
                        // Сохранить и выйти
                        saveAndExitPhoto();
                    })
                    // Отрицательная кнопка - Выйти без сохранения
                    .setNegativeButton(R.string.dialog_exit_without_saving, (dialog, id) -> {
                        // Выйти без сохранения
                        finish();
                    })
                    // Нейтральная кнопка - Остаться
                    .setNeutralButton(R.string.dialog_stay_here, (dialog, id) -> {
                        // Остаться
                    });
            // Создаем диалог
            AlertDialog alert = builder.create();
            // Показываем диалог
            alert.show();
        }
        else                        // Если фото НЕ было изменено
        {
            finish();
        }

    }

}
