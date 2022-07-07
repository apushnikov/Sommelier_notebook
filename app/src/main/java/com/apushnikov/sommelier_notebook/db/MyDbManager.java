package com.apushnikov.sommelier_notebook.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import com.apushnikov.sommelier_notebook.MainActivity;
import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.ui.adapterFhotoList.PhotoAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MyDbManager {

    private static final String TAG = "myLogs";     //Для логов

    //OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
    // Методы для работы с винными полками
    //OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO

    //=============================================================================
    // ПРОСМОТР
    // Получаем список винных полок во втором потоке
    // Использует метод getAll из wineShelfDao
    // - Сначала обращаемся к базе данных
    // - Потом вызывает через интерфейс обновление адаптера
    // - Метод запускается во втором потоке
    //=============================================================================
    public void getListWineShelf_Executer(OnDataReceivedListWineShelf onDataReceivedListWineShelf){

        List<WineShelf> tempList;
        // Метод getAll позволяют получить полный список винных полок
        tempList = MainActivity.wineShelfDao.getAll();
        // Как только закончили считывать данные, вызываем интерфейс для обновления адаптера
        onDataReceivedListWineShelf.onReceivedListWineShelf(tempList);
    }

    //=============================================================================
    // ВСТАВКА
    // Вставляем винную полку во втором потоке
    // Использует метод insert из wineShelfDao
    // - Сначала обращаемся к базе данных
    // - Потом вызывает через интерфейс обновление адаптера
    // - Метод запускается во втором потоке
    //=============================================================================
    @SuppressLint("ResourceType")
    public void insertWineShelf_Executer(
            Context context,
            WineShelf wineShelf,
            OnDataInsertedWineShelf onDataInsertedWineShelf)
            throws IOException {

        // Случайный выбор картинки винных полок
        int i = randomSelectionOfWineShelves();
        InputStream is;
        switch (i) {
            case 1:
                is = context.getResources().openRawResource(R.drawable.wine_shift_01);
                break;
            case 2:
                is = context.getResources().openRawResource(R.drawable.wine_shift_02);
                break;
            case 3:
                is = context.getResources().openRawResource(R.drawable.wine_shift_03);
                break;
            case 4:
                is = context.getResources().openRawResource(R.drawable.wine_shift_04);
                break;
            case 5:
                is = context.getResources().openRawResource(R.drawable.wine_shift_05);
                break;
            case 6:
                is = context.getResources().openRawResource(R.drawable.wine_shift_06);
                break;
            case 7:
                is = context.getResources().openRawResource(R.drawable.wine_shift_07);
                break;
            case 8:
                is = context.getResources().openRawResource(R.drawable.wine_shift_08);
                break;
            case 9:
                is = context.getResources().openRawResource(R.drawable.wine_shift_09);
                break;
            case 10:
                is = context.getResources().openRawResource(R.drawable.wine_shift_10);
                break;
            case 11:
                is = context.getResources().openRawResource(R.drawable.wine_shift_11);
                break;
            case 12:
                is = context.getResources().openRawResource(R.drawable.wine_shift_12);
                break;
            case 13:
                is = context.getResources().openRawResource(R.drawable.wine_shift_13);
                break;
            case 14:
                is = context.getResources().openRawResource(R.drawable.wine_shift_14);
                break;
            case 15:
                is = context.getResources().openRawResource(R.drawable.wine_shift_15);
                break;
            case 16:
                is = context.getResources().openRawResource(R.drawable.wine_shift_16);
                break;
            case 17:
                is = context.getResources().openRawResource(R.drawable.wine_shift_17);
                break;
            case 18:
                is = context.getResources().openRawResource(R.drawable.wine_shift_18);
                break;
            case 19:
                is = context.getResources().openRawResource(R.drawable.wine_shift_19);
                break;
            case 20:
                is = context.getResources().openRawResource(R.drawable.wine_shift_20);
                break;
            case 21:
                is = context.getResources().openRawResource(R.drawable.wine_shift_21);
                break;
            case 22:
                is = context.getResources().openRawResource(R.drawable.wine_shift_22);
                break;
            default:
                is = context.getResources().openRawResource(R.drawable.wine_shift_01);
        }

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

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Генерируем имя фото-файла
        File newFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Копируем картинку
        try {
            // TODO: Действия - если внешнее хранилище в настоящее время не подключено
            // (Очень простой код для копирования изображения из ресурса приложения во
            // внешний файл. Обратите внимание, что этот код не проверяет ошибки и предполагает,
            // что изображение маленькое (не пытается копировать его по частям).
            // Обратите внимание, что если внешнее хранилище в настоящее время не подключено,
            // это не сработает.)
            OutputStream os = new FileOutputStream(newFile);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            // (Невозможно создать файл, вероятно, потому что внешнее хранилище в
            // настоящее время не подключено)
            Log.w("ExternalStorage", "Error writing " + newFile, e);
        }

        // Вставляем путь к фото-файлу в винную полку
        wineShelf.setPhotoAbsolutePathShelf(newFile.getAbsolutePath());

        long ShelfId;
        // Метод insert для вставки
        ShelfId = MainActivity.wineShelfDao.insert(wineShelf);
        // Как только закончили вставлять, вызываем интерфейс для обновления адаптера
        onDataInsertedWineShelf.onInsertedWineShelf(ShelfId, wineShelf);
    }

    //=============================================================================
    // ОБНОВЛЕНИЕ
    // Обновляем винную полку во втором потоке
    // Использует метод update из wineShelfDao
    // - Сначала обращаемся к базе данных
    // - Потом вызывает через интерфейс обновление адаптера
    // - Метод запускается во втором потоке
    //=============================================================================
    public void updateWineShelfJustSave_Executer(
            WineShelf wineShelf){

        //Обновляем винную полку
        MainActivity.wineShelfDao.update(wineShelf);
    }

    //=============================================================================
    // ОБНОВЛЕНИЕ
    // Обновляем винную полку во втором потоке
    // Использует метод update из wineShelfDao
    // - Сначала обращаемся к базе данных
    // - Потом вызывает через интерфейс обновление адаптера
    // - Метод запускается во втором потоке
    //=============================================================================
    public void updateWineShelf_Executer(
            WineShelf wineShelf,
            OnDataUpdatedWineShelf onDataUpdatedWineShelf){

        //Обновляем винную полку
        MainActivity.wineShelfDao.update(wineShelf);

        // Как только закончили обновлять, вызываем для последующих действий
        onDataUpdatedWineShelf.onUpdatedWineShelf();

    }

    //=============================================================================
    // ОБНОВЛЕНИЕ с вставкой картинки
    // Обновляем винную полку во втором потоке
    // Использует метод update из wineShelfDao
    // - Сначала обращаемся к базе данных
    // - Потом вызывает через интерфейс обновление адаптера
    // - Метод запускается во втором потоке
    //=============================================================================
    @SuppressLint("ResourceType")
    public void updateWineShelfPicture_Executer(
            Context context,
            WineShelf wineShelf,
            int indexPicrure,
            int position,
            OnDataUpdatedWineShelfPicture onDataUpdatedWineShelfPicture) throws IOException {

        // TODO: код практически копирует insertWineShelf_Executer

        // Выбираем картинку винных полок по переданному indexPicrure
        InputStream is;
        switch (indexPicrure) {
            case 1:
                is = context.getResources().openRawResource(R.drawable.wine_shift_01);
                break;
            case 2:
                is = context.getResources().openRawResource(R.drawable.wine_shift_02);
                break;
            case 3:
                is = context.getResources().openRawResource(R.drawable.wine_shift_03);
                break;
            case 4:
                is = context.getResources().openRawResource(R.drawable.wine_shift_04);
                break;
            case 5:
                is = context.getResources().openRawResource(R.drawable.wine_shift_05);
                break;
            case 6:
                is = context.getResources().openRawResource(R.drawable.wine_shift_06);
                break;
            case 7:
                is = context.getResources().openRawResource(R.drawable.wine_shift_07);
                break;
            case 8:
                is = context.getResources().openRawResource(R.drawable.wine_shift_08);
                break;
            case 9:
                is = context.getResources().openRawResource(R.drawable.wine_shift_09);
                break;
            case 10:
                is = context.getResources().openRawResource(R.drawable.wine_shift_10);
                break;
            case 11:
                is = context.getResources().openRawResource(R.drawable.wine_shift_11);
                break;
            case 12:
                is = context.getResources().openRawResource(R.drawable.wine_shift_12);
                break;
            case 13:
                is = context.getResources().openRawResource(R.drawable.wine_shift_13);
                break;
            case 14:
                is = context.getResources().openRawResource(R.drawable.wine_shift_14);
                break;
            case 15:
                is = context.getResources().openRawResource(R.drawable.wine_shift_15);
                break;
            case 16:
                is = context.getResources().openRawResource(R.drawable.wine_shift_16);
                break;
            case 17:
                is = context.getResources().openRawResource(R.drawable.wine_shift_17);
                break;
            case 18:
                is = context.getResources().openRawResource(R.drawable.wine_shift_18);
                break;
            case 19:
                is = context.getResources().openRawResource(R.drawable.wine_shift_19);
                break;
            case 20:
                is = context.getResources().openRawResource(R.drawable.wine_shift_20);
                break;
            case 21:
                is = context.getResources().openRawResource(R.drawable.wine_shift_21);
                break;
            case 22:
                is = context.getResources().openRawResource(R.drawable.wine_shift_22);
                break;
            default:
                is = context.getResources().openRawResource(R.drawable.wine_shift_01);
        }

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

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Генерируем имя фото-файла
        File newFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Копируем картинку
        try {
            // TODO: Действия - если внешнее хранилище в настоящее время не подключено
            // (Очень простой код для копирования изображения из ресурса приложения во
            // внешний файл. Обратите внимание, что этот код не проверяет ошибки и предполагает,
            // что изображение маленькое (не пытается копировать его по частям).
            // Обратите внимание, что если внешнее хранилище в настоящее время не подключено,
            // это не сработает.)
            OutputStream os = new FileOutputStream(newFile);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            // (Невозможно создать файл, вероятно, потому что внешнее хранилище в
            // настоящее время не подключено)
            Log.w("ExternalStorage", "Error writing " + newFile, e);
        }

        // Запоминаем старую картинку для последующего удаления
        String oldPhotoAbsolutePathShelf = wineShelf.getPhotoAbsolutePathShelf();

        // Вставляем путь к фото-файлу в винную полку
        wineShelf.setPhotoAbsolutePathShelf(newFile.getAbsolutePath());

        //Обновляем винную полку
        MainActivity.wineShelfDao.update(wineShelf);

        // Как только закончили обновлять, вызываем для последующих действий
        onDataUpdatedWineShelfPicture.onUpdatedWineShelfPicture(position,newFile.getAbsolutePath());

        // TODO: проверить все файловые операции - что делать, если внешнее хранилище не подключено
        // Удаляем старый файл
        // (Получить путь к файлу на внешнем хранилище. Если внешнее хранилище в настоящее
        // время не подключено, это не удастся)
        File file = new File(oldPhotoAbsolutePathShelf);
        if (file.exists()) {
            file.delete();
        }

    }

    //=============================================================================
    // ОБНОВЛЕНИЕ с вставкой пути к фотофайлу
    // Обновляем винную полку во втором потоке
    // Использует метод update из wineShelfDao
    // - Сначала обращаемся к базе данных
    // - Потом вызывает через интерфейс обновление адаптера
    // - Метод запускается во втором потоке
    //=============================================================================
    @SuppressLint("ResourceType")
    public void updateWineShelfAbsolutePath_Executer(
            Context context,
            WineShelf wineShelf,
            String photoAbsolutePathShelf,
            int position,
            OnDataUpdatedWineShelfPicture onDataUpdatedWineShelfPicture) throws IOException {

        // TODO: код практически копирует insertWineShelf_Executer

        // Запоминаем старую картинку для последующего удаления
        String oldPhotoAbsolutePathShelf = wineShelf.getPhotoAbsolutePathShelf();

        // Вставляем путь к фото-файлу в винную полку
        wineShelf.setPhotoAbsolutePathShelf(photoAbsolutePathShelf);

        //Обновляем винную полку
        MainActivity.wineShelfDao.update(wineShelf);

        // Как только закончили обновлять, вызываем для последующих действий
        onDataUpdatedWineShelfPicture.onUpdatedWineShelfPicture(position,photoAbsolutePathShelf);

        // TODO: проверить все файловые операции - что делать, если внешнее хранилище не подключено
        // Удаляем старый файл
        // (Получить путь к файлу на внешнем хранилище. Если внешнее хранилище в настоящее
        // время не подключено, это не удастся)
        File file = new File(oldPhotoAbsolutePathShelf);
        if (file.exists()) {
            file.delete();
        }

    }


    //============================================================================
    // УДАЛЕНИЕ
    // Удаляем винную полку в базе данных по номеру винной полки во втором потоке
    // - Удаляем все вина, принадлежащие винной полке
    // - Потом удаляем саму винную полку
    // - Метод запускается во втором потоке
    //============================================================================
    public void deleteWineShelf_Executer(long mShelfId) {

        // Удаляем изображения, относящиеся ко всем винам в винной полке
        List<Photo> photoArray = MainActivity.photoDao.getByShelfId(mShelfId);
        if (photoArray.size() > 0)        // Если есть изображения
        {
            for (Photo photo : photoArray)
            {
                if (!photo.getPhotoAbsolutePath().isEmpty())
                {
                    File file = new File(photo.getPhotoAbsolutePath()); //Создаем файловую переменную
                    //Если файл существует, уничтожаем его
                    if (file.exists()) file.delete();
                }
            }
        }

        // Метод getById позволяет получить винную полку по id
        WineShelf wineShelf = MainActivity.wineShelfDao.getById(mShelfId);

        // Удаляем фото винной полки
        if (!wineShelf.getPhotoAbsolutePathShelf().isEmpty())
        {
            File file = new File(wineShelf.getPhotoAbsolutePathShelf()); //Создаем файловую переменную
            //Если файл существует, уничтожаем его
            if (file.exists()) file.delete();
        }

        //Удаляем винную полку
        // При этом каскадно удаляются вина и фото, принадлежащие всем винам в этой винной полке
        MainActivity.wineShelfDao.delete(wineShelf);

    }

    //OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
    // Методы для работы с вином
    //OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO

    //=============================================================================
    // ПРОСМОТР
    // Получаем список вин по номеру винной полки shelfId во втором потоке
    // Использует метод getByShelfId из WineDao.
    // - Сначала обращаемся к базе данных
    // - Потом вызывает через интерфейс обновление адаптера
    // - Метод запускается во втором потоке
    //=============================================================================
    public void getListWineByShelfId_Executer(long shelfId, OnDataReceivedListWine onDataReceivedListWine){

        List<Wine> tempList = null;
        // Метод getByShelfId позволяет получить список вин по номеру винной полки shelfId.
        tempList = (List<Wine>) MainActivity.wineDao.getByShelfId(shelfId);
        // Как только закончили считывать данные, вызываем интерфейс для обновления адаптера
        onDataReceivedListWine.onReceivedListWine(tempList);
    }

    //=============================================================================
    // ВСТАВКА ИЛИ ОБНОВЛЕНИЕ
    // Вставляем или обновляем вино во втором потоке
    // Использует методы из wineDao
    // - Сначала обращаемся к базе данных
    // - Потом вызывает через интерфейс обновление адаптера
    // - Метод запускается во втором потоке
    //=============================================================================
    public void insertOrUpdateWine_Executer(
            boolean isEditState,
            Wine wine,
            PhotoAdapter photoAdapter,
            OnDataInsertOrUpdateWine onDataInsertOrUpdateWine){

        // если было редактирования вина
        if (isEditState) {
            // Сохраняем имеющееся вино
            MainActivity.wineDao.update(wine);
            // Если массив фото по сравнению изменился
            if (photoAdapter.getPhotoArrayHasBeenChanged()) {
                // Обновляем массив фото для вина в базе данных (старое стираем, вставляем новое)
                updatePhotoArrayInDb(photoAdapter.getPhotoArray(), photoAdapter.getPhotoArrayOld());
            }
        }
        // если было создание нового  вина
        else {
            // Вставляем новое вино и запоминаем новый Номер винной карточки
            long wineId = MainActivity.wineDao.insert(wine);
            // Если массив фото по сравнению изменился
            if (photoAdapter.getPhotoArrayHasBeenChanged()) {
                // Вставляем массив фото для нового вина wineId
                insertPhotoArrayInDb(wineId, photoAdapter.getPhotoArray());
            }
        }
        // Как только закончили вставлять или обновлять, вызываем для последующих действий
        onDataInsertOrUpdateWine.onInsertOrUpdateWine();
    }

    //=============================================================================
    // УДАЛЕНИЕ
    // Удаляем вино во втором потоке
    // Использует методы из wineDao
    // - Сначала обращаемся к базе данных
    // - Потом вызывает через интерфейс обновление адаптера
    // - Метод запускается во втором потоке
    //=============================================================================
    public void deleteWine_Executer(long mWineId){

        // Удаляем изображения, относящиеся к удаляемому вину
        List<Photo> photoArray = MainActivity.photoDao.getByWineId(mWineId);
        if (photoArray.size() > 0)        // Если есть изображения
        {
            for (Photo photo : photoArray)
            {
                if (!photo.getPhotoAbsolutePath().isEmpty())
                {
                    File file = new File(photo.getPhotoAbsolutePath()); //Создаем файловую переменную
                    //Если файл существует, уничтожаем его
                    if (file.exists()) file.delete();
                }
            }
        }

        // Удаляем вино из базы данных
        // При этом каскадно удаляются фото, принадлежащие вину
        Wine wine = new Wine();
        wine.setId(mWineId);
        MainActivity.wineDao.delete(wine);

        // Как только закончили удалять, ничего не вызываем
    }




    //OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
    // Методы для работы с фотографиями
    //OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO

    //=============================================================================
    // ПРОСМОТР
    // Получаем массив фотографий по номеру вина wineId во втором потоке
    // Использует метод getByWineId из photoDao.
    // - Сначала обращаемся к базе данных
    // - Потом вызывает через интерфейс обновление адаптера
    // - Метод запускается во втором потоке
    //=============================================================================
    public void getListPhotoByWineId_Executer(long wineId, OnDataReceivedListPhoto onDataReceivedListPhoto){

        List<Photo> tempList = null;
        // Метод getByWineId позволяет получить список фотографий по номеру винна wineId.
        tempList = (List<Photo>) MainActivity.photoDao.getByWineId(wineId);
        // Как только закончили считывать данные, вызываем интерфейс для обновления адаптера
        onDataReceivedListPhoto.onReceivedListPhoto(tempList);
    }

    //============================================================================
    // Обновляем массив фото для вина в базе данных (старое стираем, вставляем новое)
    // Этот метод выполняется во втором потоке
    //============================================================================
    private void updatePhotoArrayInDb(List<Photo> photoArray, List<Photo> photoArrayOld) {
        // Удаляем изображения из старого массива, которые не присутсвуют в новом массиве
        deleteWithImages(photoArray, photoArrayOld);
        // Удаляем старые фото из базы данных photoArrayOld
        MainActivity.photoDao.delete(photoArrayOld);
        // Вставляем массив фото в базу photoArray
        MainActivity.photoDao.insert(photoArray);
    }

    //============================================================================
    // Вставляем массив фото для нового вина wineId
    // Этот метод выполняется во втором потоке
    //============================================================================
    private void insertPhotoArrayInDb(long wineId, List<Photo> photoArray) {
        // Если массив фото не пуст
        if (photoArray.size()>0) {
            // Проходимся по массиву фото и вставляем номер вина
            for (Photo photo: photoArray) {
                photo.setWineId(wineId);
            }
            // Вставляем массив фото в базу photoArray
            MainActivity.photoDao.insert(photoArray);
        }
    }

    //OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
    // Методы для работы с файловыми изображениями
    //OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO

    //============================================================================
    // Удаляем изображения из старого фотографий массива, которые не присутсвуют в новом массиве
    // Параметры:
    //  - photoArrayOld - старый массив
    //  - photoArray - новый массив
    //============================================================================
    private void deleteWithImages(List<Photo> photoArray, List<Photo> photoArrayOld) {

        for (Photo photoOld : photoArrayOld)
        {
            boolean shouldBeRemoved = true;
            for (Photo photo : photoArray)
            {
                if (photoOld.getPhotoAbsolutePath() == photo.getPhotoAbsolutePath())
                {
                    shouldBeRemoved = false;
//                    return;
                }
            }
            if (shouldBeRemoved)
            {
                if (!photoOld.getPhotoAbsolutePath().isEmpty())
                {
                    //Создаем файловую переменную
                    File file = new File(photoOld.getPhotoAbsolutePath());
                    //Если файл существует, уничтожаем его
                    if (file.exists()) file.delete();
                }
            }
        }
    }

    //============================================================================
    // Поворачивает фото-изображение
    // Параметры:
    //      - mCurrentPhotoPath - Абсолютный путь к фото-файлу
    //      - degreeOfRotation - Степень поворота
    //              0 - поворот на 0 градусов
    //              1 - поворот на 90 градусов
    //              2 - поворот на 180 градусов
    //              3 - поворот на 270 градусов
    //      - context - контекст
    // Этот метод выполняется во втором потоке
    //============================================================================
    public void photoFileRotation_Executer(
            String mCurrentPhotoPath,
            int degreeOfRotation,
            int position,
            OnDataPhotoFileRotation onDataPhotoFileRotation,
            Context context) throws IOException {

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
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Генерируем имя фото-файла
        Log.d(TAG, "MyDbManager: photoFileRotation_Executer Генерируем имя фото-файла");
        File newFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        //==============================

        //==============================
        // Только поворачивает фото-изображение
        // Действия:
        //      - Берет старый фото-фойл
        //      - Порорачивает его и помещает в новой фото-файле
        photoFileRotation(mCurrentPhotoPath, newFile, degreeOfRotation);

        //==============================
        // Как только закончили поворачивать фото-изображение, вызываем для последующих действий
        onDataPhotoFileRotation.onPhotoFileRotation(newFile.getAbsolutePath(), position);
        //==============================

    }

    //============================================================================
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
    // Этот метод выполняется во втором потоке
    //============================================================================
    public void photoFileRotation(
            String mOldPhotoPath,
            File newFile,
            int degreeOfRotation) {

        //==============================
        // Получаем bitmap с фото

        // Создаем BitmapFactory.Options
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        // Определите, насколько уменьшить изображение
        int scaleFactor = 1;

        // Декодируйте файл изображения в растровое изображение размером, чтобы заполнить вид
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        // Возвращаем с растровым изображением
        Bitmap bitmap = BitmapFactory.decodeFile(mOldPhotoPath, bmOptions);
        //==============================

        //==============================
        // Вращаем фото

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(90*degreeOfRotation);
        // rotatedBMP - битмап с повернутым файлом
        Bitmap rotatedBMP = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
        //==============================

        //==============================
        // Сохраняем фото
        try {
            // СОХРАНЕНИЕ
            newFile.createNewFile();
            FileOutputStream ostream = new FileOutputStream(newFile);
            rotatedBMP.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.flush();
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //==============================

    }

    //============================================================================
    // Случайный выбор винных полок
    //============================================================================
    private int randomSelectionOfWineShelves() {
        int low = 1;
        int high = 22;
        Random r = new Random();
        int aNumber = low + r.nextInt(high-low+1); //+1 if high is inclusive
        return aNumber;
    }



}
