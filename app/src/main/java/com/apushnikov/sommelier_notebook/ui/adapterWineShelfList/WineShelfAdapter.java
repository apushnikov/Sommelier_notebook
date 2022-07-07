package com.apushnikov.sommelier_notebook.ui.adapterWineShelfList;

import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.CHOOSE_FROM_PICTURES_POSITION;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.CHOOSE_FROM_PICTURES_WINE_SHELF;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_IS_MAIN;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_POSITION;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_TYPE;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PHOTO_PROCESSING_TYPE_WINE_SHELF_ADAPTER;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.PICK_IMAGE_CODE_WINE_SHELF;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.REQUEST_CHOOSE_FROM_PICTURES;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.REQUEST_LARGE_PHOTO_WINE_SHELF;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.globalPhotoAbsolutePathShelf;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.globalPosition;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.globalWineShelf;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.theSecondThreadIsRunning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.apushnikov.sommelier_notebook.Global.GlobalConstants;
import com.apushnikov.sommelier_notebook.MainActivity;
import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.db.AppExecuter;
import com.apushnikov.sommelier_notebook.db.OnDataPhotoFileRotation;
import com.apushnikov.sommelier_notebook.db.OnDataUpdatedWineShelf;
import com.apushnikov.sommelier_notebook.db.WineShelf;
import com.apushnikov.sommelier_notebook.photoProcessing.RotationSampleActivity;
import com.apushnikov.sommelier_notebook.ui.chooseFromPictures.ChooseFromPictures;
import com.apushnikov.sommelier_notebook.utilities.EditWineShelfDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
// WineShelfAdapter - в строит горизонтальный список винных полок
public class WineShelfAdapter extends RecyclerView.Adapter<WineShelfAdapter.MyViewWineShelfHolder>
        implements OnDataPhotoFileRotation {

    private static final String TAG = "myLogs";     //Для логов

    //============================================================================
    // Описываем интерфейс onClickWineShelfListener.
    // Была нажата винная полка
    // В onClickWineShelfListener метод clickWineShelf, который получает на вход номер винной полки
    // Этот интерфейс будет реализовывать Activity.
    //============================================================================
    public interface onClickWineShelfListener {
        public void clickWineShelf(long mShelfId);
    }
    public onClickWineShelfListener clickWineShelfListener;
    //============================================================================

    //============================================================================
    // ПОЛЯ
    //============================================================================

    // экземпляр SharedPreferences
    SharedPreferences prefs = null;

    // Горизонтальный RecyclerView для показа винных полок, переданный в конструкторе
    private RecyclerView recyclerViewListWineShelf;

    // Это конткест MainActivity
    //      - контекст нужен для в LayoutInflater для раздувания отдельной строки
    private Context context;
    // wineShelfArray - это список List<WineShelf> для винных полок
    private List<WineShelf> wineShelfArray;

    // Текущий номер выделенной полки
    //      Если значение > 0, то это настоящая винная полка
    //      Если значение -1, то винных полок нет
    private long currentShelfId;
    // Текущий вью, соответствующий выделенной полке
    private View currentView;
    //TODO: Зчем нужень currentView. Можно без него обойтись

    //============================================================================
    // Конструктор
    //      - Запоминаем контекст
    //      - Создаем пустой массив - спискок List<WineShelf> для винных полок
    //      - Зопоминаем номер выделенной полки
    //============================================================================
    public WineShelfAdapter(Context context, long currentShelfId, RecyclerView recyclerViewListWineShelf) {

//        Log.d(TAG,"WineShelfAdapter: WineShelfAdapter (Конструктор)");

        // Запоминаем контекст - Это конткест MainActivity
        this.context = context;
        // Создаем пустой массив - спискок List<WineShelf> для винных полок
        wineShelfArray = new ArrayList<>();
        // Зопоминаем номер выделенной полки
        this.currentShelfId = currentShelfId;
        // Зопоминаем RecyclerView для показа винных полок
        this.recyclerViewListWineShelf = recyclerViewListWineShelf;

        try {

//            Log.d(TAG,"WineShelfAdapter: WineShelfAdapter (Конструктор): try");

            // Определяем слушатель события - Была нажата винная полка
            clickWineShelfListener = (onClickWineShelfListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onClickWineShelfListener");
        }

//        Log.d(TAG,"WineShelfAdapter: WineShelfAdapter (Конструктор) Конец");

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
    public MyViewWineShelfHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        Log.d(TAG,"WineShelfAdapter: onCreateViewHolder");

        // Раздуваем view для отдельной строки винной полки
        // Это конткест MainActivity
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_list_wine_shelf,
                parent,
                false);

//        Log.d(TAG,"WineShelfAdapter: onCreateViewHolder Конец");

        return new MyViewWineShelfHolder(view, context);
    }

    //============================================================================
    // onBindViewHolder (): RecyclerView вызывает этот метод, чтобы связать ViewHolder с данными.
    // Метод извлекает соответствующие данные и использует их для заполнения макета
    // держателя представления.
    // Например, если RecyclerView отображает список имен, метод может найти соответствующее
    // имя в списке и заполнить виджет TextView держателя представления
    //============================================================================
    @Override
    public void onBindViewHolder(@NonNull MyViewWineShelfHolder holder, int position) {

//        Log.d(TAG,"WineShelfAdapter: onBindViewHolder");

        // Берем wineShelfArray
        // получаем элемент position
        // Берем Наименование винной полки
        holder.setData(
                wineShelfArray.get(position).getNameShelf(),
                wineShelfArray.get(position).getPhotoAbsolutePathShelf());

        // Находим в объекте-держателе представления ImageView tvWineShelf
        TextView tvWineShelf = holder.itemView.findViewById(R.id.tvWineShelf);
        ConstraintLayout cl_02 = holder.itemView.findViewById(R.id.cl_02);

        // Если номер элемента массива совпадат с номером текущей выделенной полки,
        // т.е. полка является выделенной
        if (wineShelfArray.get(position).getId() == currentShelfId)
        {
            // Запоминаем текущий вью, соответствующий выделенной полке
            currentView = holder.itemView;

            //TODO: слишком много операций findViewById, можно оптимизировать

            // Красим выделенную полку в выделенный цвет
//            tvWineShelf.setTextColor(0xFFBB86FC);
            tvWineShelf.setTypeface(null, Typeface.BOLD);
            cl_02.setBackgroundResource(R.drawable.my_wineshelfbox_background_highlighted);
            cl_02.setAlpha(1);

        } else // Если номер элемента массива НЕ совпадат с номером текущая выделенной полки
        {
            // Красим НЕ выделенную полку в нормальный цвет
//            tvWineShelf.setTextColor(0xFF000000);
            tvWineShelf.setTypeface(null, Typeface.NORMAL);
            cl_02.setBackgroundResource(R.drawable.my_wineshelfbox_background_normal);
            cl_02.setAlpha(0.5F);

        }

//        Log.d(TAG,"WineShelfAdapter: onBindViewHolder Конец");

    }

    //============================================================================
    // getItemCount (): RecyclerView вызывает этот метод, чтобы получить размер набора данных.
    // Например, в приложении адресной книги это может быть общее количество адресов.
    // RecyclerView использует это, чтобы определить, когда больше нет элементов,
    // которые могут быть отображены
    //============================================================================
    @Override
    public int getItemCount() {

//        Log.d(TAG,"WineShelfAdapter: getItemCount");

        return wineShelfArray.size();
    }
    //=================================================================================

    //=================================================================================
    // Каждый отдельный элемент в списке определяется объектом-держателем представления.
    //      Когда держатель представления создан, с ним не связаны никакие данные.
    //      После создания держателя представления RecyclerView привязывает его к своим данным.
    //      Вы определяете держателя представления, расширяя RecyclerView.ViewHolder.
    //=================================================================================
        public class MyViewWineShelfHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener,
            PopupMenu.OnMenuItemClickListener,
            EditWineShelfDialogFragment.EditDialogListener,
            OnDataUpdatedWineShelf {

        private Context context;
        final ImageView ivWineShelf;
        final TextView tvWineShelf;
        private ImageButton iButtonMoreVert;

        //============================================================================
        // Конструктор
        //============================================================================
        public MyViewWineShelfHolder(@NonNull View itemView, Context context) {
            super(itemView);

//            Log.d(TAG,"WineShelfAdapter: MyViewWineShelfHolder: MyViewWineShelfHolder(Конструктор)");

            // Запоминаем контекст
            // Это конткест MainActivity
            this.context = context;

            // Находим в объекте-держателе представления ImageView tvWineShelf
            tvWineShelf = itemView.findViewById(R.id.tvWineShelf);
            ivWineShelf = itemView.findViewById(R.id.ivWineShelf);
            iButtonMoreVert = itemView.findViewById(R.id.iButtonMoreVert);

            // На itemView навешиваем обработчик на короткий клик
            tvWineShelf.setOnClickListener(this);
            ivWineShelf.setOnClickListener(this);
            iButtonMoreVert.setOnClickListener(this);

            // Зарегистрируйте обратный вызов, который будет вызываться при создании
            // контекстного меню для этого представления.
            // Если этот вид не является долгим щелчком, он становится долгим щелчком.
            tvWineShelf.setOnCreateContextMenuListener(this);
            ivWineShelf.setOnCreateContextMenuListener(this);

//            Log.d(TAG,"WineShelfAdapter: MyViewWineShelfHolder: MyViewWineShelfHolder(Конструктор) Конец");

        }

        //============================================================================
        // setData - заполняет объекте-держателе представления винные полки
        // String nameShelf - Наименование винной полки
        //============================================================================
        public void setData(String nameShelf, String photoPath){

//            Log.d(TAG,"WineShelfAdapter: MyViewWineShelfHolder: setData");

            tvWineShelf.setText(nameShelf);

            // Рисуем фотографию винной полки
            if (photoPath != null) {
                // Получите размеры представления imageView
                int targetW = ivWineShelf.getWidth();
                int targetH = ivWineShelf.getHeight();

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

                ivWineShelf.setImageBitmap(mBitmap);
            }

        }

        //============================================================================
        // Была нажата винная полка
        // По нажатию на винную полку, запускаем просмотр содержимого винной полки
        // По нажатию на сандвич, запускаем контекстное меню
        // Это конткест MainActivity
        //============================================================================
        @Override
        public void onClick(View v) {

//            Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onClick");

            switch (v.getId()) {

                // По нажатию на винную полку, запускаем просмотр содержимого винной полки
                case R.id.ivWineShelf:

                // По нажатию на винную полку, запускаем просмотр содержимого винной полки
                case R.id.tvWineShelf:
                    // TODO: Если мы нажали на выделенной винной полке, то перерисовывать ничего не нужно.
                    //  А сейчас перерисовывается

                    // Запоминаем номер винной полки
                    long mShelfId = wineShelfArray.get(getAdapterPosition()).getId();

                    // Была нажата винная полка
                    // Вызываем через интерфейс обработчик события "click на винной полке" clickWineShelf
                    // в качестве обработчика выступает MainActivity,
                    // т.е. clickWineShelfListener = MainActivity
                    clickWineShelfListener.clickWineShelf(mShelfId);

                    // Если текущая винная полка не совпадает с нажатой
                    if (currentShelfId != mShelfId) {
                        // Красим выделенную полку в нормальный цвет
                        TextView tvOldWineShelf = currentView.findViewById(R.id.tvWineShelf);
//                        tvOldWineShelf.setTextColor(0xFF000000);
                        tvOldWineShelf.setTypeface(null, Typeface.NORMAL);
                        ConstraintLayout old_cl_02 = currentView.findViewById(R.id.cl_02);
                        old_cl_02.setBackgroundResource(R.drawable.my_wineshelfbox_background_normal);
                        old_cl_02.setAlpha(0.5F);

                        // Красим нажатую полку в выделенный цвет
                        TextView tvNewWineShelf = itemView.findViewById(R.id.tvWineShelf);
//                        tvNewWineShelf.setTextColor(0xFFBB86FC);
                        tvNewWineShelf.setTypeface(null, Typeface.BOLD);
                        ConstraintLayout new_cl_02 = itemView.findViewById(R.id.cl_02);
                        new_cl_02.setBackgroundResource(R.drawable.my_wineshelfbox_background_highlighted);
                        new_cl_02.setAlpha(1);

                        // Запоминаем для выделенной полки номер и вью
                        currentView = itemView;
                        currentShelfId = mShelfId;
                    }
                    break;

                // По нажатию на сандвич, запускаем контекстное меню
                case R.id.iButtonMoreVert:
                    PopupMenu popup = new PopupMenu(context, v);
                    popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) this);
                    popup.inflate(R.menu.menu_wineshelf_adapter);
                    popup.show();
                    break;
            }

//            Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onClick Конец");

        }

        //============================================================================
        // Создание контекстного меню
        //============================================================================
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

//            Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onCreateContextMenu: меню создано");

            // TODO: Нужно создавать контекстное меню из ресурса.
            //  Сейчас меню создается 2-мя способами - это неправильно

            // Создаем контекстное меню
            MenuItem myActionItem1 = menu.add(0,R.id.menu_wineshelf_viewing,0,"Просмотр изображения");
            MenuItem myActionItem2 = menu.add(0,R.id.menu_wineshelf_edit,0,"Редактировать полку");
            MenuItem myActionItem3 = menu.add(0,R.id.menu_wineshelf_ChooseFromPictures,0,"Выбрать из картинок");
            MenuItem myActionItem4 = menu.add(0,R.id.menu_wineshelf_PhotoOrGallery,0,"Фото или Галерея");
            MenuItem myActionItem5 = menu.add(0,R.id.menu_wineshelf_rotation_right,0,"Повернуть фото вправо");
            MenuItem myActionItem6 = menu.add(0,R.id.menu_wineshelf_rotation_left,0,"Повернуть фото влево");
            MenuItem myActionItem7 = menu.add(0,R.id.menu_wineshelf_delete,0,"Удалить полку");

            // Навешиваем обработчик на нажатия пунтов контекстного меню
            myActionItem1.setOnMenuItemClickListener(this);
            myActionItem2.setOnMenuItemClickListener(this);
            myActionItem3.setOnMenuItemClickListener(this);
            myActionItem4.setOnMenuItemClickListener(this);
            myActionItem5.setOnMenuItemClickListener(this);
            myActionItem6.setOnMenuItemClickListener(this);
            myActionItem7.setOnMenuItemClickListener(this);
        }

        //============================================================================
        // Реакция на нажатие контекстного меню
        //============================================================================
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onMenuItemClick: меню нажато");

            // Запоминаем название винной полки для редактирования или удаления
            String mNameShelf = wineShelfArray.get(getAdapterPosition()).getNameShelf();

            switch (item.getItemId()) {

                case R.id.menu_wineshelf_viewing:      // Просмотр изображения
                    Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onMenuItemClick: Просмотр изображения");
                    if (wineShelfArray.get(getAdapterPosition()).getPhotoAbsolutePathShelf() != null) {
                        // Создаем интент
                        Intent i = new Intent(context, RotationSampleActivity.class);
                        // Погружаем в интент абсолютный путь к фото файлу
                        i.putExtra(PHOTO_PROCESSING, wineShelfArray.get(getAdapterPosition()).getPhotoAbsolutePathShelf());
                        // Погружаем в интент, является ли фото главным
                        i.putExtra(PHOTO_PROCESSING_IS_MAIN, 0);
                        // Погружаем в интент, тип передаваемого адаптера
                        i.putExtra(PHOTO_PROCESSING_TYPE, PHOTO_PROCESSING_TYPE_WINE_SHELF_ADAPTER);

                        // TODO: А как без внешней переменной обеспечить обратный вызов onPhotoFileRotation
                        // Передаем через внещнюю переменную контекст для PhotoAdapter
                        // Чтобы обеспечить обратный вызов onPhotoFileRotation
                        GlobalConstants.context_WineShelfAdapter = WineShelfAdapter.this;

                        // Передаем позицию адаптера
                        i.putExtra(PHOTO_PROCESSING_POSITION, getAdapterPosition());
                        context.startActivity(i);
                        Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onMenuItemClick: Запущено startActivity(i)");
                    }

                    break;

                case R.id.menu_wineshelf_edit:      // Редактировать полку
                    // создаем диалог - введите название винной полки
                    DialogFragment dialog = new EditWineShelfDialogFragment(mNameShelf,this);
                    // показываем диалог
                    dialog.show(((AppCompatActivity)context).getSupportFragmentManager(), "EditWineShelfDialogFragment");
                    break;

                case R.id.menu_wineshelf_ChooseFromPictures:      // Выбрать из картинок
                    Intent myIntent;
                    myIntent = new Intent(context, ChooseFromPictures.class);
                    // Погружаем в интент винную полку (целиком) за счет того, что класс WineShelf сериализуем
                    myIntent.putExtra(CHOOSE_FROM_PICTURES_WINE_SHELF, wineShelfArray.get(getAdapterPosition()));
                    // Погружаем в интент позицию адаптера
                    myIntent.putExtra(CHOOSE_FROM_PICTURES_POSITION, getAdapterPosition());

                    // TODO: Заменить устаревший вызов startActivityForResult на более современное
                    ((AppCompatActivity)context).startActivityForResult(myIntent, REQUEST_CHOOSE_FROM_PICTURES);
                    break;

                case R.id.menu_wineshelf_PhotoOrGallery:      // Фото или Галерея

                    //TODO: вынести работу с фото или галерей в отдельный класс.
                    // Это практически копия методов из EditActivity

                    // Используйте класс Builder для удобного построения диалогов
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context,R.style.AlertDialogTheme);
                    builder1
                            .setTitle(R.string.dialog_new_photo)    // Заголовок диалога
                            // Позитивная кнопка - сделать фото
                            .setPositiveButton(R.string.dialog_to_make_a_photo, new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.M)
                                public void onClick(DialogInterface dialog, int id) {
                                    // Делаем новую фотографию
                                    makePhoto();
                                }
                            })
                            // Нейтральная кнопка - взять из галереи
                            .setNeutralButton(R.string.dialog_take_from_gallery, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Загружаем фото из галереи
                                    takePhotoFromGallery();
                                }
                            });
                    // Создаем диалог
                    AlertDialog alert1 = builder1.create();
                    // Показываем диалог
                    alert1.show();
                    
                    break;

                case R.id.menu_wineshelf_rotation_right:      // Повернуть фото вправо
                    Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onMenuItemClick: Повернуть фото вправо");
                    // Поворачивает фото-изображение во втором потоке
                    AppExecuter.getInstance().getSubIO().execute(() -> {
                        try {
                            MainActivity.myDbManager.photoFileRotation_Executer(
                                    wineShelfArray.get(getAdapterPosition()).getPhotoAbsolutePathShelf(),
                                    1,
                                    getAdapterPosition(),
                                    (OnDataPhotoFileRotation) WineShelfAdapter.this,
                                    context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    // ВАЖНО - продолжение метода после отработки второго потока
                    // в интерфейсе OnDataPhotoFileRotation

                    Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onMenuItemClick: Конец Повернуть фото вправо");
                    break;

                case R.id.menu_wineshelf_rotation_left:      // Повернуть фото влево
                    Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onMenuItemClick: Повернуть фото влево");
                    // Поворачивает фото-изображение во втором потоке
                    AppExecuter.getInstance().getSubIO().execute(() -> {
                        try {
                            MainActivity.myDbManager.photoFileRotation_Executer(
                                    wineShelfArray.get(getAdapterPosition()).getPhotoAbsolutePathShelf(),
                                    3,
                                    getAdapterPosition(),
                                    (OnDataPhotoFileRotation) WineShelfAdapter.this,
                                    context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    // ВАЖНО - продолжение метода после отработки второго потока
                    // в интерфейсе OnDataPhotoFileRotation
                    Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onMenuItemClick: Конец Повернуть фото влево");
                    break;

                case R.id.menu_wineshelf_delete:    // Удалить полку
                    //Проверяем данные для удаления
                    // Запоминаем номер винной полки
                    long mShelfId = wineShelfArray.get(getAdapterPosition()).getId();

                    //Сколько вина имеется в винной полке?
                    final long[] mCountWineByShelfId = {0};
                    theSecondThreadIsRunning = true;
                    Log.d(TAG, "Начало выполнения 2 потока");

                    // Создаем новый поток
                    new Thread(new Runnable() {
                        // У интерфейса Runnable() необходимо переопределить метод run (в котором будут вычисления)
                        public void run() {
                            // Получаем количество вин по номеру винной полки shelfId
                            mCountWineByShelfId[0] = MainActivity.wineDao.countByShelfId(mShelfId);

                            theSecondThreadIsRunning = false;
                            Log.d(TAG,"Конец выполнения 2 потока");

                        }
                    }).start();

                    while (theSecondThreadIsRunning) { // линия 1
                        // TODO: Здесь пока оправдано.
                        //  плохай стиль 1) передача через глобальную переменную, 2) задержка потока IU
                        // Ждем - выполняется второй поток
                    }

                    //Подготовка сообщения о количесвте удаляемых вин
                    String mMessage;
                    if (mCountWineByShelfId[0] > 0) {
                        mMessage = context.getString(R.string.mMessage01) + " \"" +
                                mNameShelf +
                                "\" " + context.getString(R.string.mMessage02) +
                                mCountWineByShelfId[0] +
                                context.getString(R.string.mMessage03) +
                                context.getString(R.string.mMessage04) +
                                context.getString(R.string.mMessage05);
                    } else {
                        mMessage = context.getString(R.string.mMessage01) + " \"" +
                                mNameShelf +
                                "\" " + context.getString(R.string.mMessage06) +
                                context.getString(R.string.mMessage05);
                    }

                    // Используйте класс Builder для удобного построения диалогов
                    AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogTheme);
                    builder
                            .setTitle(R.string.dialog_delete_wine_shelf)    // Заголовок диалога
                            .setMessage(mMessage)    // Заголовок диалога
                            // Позитивная кнопка - удалить винную полку
                            .setPositiveButton(R.string.dialog_delete_wine_shelf_yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Удаляем винную полку
                                    deleteWineShelf(mShelfId);
                                }
                            })
                            // Отрицательная кнопка - Отказ
                            .setNegativeButton(R.string.dialog_delete_wine_shelf_no, new DialogInterface.OnClickListener() {
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
        // Пользователь коснулся положительной кнопки диалога - редактирование винной полки
        //============================================================================
        @Override
        public void onDialogPositiveClickEditWineShelf(EditWineShelfDialogFragment dialog) {

//            Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onDialogPositiveClickEditWineShelf");

            // Проверка, что введенное значение не пусто
            String newNameShelf = dialog.getNameShelf();
            if (!newNameShelf.isEmpty()) // если введенное наименование не пусто
            {

                // TODO: Если мы нажали меню на выделенной винной полке, то перерисовывать ничего не нужно.
                //  А сейчас перерисовывается - надо оптимизировать

                // Используя метод getAdapterPosition(), в массиве винных полок wineShelf
                // берем ту, которая соответствует нажатому меню
                // При этом в wineShelf попадают все атрибуты имеющейся винной полки,
                // в том числе фотография
                WineShelf wineShelf = wineShelfArray.get(getAdapterPosition());

                // Запоминаем номер винной полки
                long mShelfId = wineShelf.getId();
                // помещаем в винную полку новое новое название
                wineShelf.setNameShelf(newNameShelf);
                // На Доп.характеристики (например, тема дегустации) внимания пока не обращаем

                // Обновляем винную полку во втором потоке
                AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.myDbManager.updateWineShelf_Executer(
                                wineShelf,
                                (OnDataUpdatedWineShelf) MyViewWineShelfHolder.this);
                    }
                });
                // ВАЖНО - продолжение метода после отработки второго потока
                // в интерфейсе onUpdatedWineShelf

                //TODO: действия похожие на onClick,  только с редактированим винной полки.
                // надо оптимизировать

                // Была нажата винная полка
                // Вызываем через интерфейс обработчик события "click на винной полке" clickWineShelf
                // в качестве обработчика выступает MainActivity,
                // т.е. clickWineShelfListener = MainActivity
                clickWineShelfListener.clickWineShelf(mShelfId);

                // Если текущая винная полка не совпадает с нажатой
                if (currentShelfId != mShelfId) {
                    // Красим выделенную полку в нормальный цвет
                    TextView tvOldWineShelf = currentView.findViewById(R.id.tvWineShelf);
//                    tvOldWineShelf.setTextColor(0xFF000000);
                    tvOldWineShelf.setTypeface(null, Typeface.NORMAL);
                    ConstraintLayout old_cl_02 = currentView.findViewById(R.id.cl_02);
                    old_cl_02.setBackgroundResource(R.drawable.my_wineshelfbox_background_normal);
                    old_cl_02.setAlpha(0.5F);

                    // Красим нажатую полку в выделенный цвет
                    TextView tvNewWineShelf = itemView.findViewById(R.id.tvWineShelf);
//                    tvNewWineShelf.setTextColor(0xFFBB86FC);
                    tvNewWineShelf.setTypeface(null, Typeface.BOLD);
                    ConstraintLayout new_cl_02 = itemView.findViewById(R.id.cl_02);
                    new_cl_02.setBackgroundResource(R.drawable.my_wineshelfbox_background_highlighted);
                    new_cl_02.setAlpha(1);

                    // Запоминаем для выделенной полки номер и вью
                    currentView = itemView;
                    currentShelfId = mShelfId;
                }
            }
            else // если введенное наименование пусто
            {
                Snackbar.make(((MainActivity)context).getCoordinator_layoutMain(),
                        R.string.shelf_name_must_not_be_empty,
                        Snackbar.LENGTH_LONG).show();
            }

//            Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: onDialogPositiveClickEditWineShelf Конец");

        }

        //==============================================================================
        // Интерфейс - действия, когда мы обновили винную полку
        // Обновление адаптера в ОСНОВНОМ потоке
        //==============================================================================
        @Override
        public void onUpdatedWineShelf() {
            AppExecuter.getInstance().getMainIO().execute(new Runnable() {
                @Override
                public void run() {
                    // Сообщаем зарегистрированным пользователям, что нажатый элемент изменился
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }

        //============================================================================
        // Пользователь коснулся отрицательной кнопки диалога - редактирование винной полки
        //============================================================================
        @Override
        public void onDialogNegativeClickEditWineShelf(EditWineShelfDialogFragment dialog) {

        }

        //============================================================================
        // Удаляем винную полку
        //============================================================================
        public void deleteWineShelf(long mShelfId) {

            // TODO: Метод необходимо оптимизировать!!!!
            //  В качестве образца нужно использовать раздел:
            //  - "Если текущая винная полка совпадает с нажатой"
            //  - "Если позиция винной полки не первая, удаляем и перемещаемся на шаг назад".
            //  Перерисовывать нечего не нужно - все перерисование происходит в "onBindViewHolder"

//            Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: deleteWineShelf");

            // Запоминаем позицию винной полки
            int i = getAdapterPosition();

            if (currentShelfId != mShelfId) // Если текущая винная полка не совпадает с нажатой
            {

//                Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: deleteWineShelf: текущая винная полка не совпадает с нажатой");

                //Удаляем винную полку из массива винных полок
                // Используя метод getAdapterPosition(), в массиве винных полок wineShelf
                // берем ту, которая соответствует нажатому меню
                wineShelfArray.remove(i);
                // Перерисовываем
                notifyItemRemoved(i);

                // Удаляем винную полку в базе данных по номеру винной полки во втором потоке
                AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.myDbManager.deleteWineShelf_Executer(mShelfId);
                    }
                });

            }
            else    // Если текущая винная полка совпадает с нажатой
            {

//                Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: deleteWineShelf: текущая винная полка совпадает с нажатой");

                if (i > 0)  // Если позиция винной полки не первая, удаляем и перемещаемся на шаг назад
                {

                    //========================================================================
                    // Образец для оптимизации

                    // Удаляем
                    //Удаляем винную полку из массива винных полог
                    // Используя метод getAdapterPosition(), в массиве винных полок wineShelf
                    // берем ту, которая соответствует нажатому меню
                    wineShelfArray.remove(i);

                    // Удаляем винную полку в базе данных по номеру винной полки во втором потоке
                    AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.myDbManager.deleteWineShelf_Executer(mShelfId);
                        }
                    });

                    // Запоминаем номер винной полки - назад на 1 позицию
                    currentShelfId = wineShelfArray.get(i-1).getId();

                    // Имитируем нажатие на винная полка
                    // Вызываем через интерфейс обработчик события "click на винной полке" clickWineShelf
                    // в качестве обработчика выступает MainActivity,
                    // т.е. clickWineShelfListener = MainActivity
                    clickWineShelfListener.clickWineShelf(currentShelfId);

                    //Перемещаемся на шаг назад
                    // Прокручиваем recyclerView назад на 1 позицию
                    recyclerViewListWineShelf.scrollToPosition(i - 1);

                    // Уведомите всех зарегистрированных наблюдателей об изменении набора данных
                    // TODO: при оптимизации метода можно поэкспериментировать
                    //  вместо notifyDataSetChanged() взять что-нидудь еще
                    notifyDataSetChanged();

                }
                else        // Если позиция винной полки первая, т.е. = 0
                            // удаляем и перемещаемся на шаг вперед
                {
                    if (wineShelfArray.size() > 1)  //Если кроме этой полки есть еще
                    {
//                        Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: deleteWineShelf: удаляем и перемещаемся на шаг вперед");

                        // Удаляем
                        //Удаляем винную полку из массива винных полог
                        // Используя метод getAdapterPosition(), в массиве винных полок wineShelf
                        // берем ту, которая соответствует нажатому меню
                        wineShelfArray.remove(i);
                        // Перерисовываем
                        notifyItemRemoved(i);

                        // Удаляем винную полку в базе данных по номеру винной полки во втором потоке
                        AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.myDbManager.deleteWineShelf_Executer(mShelfId);
                            }
                        });

                        //Перемещаемся на шаг вперед
                        // Прокручиваем recyclerView вперед на 1 позицию
                        // (единицу не прибавляем, т.к. уже первый элемент массива удален)
                        // т.е. i = 0
                        recyclerViewListWineShelf.scrollToPosition(i);
                        // Ищем RecyclerView.ViewHolder для позиции - вперед на 1 позицию
                        RecyclerView.ViewHolder mViewHolder =
                                recyclerViewListWineShelf.findViewHolderForAdapterPosition(i);

                        // Определяем номер винной полки - вперед на 1 позицию
                        WineShelf wineShelf = wineShelfArray.get(i);
                        // Запоминаем номер винной полки - вперед на 1 позицию
                        long shelfId = wineShelf.getId();

                        // Имитируем нажатие на винная полка
                        // Вызываем через интерфейс обработчик события "click на винной полке" clickWineShelf
                        // в качестве обработчика выступает MainActivity,
                        // т.е. clickWineShelfListener = MainActivity
                        clickWineShelfListener.clickWineShelf(shelfId);

//                        Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: deleteWineShelf: прошли clickWineShelf");

                        // Красим нажатую полку в выделенный цвет
                        TextView tvNewWineShelf = mViewHolder.itemView.findViewById(R.id.tvWineShelf);
//                        tvNewWineShelf.setTextColor(0xFFBB86FC);
                        tvNewWineShelf.setTypeface(null, Typeface.BOLD);
                        ConstraintLayout new_cl_02 = mViewHolder.itemView.findViewById(R.id.cl_02);
                        new_cl_02.setBackgroundResource(R.drawable.my_wineshelfbox_background_highlighted);
                        new_cl_02.setAlpha(1);

//                        Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: deleteWineShelf: покрасили полку");

                        // Запоминаем для выделенной полки номер и вью
                        currentView = mViewHolder.itemView;
                        currentShelfId = shelfId;

//                        Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: deleteWineShelf: Запомнили для выделенной полки номер и вью");

                    }
                    else // Первая полка единственноя (последняя)
                    {

//                        Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: deleteWineShelf: удаляем последню полку");

                        // Удаляем
                        //Удаляем винную полку из массива винных полог
                        // Используя метод getAdapterPosition(), в массиве винных полок wineShelf
                        // берем ту, которая соответствует нажатому меню
                        wineShelfArray.remove(i);
                        // Перерисовываем
                        notifyItemRemoved(i);

                        // Удаляем винную полку в базе данных по номеру винной полки во втором потоке
                        AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.myDbManager.deleteWineShelf_Executer(mShelfId);
                            }
                        });

                        // Винных полок больше нет
                        long shelfId = -1;

                        // Имитируем нажатие на винная полка
                        // Вызываем через интерфейс обработчик события "click на винной полке" clickWineShelf
                        // в качестве обработчика выступает MainActivity,
                        // т.е. clickWineShelfListener = MainActivity
                        clickWineShelfListener.clickWineShelf(shelfId);

                        // Запоминаем для выделенной полки номер и вью
//                        currentView = mViewHolder.itemView;
                        currentShelfId = shelfId;

//                        Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: deleteWineShelf: Запомнили для выделенной полки номер и вью");

                    }

                }
            }

//            Log.d(TAG, "WineShelfAdapter: MyViewWineShelfHolder: deleteWineShelf Конец");

        }

        //============================================================================
        // Загружаем фото из галереи
        //============================================================================
        public void takePhotoFromGallery() {

            Intent chooserPhoto = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            chooserPhoto.setType("image/*");

            //TODO: Не удалось передать в интенте chooserPhoto винную полку и позицию в адаптере.
            // Это временное решение
            // Приходится передавать через глобальную переменую

            // Передаем винную полку через глобальную переменную
            globalWineShelf = wineShelfArray.get(getAdapterPosition());
            // Передаем позицию адаптера через глобальную переменную
            globalPosition = getAdapterPosition();

/*            // Погружаем в интент винную полку (целиком) за счет того, что класс WineShelf сериализуем
            chooserPhoto.putExtra(CHOOSE_FROM_PICTURES_WINE_SHELF_FROM, wineShelfArray.get(getAdapterPosition()));
            // Погружаем в интент позицию адаптера
            chooserPhoto.putExtra(CHOOSE_FROM_PICTURES_POSITION_FROM, getAdapterPosition());*/

            // TODO: Заменить устаревший вызов startActivityForResult на более современное
            // Запускаем интент, ожидая результата
            ((AppCompatActivity)context).startActivityForResult(chooserPhoto, PICK_IMAGE_CODE_WINE_SHELF);

        }

        //============================================================================
        // Делаем новую фотографию
        //============================================================================
        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("QueryPermissionsNeeded")
        public void makePhoto() {

            //TODO: Это фактичечески копия метода

            File mPhotoFile;

            // Проверяем, есть ли разрешение на камеру
            int cameraPermission = ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.CAMERA);

            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {    // Если есть разрешение на камеру
                try {
                    // Создайте неявное намерение для захвата изображения.
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Убедитесь, что есть активность камеры для обработки намерения
                    if (takePictureIntent.resolveActivity(((AppCompatActivity)context).getPackageManager()) != null) {
                        // Запоминаем старый фото-файл и абсолютный путь к нему
    //            mOldPhotoFile = mPhotoFile;
    //            mOldCurrentPhotoPath = mCurrentPhotoPath;
                        // Обнуляем Фото-файл и абсолютный путь к нему
                        mPhotoFile = null;
    //                mCurrentPhotoPath = "";
                        // Генерируем новый фото-файл
                        // (при этом в mCurrentPhotoPath запоминается абсолютный путь к нему)
                        try {
                            mPhotoFile = createImageFile();
                        } catch (IOException ex) {
                            // Ошибка при создании файла
                            Toast.makeText(context, R.string.file_was_not_created, Toast.LENGTH_LONG).show();
                        }
                        // Продолжить, только если файл был успешно создан
                        if (mPhotoFile != null) {
                            // Возвращает URI-содержимого для данного файла mPhotoFile
                            // FileProvider может возвращать Uri содержимого только для путей к файлам,
                            // определенных в их элементе метаданных <paths>
                            Uri mPhotoURI = FileProvider.getUriForFile(context,
                                    "com.apushnikov.sommelier_notebook.fileprovider",
                                    mPhotoFile);
                            // В неявное намерение для захвата изображения кладем URI-содержимого
                            // для файла mPhotoFile
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);

                            //TODO: Не удалось передать в интенте chooserPhoto винную полку и позицию в адаптере.
                            // Это временное решение
                            // Приходится передавать через глобальную переменую

                            // Передаем винную полку через глобальную переменную
                            globalWineShelf = wineShelfArray.get(getAdapterPosition());
                            // Передаем позицию адаптера через глобальную переменную
                            globalPosition = getAdapterPosition();

                            // TODO: Заменить устаревший вызов startActivityForResult на более современное
                            // Вызываем интент и ожидаем результат
                            ((AppCompatActivity)context).startActivityForResult(takePictureIntent, REQUEST_LARGE_PHOTO_WINE_SHELF);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(context, R.string.error_occurred_while_trying_to_take_photo, Toast.LENGTH_LONG).show();
//                    e.printStackTrace();
                }
            }
            else                // Если нет разрешения на камеру, запрашиваем его
            {
                ((AppCompatActivity)context).requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        GlobalConstants.REQUEST_ID_CAMERA_PERMISSION);
            }
        }

    }

    //=================================================================================
    // updateAdapter - Обновляем адаптер используя newList - List<WineShelf>
    //      - Очищаем спискок List<WineShelf> записей винных полок
    //      - Добавляет все элементы в указанной коллекции в конец этого списка в том порядке,
    //          в котором они возвращаются итератором указанной коллекции (необязательная операция).
    //          Поведение этой операции не определено, если указанная коллекция изменяется во время
    //          выполнения операции.
    //          (Обратите внимание, что это произойдет, если указанная коллекция является этим
    //
    //      - Уведомите всех зарегистрированных наблюдателей об изменении набора данных
    //=================================================================================
    public void updateAdapter(List<WineShelf> newList) {

//        Log.d(TAG,"WineShelfAdapter: updateAdapter");

        // Очищаем список винных полок (хотя он и должен быть пустым)
        wineShelfArray.clear();
        // Список wineShelfArray заменяем новым списком newList
        wineShelfArray.addAll(newList);

/*        // Список photoArrayOld заменяем новым списком newList (для запоминания старых фотографий)
        photoArrayOld.addAll(newList);*/

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

//        Log.d(TAG,"WineShelfAdapter: updateAdapter Конец");

    }

    //============================================================================
    // Обновляем адаптер, добавляя в данные адаптера wineShelf и используя notifyItemInserted
    //============================================================================
    public void updateAdapterWineShelfInserted(WineShelf wineShelf) {

//        Log.d(TAG,"PhotoAdapter: updateAdapterWineShelfInserted");

        // Опрелеляем размер массива wineShelf
        int i = wineShelfArray.size();
        // Добавляем винную полку wineShelf в конец массива
        wineShelfArray.add(wineShelf);

        // Красим старую выделенную полку в нормальный цвет
        if (currentView != null) {
            TextView tvOldWineShelf = currentView.findViewById(R.id.tvWineShelf);
//            tvOldWineShelf.setTextColor(0xFF000000);
            tvOldWineShelf.setTypeface(null, Typeface.NORMAL);
            ConstraintLayout old_cl_02 = currentView.findViewById(R.id.cl_02);
            old_cl_02.setBackgroundResource(R.drawable.my_wineshelfbox_background_normal);
            old_cl_02.setAlpha(0.5F);

        }

        // Устанавливаем новый Текущий номер выделенной полки - во вновь созданную винную полку
        currentShelfId = wineShelf.getId();

        // Сообщает всем зарегистрированным наблюдателям, что отраженный элемент i был добавлен заново.
        // Предмет, который ранее находился на i-й позиции, теперь находится на позиции i + 1.
        notifyItemInserted(i);

//        Log.d(TAG,"PhotoAdapter: updateAdapterWineShelfInserted Конец");

    }

    //============================================================================
    // Обновляем адаптер, обновляя один элемент адаптера wineShelf и используя notifyItemChanged
    //============================================================================
    public void updateAdapterWineShelfUpdated(int position) {

        // Сообщает всем зарегистрированным наблюдателям, что отраженный элемент position был обновлен
        notifyItemChanged(position);

    }

    //============================================================================
    // Обновляем элемент массива wineShelfArray в позици position
    //  - заменяет photoAbsolutePathShelf
    //============================================================================
    public void updateWineShelfArrayWithPhotoAbsolutePathShelf(int position, String photoAbsolutePathShelf) {

        wineShelfArray.get(position).setPhotoAbsolutePathShelf(photoAbsolutePathShelf);
    }

    //==============================================================================
    // Интерфейс - действия, когда мы повернули фото
    // Обновление адаптера в ОСНОВНОМ потоке
    //==============================================================================
    @Override
    public void onPhotoFileRotation(String newPhotoPath, int position) {

        // Присваиваем новый путь к фотофайлу
        wineShelfArray.get(position).setPhotoAbsolutePathShelf(newPhotoPath);

        MainActivity.myDbManager.updateWineShelfJustSave_Executer(
                wineShelfArray.get(position));

        AppExecuter.getInstance().getMainIO().execute(new Runnable() {
            @Override
            public void run() {
                // Сообщите зарегистрированным наблюдателям, что позиция изменилась.
                // Эквивалентно вызову notifyItemChanged (position, null) ;.
                notifyItemChanged(position);
            }
        });

    }

    // ============================================================================
    // Генерация имени фото-файла
    // TODO: Этот метод нужно перенести, вместе с файловыми операциями в класс MyDbManager
    // ============================================================================
    private File createImageFile() throws IOException {
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
        File storageDir = ((AppCompatActivity)context).getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Генерируем имя фото-файла
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        //TODO: Передача через глобальную переменную. Это временное решение

        // Сохранить абсолютный путь к файлу
        globalPhotoAbsolutePathShelf = image.getAbsolutePath();

        // Возвращаем имя фото-файла
        return image;
    }

    // ============================================================================
    // По номеру текущей винной полки currentShelfId определяем позицию адаптера
    // ============================================================================
    public int positionForShelfId(long shelfId) {

        // Цикл по массиву винных полок
        int returnPosition = 0;
        if (wineShelfArray.size()>0) {
            for (WineShelf wineShelf : wineShelfArray) {
                if (shelfId == wineShelf.getId()) {
                    break;
                }
                returnPosition++;
            }
        } else {
            returnPosition = -1;
        }
        return returnPosition;
    }


}
