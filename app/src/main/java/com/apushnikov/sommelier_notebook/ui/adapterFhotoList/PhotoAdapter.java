package com.apushnikov.sommelier_notebook.ui.adapterFhotoList;

import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_IS_MAIN;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_POSITION;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_TYPE;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_TYPE_PHOTO_ADAPTER;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.apushnikov.sommelier_notebook.Global.GlobalConstants;
import com.apushnikov.sommelier_notebook.MainActivity;
import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.db.AppExecuter;
import com.apushnikov.sommelier_notebook.db.OnDataPhotoFileRotation;
import com.apushnikov.sommelier_notebook.db.Photo;
import com.apushnikov.sommelier_notebook.photoProcessing.RotationSampleActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// RecyclerView - это ViewGroup, которая содержит представления, соответствующие вашим данным.
//      Это само представление, поэтому вы добавляете RecyclerView в свой макет так же,
//      как и любой другой элемент пользовательского интерфейса.
// Каждый отдельный элемент в списке определяется объектом-держателем представления.
//      Когда держатель представления создан, с ним не связаны никакие данные.
//      После создания держателя представления RecyclerView привязывает его к своим данным.
//      Вы определяете держателя представления, расширяя RecyclerView.ViewHolder.
//  RecyclerView запрашивает эти представления и связывает представления с их данными,
//      вызывая методы в адаптере. Вы определяете адаптер, расширяя RecyclerView.Adapter.
//  Менеджер по расположению размещает отдельные элементы в вашем списке.
//      Вы можете использовать один из менеджеров компоновки, предоставляемый библиотекой RecyclerView,
//      или определить свой собственный. Все менеджеры компоновки основаны на абстрактном классе
//      библиотеки LayoutManager.
// PhotoAdapter - в текущем фрагменте строит горизонтальный список фотографий
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyViewPhotoHolder>
        implements OnDataPhotoFileRotation{

    private static final String TAG = "myLogs";     //Для логов

    //============================================================================
    // Описываем интерфейс onDeletePhotoInterfaceListener.
    // Была удалена фотография>
    // В DeletePhotoInterfaceListener метод deletePhotoInterface
    // Этот интерфейс будет реализовывать Activity.
    //============================================================================
    public interface onDeletePhotoInterfaceListener {
        public void deletePhotoInterface();
    }
    public onDeletePhotoInterfaceListener deletePhotoInterfaceListener;
    //============================================================================

    // Это конткест EditActivity
    //      - контекст нужен для в LayoutInflater для раздувания отдельной строки
    private Context context;
    // photoArray - это список List<Photo> для фотографий
    private List<Photo> photoArray;
    //TODO: Нужно избавиться от photoArrayOld, если нужно, то вставить чистку мусорных фото
    // photoArrayOld - это список List<Photo> для фотографий, заполненных в начале
    private List<Photo> photoArrayOld;
    // photoArrayHasBeenChanged - показывает, изменился ли массив фото по сравнению
    // с начальным состоянием.
    // Начальное значение - не изменился
    private boolean photoArrayHasBeenChanged = false;

    // Горизонтальный RecyclerView для показа фото
    private RecyclerView recyclerViewListPhoto;

    //============================================================================
    // Конструктор
    //      - Запоминаем контекст
    //      - Создаем пустой массив - спискок List<Photo> для фотографий
    //============================================================================
    public PhotoAdapter(Context context, RecyclerView recyclerViewListPhoto) {

        Log.d(TAG,"PhotoAdapter: PhotoAdapter (Конструктор)");

        // Запоминаем контекст - Это конткест EditActivity
        this.context = context;
        // Создаем пустой массив - спискок List<Photo> для фотографий
        photoArray = new ArrayList<>();
        // Создаем пустой массив - спискок List<Photo> для фотографий, заполненных в начале
        photoArrayOld = new ArrayList<>();
        // Зопоминаем RecyclerView для фото
        this.recyclerViewListPhoto = recyclerViewListPhoto;

        try {
            // Определяем слушатель события - Было удалено вино
            deletePhotoInterfaceListener = (onDeletePhotoInterfaceListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onDeletePhotoInterfaceListener");
        }
    }

    //====================================================================================
    // Геттеры и сеттеры
    //====================================================================================

    //============================================================================
    // Показывает, изменился ли массив фото по сравнению
    // с начальным состоянием.
    //============================================================================
    public boolean getPhotoArrayHasBeenChanged() {
        return photoArrayHasBeenChanged;
    }

    //============================================================================
    // Устанавливает, изменился ли массив фото по сравнению
    // с начальным состоянием. Если изменился, ставим значение true
    //============================================================================
    public void setPhotoArrayHasBeenChanged(boolean photoArrayHasBeenChanged) {
        this.photoArrayHasBeenChanged = photoArrayHasBeenChanged;
    }

    //============================================================================
    // getPhotoArray - возвращает массив
    // photoArray - это список List<Photo> для фотографий
    //============================================================================
    public List<Photo> getPhotoArray() {
        return photoArray;
    }

    //============================================================================
    // getPhotoArrayOld - возвращает массив
    // photoArrayOld - это список List<Photo> для фотографий, заполненных в начале
    //============================================================================
    public List<Photo> getPhotoArrayOld() {
        return photoArrayOld;
    }

    //=================================================================================
    // Когда вы определяете свой адаптер, вам необходимо переопределить три ключевых метода:
    // onCreateViewHolder ()
    // onBindViewHolder ()
    // getItemCount ()
    //=================================================================================

    //============================================================================
    // onCreateViewHolder (): RecyclerView вызывает этот метод всякий раз, когда ему нужно создать
    // новый ViewHolder.
    // Метод создает и инициализирует ViewHolder и связанный с ним View, но не заполняет
    // содержимое представления - ViewHolder еще не привязан к конкретным данным
    //============================================================================
    @NonNull
    @Override
    public MyViewPhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d(TAG,"PhotoAdapter: onCreateViewHolder");

        // Раздуваем view для отдельной строки фотографии
        // Это конткест EditActivity
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_list_pfoto_layout,
                parent,
                false);

//        return new MyViewPhotoHolder(view, context, photoArray);
        return new MyViewPhotoHolder(view, context);

    }

    //============================================================================
    // onBindViewHolder (): RecyclerView вызывает этот метод, чтобы связать ViewHolder с данными.
    // Метод извлекает соответствующие данные и использует их для заполнения макета
    // держателя представления.
    // Например, если RecyclerView отображает список имен, метод может найти соответствующее
    // имя в списке и заполнить виджет TextView держателя представления
    //============================================================================
    @Override
    public void onBindViewHolder(@NonNull MyViewPhotoHolder holder, int position) {

        Log.d(TAG,"PhotoAdapter: onBindViewHolder");

        // Берем photoArray
        // получаем элемент position
        // Берем абсолютный путь к фото-файлу
        holder.setData(photoArray.get(position).getPhoto(), photoArray.get(position).getMainPhoto());
    }

    //============================================================================
    // getItemCount (): RecyclerView вызывает этот метод, чтобы получить размер набора данных.
    // Например, в приложении адресной книги это может быть общее количество адресов.
    // RecyclerView использует это, чтобы определить, когда больше нет элементов,
    // которые могут быть отображены
    //============================================================================
    @Override
    public int getItemCount() {

        Log.d(TAG,"PhotoAdapter: getItemCount");

        return photoArray.size();
    }

    //=================================================================================

    //=================================================================================
    // Каждый отдельный элемент в списке определяется объектом-держателем представления.
    //      Когда держатель представления создан, с ним не связаны никакие данные.
    //      После создания держателя представления RecyclerView привязывает его к своим данным.
    //      Вы определяете держателя представления, расширяя RecyclerView.ViewHolder.
    //=================================================================================
    public class MyViewPhotoHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener,
            PopupMenu.OnMenuItemClickListener {

        private ConstraintLayout cl_02;
        final ImageView vPhotoView;
        private ImageButton imageButtonMoreVert;
        // Это конткест EditActivity
        private Context context;

        //============================================================================
        // Конструктор
        //============================================================================
        public MyViewPhotoHolder(@NonNull View itemView, Context context) {
            super(itemView);

            Log.d(TAG,"PhotoAdapter: MyViewPhotoHolder: MyViewPhotoHolder(Конструктор)");

            // Запоминаем контекст
            this.context = context;

            // Находим в объекте-держателе представления ImageView cl_02
            cl_02 = itemView.findViewById(R.id.cl_02);
            // Находим в объекте-держателе представления ImageView vPhotoView
            vPhotoView = itemView.findViewById(R.id.vPhotoView);
            // На itemView навешиваем обработчик на короткий клик
            vPhotoView.setOnClickListener(this);

            // Зарегистрируйте обратный вызов, который будет вызываться при создании
            // контекстного меню для этого представления.
            // Если этот вид не является долгим щелчком, он становится долгим щелчком.
            vPhotoView.setOnCreateContextMenuListener(this);

            // Находим в объекте-держателе представления ImageView сандвич
            imageButtonMoreVert = itemView.findViewById(R.id.imageButtonMoreVert);
            // На itemView навешиваем обработчик на короткий клик
            imageButtonMoreVert.setOnClickListener(this);
        }

        //============================================================================
        // setData - заполняет объекте-держателе представления фото
        // String photo - абсолютный путь к фото-файлу
        //============================================================================
        public void setData(String photoPath, int isMainPhoto){

            // Выделяем главное фото
            if (isMainPhoto == 1) // Если фото является главным
            {
                cl_02.setBackgroundResource(R.drawable.my_photobox_background_highlighted);
            } else  // Если фото НЕ является главным
            {
                cl_02.setBackgroundResource(R.drawable.my_photobox_background_normal);
            }

            // Рисуем фотографию
            if (photoPath != null) {
                // Получите размеры представления imageView
                int targetW = vPhotoView.getWidth();
                int targetH = vPhotoView.getHeight();

                if (targetW == 0) {
                    targetW = 100;
                }
                if (targetH == 0) {
                    targetH = 100;
                }

                // Получить размеры растрового изображения
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                // Если установлено значение true, декодер вернет null (без растрового изображения),
                // но поля out ... будут по-прежнему установлены, позволяя вызывающей стороне запрашивать
                // растровое изображение без необходимости выделять память для его пикселей.
                bmOptions.inJustDecodeBounds = true;

                // Расшифровать путь к файлу в растровое изображение
                // Параметры:
                // полный путь к декодируемому файлу - Абсолютный путь к фото-файлу
                // BitmapFactory.Options: null-ok; Параметры, которые управляют понижающей дискретизацией
                // и должно ли изображение быть полностью декодировано или просто возвращается размер.
                BitmapFactory.decodeFile(photoPath, bmOptions);

                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Определите, насколько уменьшить изображение
                // photoW/targetW - ищем минимум - во сколько раз фото больше целевого изображения
                // Math.max - возвращает:
                //      >1, если изображение фото больше целевого изображения
                //      1, если изображение фото меньше целевого изображеният, т.е не мастшабируется
                int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

                // Декодируйте файл изображения в растровое изображение размером, чтобы заполнить вид
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;

                // BitmapFactory - Создает объекты Bitmap из различных источников,
                // включая файлы, потоки и байтовые массивы
                // decodeFile - Расшифровать путь к файлу в растровое изображение.
                // Параметры:
                //      - полный путь к декодируемому файлу - Абсолютный путь к фото-файлу
                //      - BitmapFactory.Options: null-ok; Параметры, которые управляют понижающей
                //      дискретизацией и должно ли изображение быть полностью декодировано или
                //      просто возвращается размер.
                // Объект Bitmap
                Bitmap mBitmap = BitmapFactory.decodeFile(photoPath, bmOptions);

                vPhotoView.setImageBitmap(mBitmap);
            }
        }

        //============================================================================
        // По нажатию на фото, запускаем фото на увеличение
        // По нажатию на сандвич, запускаем контекстное меню
        // Это конткест EditActivity
        //============================================================================
        @Override
        public void onClick(View v) {

            Log.d(TAG,"PhotoAdapter: MyViewPhotoHolder: onClick");

            switch (v.getId()) {

                // По нажатию на фото, запускаем фото на увеличение
                case R.id.vPhotoView:
                    if (photoArray.get(getAdapterPosition()).getPhoto() != null) {
                        // Создаем интент
                        Intent i = new Intent(context, RotationSampleActivity.class);
                        // Погружаем в интент абсолютный путь к фото файлу
                        i.putExtra(PHOTO_PROCESSING, photoArray.get(getAdapterPosition()).getPhoto());
                        // Погружаем в интент, является ли фото главным
                        i.putExtra(PHOTO_PROCESSING_IS_MAIN, photoArray.get(getAdapterPosition()).getMainPhoto());
                        // Погружаем в интент, тип передаваемого адаптера
                        i.putExtra(PHOTO_PROCESSING_TYPE, PHOTO_PROCESSING_TYPE_PHOTO_ADAPTER);

                        // TODO: А как без внешней переменной обеспечить обратный вызов onPhotoFileRotation
                        // Передаем через внещнюю переменную контекст для PhotoAdapter
                        // Чтобы обеспечить обратный вызов onPhotoFileRotation
                        GlobalConstants.context_PhotoAdapter = PhotoAdapter.this;

                        // Передаем позицию адаптера
                        i.putExtra(PHOTO_PROCESSING_POSITION, getAdapterPosition());
                        context.startActivity(i);
                    }
                    break;

                // По нажатию на сандвич, запускаем контекстное меню
                case R.id.imageButtonMoreVert:
                    PopupMenu popup = new PopupMenu(context, v);
                    popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) this);
                    popup.inflate(R.menu.menu_photo_adapter);
                    popup.show();
                    break;

            }

        }

        //============================================================================
        // Создание контекстного меню
        //============================================================================
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onCreateContextMenu: меню создано");

            // TODO: Нужно создавать контекстное меню из ресурса.
            //  Сейчас меню создается 2-мя способами - это неправильно

            // Создаем контекстное меню
            MenuItem myActionItem1 = menu.add(0,R.id.menu_photo_main,0,"Сделать фото главным");
            MenuItem myActionItem2 = menu.add(0,R.id.menu_photo_rotation_right,0,"Повернуть фото вправо");
            MenuItem myActionItem3 = menu.add(0,R.id.menu_photo_rotation_left,0,"Повернуть фото влево");
            MenuItem myActionItem4 = menu.add(0,R.id.menu_photo_delete,0,"Удалить фото");

            // Навешиваем обработчик на нажатия пунтов контекстного меню
            myActionItem1.setOnMenuItemClickListener(this);
            myActionItem2.setOnMenuItemClickListener(this);
            myActionItem3.setOnMenuItemClickListener(this);
            myActionItem4.setOnMenuItemClickListener(this);
        }


        //============================================================================
        // Реакция на нажатие контекстного меню
        //============================================================================
        @Override
        public boolean onMenuItemClick(MenuItem item) {

//            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            switch (item.getItemId()) {
                case R.id.menu_photo_main:      // Сделать фото главным

                    // Запоминаем текущее фото
                    Photo currentPhoto = photoArray.get(getAdapterPosition());
                    // Делаем, только если текущее фото НЕ является главными
                    if (currentPhoto.getMainPhoto() != 1) {
                        // Берем текущее фото
                        Photo mPhoto = photoArray.get(getAdapterPosition());
                        // Делаем фото главным
                        mPhoto.setMainPhoto(1);
                        // Все остальные фото делаем не главными
                        for (Photo photo : photoArray) {
                            if (photo != currentPhoto) {
                                // Делаем фото НЕ главным
                                photo.setMainPhoto(0);
                            }
                        }
                        // Сообщаем зарегистрированным пользователям, что элемент изменился
                        // и перерисовать надо все
                        notifyDataSetChanged();
                        // Устанавливаем, что массив фото по сравнению с начальным состоянием изменился
                        setPhotoArrayHasBeenChanged(true);
                    }
                    break;

                case R.id.menu_photo_rotation_right:    // Повернуть фото вправо

                    // Устанавливаем, что массив фото по сравнению с начальным состоянием изменился
                    setPhotoArrayHasBeenChanged(true);
                    // Поворачивает фото-изображение во втором потоке
                    AppExecuter.getInstance().getSubIO().execute(() -> {
                        try {
                            MainActivity.myDbManager.photoFileRotation_Executer(
                                    photoArray.get(getAdapterPosition()).getPhoto(),
                                    1,
                                    getAdapterPosition(),
                                    (OnDataPhotoFileRotation) PhotoAdapter.this,
                                    context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    // ВАЖНО - продолжение метода после отработки второго потока
                    // в интерфейсе OnDataPhotoFileRotation
                    break;

                case R.id.menu_photo_rotation_left:    // Повернуть фото влево

                    // Устанавливаем, что массив фото по сравнению с начальным состоянием изменился
                    setPhotoArrayHasBeenChanged(true);
                    // Поворачивает фото-изображение во втором потоке
                    AppExecuter.getInstance().getSubIO().execute(() -> {
                        try {
                            MainActivity.myDbManager.photoFileRotation_Executer(
                                    photoArray.get(getAdapterPosition()).getPhoto(),
                                    3,
                                    getAdapterPosition(),
                                    (OnDataPhotoFileRotation) PhotoAdapter.this,
                                    context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    // ВАЖНО - продолжение метода после отработки второго потока
                    // в интерфейсе OnDataPhotoFileRotation
                    break;

                case R.id.menu_photo_delete:    // Удалить фото

                    // Запоминаем номер фотографии
                    long mPhotoId = photoArray.get(getAdapterPosition()).getId();

                    // Используйте класс Builder для удобного построения диалогов
                    AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogTheme);
                    builder
                            .setTitle(R.string.dialog_delete_photo)    // Заголовок диалога
                            // Позитивная кнопка - удалить винную полку
                            .setPositiveButton(R.string.dialog_delete_photo_yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Удаляем винную полку
                                    deletePhoto(mPhotoId);
                                }
                            })
                            // Отрицательная кнопка - Отказ
                            .setNegativeButton(R.string.dialog_delete_photo_no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Отказ от удаления.
                                    // Ничего не делаем
                                }
                            });
                    // Создаем диалог
                    AlertDialog alert = builder.create();
                    // Показываем диалог
                    alert.show();
                    break;

            }
            return true;
        }

        //============================================================================
        // Удаляем фотографию
        // Удаляем из массива фото
        // Переназначаем, если нужно главное фото
        // Помечаем, что массив фото изменился
        //============================================================================
        public void deletePhoto(long mPhotoId) {

            // Запоминаем позицию фото в массиве
            int i = getAdapterPosition();
            // Запоминаем текущее фото
            Photo currentPhoto = photoArray.get(i);

            if (currentPhoto.getMainPhoto() == 1)   // Если текущее фото является главными
            {
                //Удаляем фото из массива
                photoArray.remove(i);
                if (photoArray.size() > 0)  // Если в массиве есть еще фотографии
                {
                    // Делаем главной первое фото
                    photoArray.get(0).setMainPhoto(1);

                }
                // Сообщаем зарегистрированным пользователям, что элемент изменился
                // и перерисовать надо все
                notifyDataSetChanged();
            }
            else          // Если текущее фото НЕ является главными
            {
                //Удаляем фото из массива
                photoArray.remove(i);
                // Перерисовываем
                notifyItemRemoved(i);
            }

            // Была удалена фотография
            // Вызываем через интерфейс обработчик события deletePhotoInterface
            // в качестве обработчика выступает EditActivity,
            // т.е. clickPhotoShelfListener = EditActivity
            deletePhotoInterfaceListener.deletePhotoInterface();

            // Устанавливаем, что массив фото по сравнению с начальным состоянием изменился
            setPhotoArrayHasBeenChanged(true);
        }

    }

    //=================================================================================
    // updateAdapter - Обновляем адаптер используя newList - List<Photo>
    //      - Очищаем спискок List<Photo> записей фотографии
    //      - Добавляет все элементы в указанной коллекции в конец этого списка в том порядке,
    //          в котором они возвращаются итератором указанной коллекции (необязательная операция).
    //          Поведение этой операции не определено, если указанная коллекция изменяется во время
    //          выполнения операции.
    //          (Обратите внимание, что это произойдет, если указанная коллекция является этим
    //
    //      - Уведомите всех зарегистрированных наблюдателей об изменении набора данных
    //=================================================================================
    public void updateAdapter(List<Photo> newList) {

        Log.d(TAG,"PhotoAdapter: updateAdapter");

        // Очищаем список фотографий (хотя он и должен быть пустым)
        photoArray.clear();
        // Список photoArray заменяем новым списком newList
        photoArray.addAll(newList);
        // Список photoArrayOld заменяем новым списком newList (для запоминания старых фотографий)
        photoArrayOld.addAll(newList);
        // Уведомите всех зарегистрированных наблюдателей об изменении набора данных.
        // Существует два разных класса событий изменения данных: изменения элементов и
        // структурные изменения. Изменения элемента - это когда данные отдельного элемента
        // обновлены, но позиционных изменений не произошло. Структурные изменения - это когда
        // элементы вставляются, удаляются или перемещаются в наборе данных.
        //Это событие не указывает, что было изменено в наборе данных, заставляя любых
        // наблюдателей предполагать, что все существующие элементы и структура могут больше
        // не быть действительными. LayoutManager будет вынужден полностью повторно привязать и
        // ретранслировать все видимые представления
        // (RecyclerView будет пытаться синтезировать видимые события структурных изменений для
        // адаптеров, которые сообщают, что у них есть стабильные идентификаторы при использовании
        // этого метода. Это может помочь для целей анимации и сохранения визуальных объектов,
        // но представления отдельных элементов все равно необходимо будет повторно привязать и
        // повторно использовать.
        // Если вы пишете адаптер, всегда будет более эффективно использовать более конкретные
        // события изменения, если это возможно.
        // !!!!!Положитесь на notifyDataSetChanged () как на последнее средство
        notifyDataSetChanged();
    }

    //============================================================================
    // Обновляем адаптер, добавляя в данные адаптера photo и используя notifyItemInserted
    // Новый элемент еще не в базе данных
    //============================================================================
    public void updateAdapterPhotoInserted(Photo photo) {

        Log.d(TAG,"PhotoAdapter: updateAdapterPhotoInserted");

        // Опрелеляем размер массива photoArray
        int i = photoArray.size();
        // Если фотографий еще в массиве нет, делаем вставляемую фотографию главной
        if (i <= 0) {
            photo.setMainPhoto(1);
        }

        // Добавляем photo в конец массива
        photoArray.add(photo);
        // Сообщает всем зарегистрированным наблюдателям, что отраженный элемент i был добавлен заново.
        // Предмет, который ранее находился на i-й позиции, теперь находится на позиции i + 1.
        notifyItemInserted(i);
        // Прокручиваем recyclerView на позицию вставленного элемента
        recyclerViewListPhoto.scrollToPosition(i);

        // Устанавливаем, что массив фото по сравнению с начальным состоянием изменился
        setPhotoArrayHasBeenChanged(true);
    }

    //==============================================================================
    // Интерфейс - действия, когда мы повернули фото
    // Обновление адаптера в ОСНОВНОМ потоке
    //==============================================================================
    @Override
    public void onPhotoFileRotation(String newPhotoPath, int position) {

        AppExecuter.getInstance().getMainIO().execute(new Runnable() {
            @Override
            public void run() {
                // Присваиваем новый путь к фотофайлу
                photoArray.get(position).setPhotoAbsolutePath(newPhotoPath);

                // Сообщите зарегистрированным наблюдателям, что позиция изменилась.
                // Эквивалентно вызову notifyItemChanged (position, null) ;.
                notifyItemChanged(position);
            }
        });
    }

}

