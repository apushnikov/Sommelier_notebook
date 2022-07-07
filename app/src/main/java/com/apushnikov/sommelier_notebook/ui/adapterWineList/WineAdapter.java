package com.apushnikov.sommelier_notebook.ui.adapterWineList;


import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.global_type_of_assessmen;
import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.theSecondThreadIsRunning;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener;
import androidx.recyclerview.widget.RecyclerView;

import com.apushnikov.sommelier_notebook.EditActivity;
import com.apushnikov.sommelier_notebook.Global.GlobalConstants;
import com.apushnikov.sommelier_notebook.MainActivity;
import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.db.AppExecuter;
import com.apushnikov.sommelier_notebook.db.Photo;
import com.apushnikov.sommelier_notebook.db.Wine;
import com.apushnikov.sommelier_notebook.swipe.SwipeRevealLayout;
import com.apushnikov.sommelier_notebook.swipe.ViewBinderHelper;
import com.apushnikov.sommelier_notebook.utilities.Utilities;

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
// WineAdapter - в текущем фрагменте строит список вин
public class WineAdapter extends RecyclerView.Adapter<WineAdapter.MyViewHolder> {

    //===========================================================================================
    // region: Интерфейс
    //==========================================================================================

    //============================================================================
    // Описываем интерфейс onDeleteWineInterfaceListener.
    // Было удалено вино
    // В DeleteWineInterfaceListener метод deleteWineInterface
    // Этот интерфейс будет реализовывать Activity.
    //============================================================================
    public interface onDeleteWineInterfaceListener {
        public void deleteWineInterface();
    }
    public onDeleteWineInterfaceListener deleteWineInterfaceListener;
    //============================================================================

    // endregion

    //===========================================================================================
    // region: Поля и константы
    //==========================================================================================

    private static final String TAG = "myLogs";     //Для логов

    // Это конткест MainActivity
    //      - контекст нужен для в LayoutInflater для раздувания отдельной строки
    //      - для передачи в MyViewHolder
    //          - (в MyViewHolder контекст нужен для вызова интента для редактирования)
    private Context context;
    // wineArray - это список List<Wine> вин
    private List<Wine> wineArray;

    // ViewBinderHelper предоставляет быстрое и простое решение для восстановления
    // * открытого / закрытого состояния элементов в RecyclerView, ListView, GridView или
    // * любом представлении, которое требует, чтобы его дочернее представление связывало
    // * представление с объектом данных.
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();

    // endregion

    //===========================================================================================
    // region: Методы
    //===========================================================================================

    //============================================================================
    // Конструктор
    //      - Запоминаем контекст
    //      - Создаем пустой массив - спискок List<Wine> вин
    //============================================================================
    public WineAdapter(Context context) {

//        Log.d(TAG, "WineAdapter: Конструктор WineAdapter");

        // Запоминаем контекст - это конткест MainActivity
        this.context = context;
        // Создаем пустой массив - спискок List<Wine> вин
        wineArray = new ArrayList<>();

        // раскомментируйте, если хотите при удалении открывать только одну строку за раз
        binderHelper.setOpenOnlyOne(true);

        try {
            // Определяем слушатель события - Было удалено вино
            deleteWineInterfaceListener = (onDeleteWineInterfaceListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onDeleteWineInterfaceListener");
        }

//        Log.d(TAG, "WineAdapter: Конструктор WineAdapter Конец");
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
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        Log.d(TAG, "WineAdapter: onCreateViewHolder");

        // Раздуваем view для отдельной строки вина
        // Это конткест MainActivity
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_list_layout,
                parent,
                false);

//        Log.d(TAG, "WineAdapter: onCreateViewHolder Конец");

        return new MyViewHolder(view, context, wineArray);
    }

    //============================================================================
    // onBindViewHolder (): RecyclerView вызывает этот метод, чтобы связать ViewHolder с данными.
    // Метод извлекает соответствующие данные и использует их для заполнения макета
    // держателя представления.
    // Например, если RecyclerView отображает список имен, метод может найти соответствующее
    // имя в списке и заполнить виджет TextView держателя представления
    //============================================================================
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final MyViewHolder mHolder = (MyViewHolder) holder;

//        Log.d(TAG, "WineAdapter: onBindViewHolder");

        // Стоим строку даты в формате ДД.ММ.ГГГГ
        String mGetDateWine = Utilities.getDateWine(
                2,
                wineArray.get(position).getDateWineDay(),
                wineArray.get(position).getDateWineMonth(),
                wineArray.get(position).getDateWineYear());
        // setData - заполняет объекте-держателе представления TextView tvTitle:
        //      - берет спискок List<Wine> вин
        //      - берет элемент position
        //      - берет поле nameWine из Wine (описание одной записи вина)
        //      - берет поле rating из Wine (Моя оценка (пятибальная шкала) – набор звездочек)
        //      - строку даты в формате ДД.ММ.ГГГГ

//        Log.d(TAG, "WineAdapter: onBindViewHolder: готовимся запустить setData с именем вина: " + wineArray.get(position).getNameWine());

        mHolder.setData(
                wineArray.get(position).getId(),
                wineArray.get(position).getNameWine(),
                wineArray.get(position).getRating(),
                mGetDateWine);

        // Используйте ViewBindHelper для восстановления и сохранения открытого / закрытого
        // состояния SwipeRevealView, поместите уникальный строковый идентификатор в
        // качестве значения, может быть любой строкой, которая однозначно определяет данные
        final String data = Long.toString(wineArray.get(position).getId());
        binderHelper.bind(mHolder.swipeLayout, data);

//        Log.d(TAG, "WineAdapter: onBindViewHolder Конец");
    }

    //============================================================================
    // getItemCount (): RecyclerView вызывает этот метод, чтобы получить размер набора данных.
    // Например, в приложении адресной книги это может быть общее количество адресов.
    // RecyclerView использует это, чтобы определить, когда больше нет элементов,
    // которые могут быть отображены
    //============================================================================
    @Override
    public int getItemCount() {

//        Log.d(TAG, "WineAdapter: getItemCount");

        return wineArray.size();
    }

    //============================================================================
    // Только если вам нужно восстановить открытое / закрытое состояние при изменении ориентации.
    // Вызовите этот метод в {@link android.app.Activity # onSaveInstanceState (Bundle)}
    //============================================================================
    public void saveStates(Bundle outState) {
        binderHelper.saveStates(outState);
    }

    //============================================================================
    // Только если вам нужно восстановить открытое / закрытое состояние при изменении ориентации.
    // Вызовите этот метод в {@link android.app.Activity # onSaveInstanceState (Bundle)}
    //============================================================================
    public void restoreStates(Bundle inState) {
        binderHelper.restoreStates(inState);
    }

    //=================================================================================

    //=================================================================================
    // updateAdapter - Обновляем адаптер используя newList - List<Wine>
    //      - Очищаем спискок List<Wine> записей вин
    //      - Добавляет все элементы в указанной коллекции в конец этого списка в том порядке,
    //          в котором они возвращаются итератором указанной коллекции (необязательная операция).
    //          Поведение этой операции не определено, если указанная коллекция изменяется во время
    //          выполнения операции.
    //          (Обратите внимание, что это произойдет, если указанная коллекция является этим
    //
    //      - Уведомите всех зарегистрированных наблюдателей об изменении набора данных
    //=================================================================================
    public void updateAdapter(List<Wine> newList) {

//        Log.d(TAG, "WineAdapter: updateAdapter");

        wineArray.clear();
        wineArray.addAll(newList);
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

//        Log.d(TAG, "WineAdapter: updateAdapter Конец");

    }



    // endregion

    //===========================================================================================
    // region: Класс MyViewHolder
    //===========================================================================================

    //=================================================================================
    // Каждый отдельный элемент в списке определяется объектом-держателем представления.
    //      Когда держатель представления создан, с ним не связаны никакие данные.
    //      После создания держателя представления RecyclerView привязывает его к своим данным.
    //      Вы определяете держателя представления, расширяя RecyclerView.ViewHolder.
    //=================================================================================
    class MyViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener,
            OnMenuItemClickListener {

        //===========================================================================================
        // region: Поля и константы
        //==========================================================================================

        private SwipeRevealLayout swipeLayout;  // Корневой View типа SwipeRevealLayout
        private View tap_layout;       // View для содержимого элемента
        private View deleteLayout;      // View для удаления
        private ImageButton iButtonWineMoreVert;

        final ImageView imagePhoto;
        final TextView tvNameWine;
        final ImageView imageRating;
        private TextView tvDateWine = null;
        private Context context;
        private List<Wine> wineArray;

        // TODO: Зачем сюда передается List<Wine> mainArray. Он не используется,
        //  а только занимает место

        // endregion

        //===========================================================================================
        // region: Методы
        //===========================================================================================

        //============================================================================
        // Конструктор
        //============================================================================
        public MyViewHolder(@NonNull View itemView, Context context, List<Wine> wineArray) {
            super(itemView);

//            Log.d(TAG, "WineAdapter->MyViewHolder: Конструктор MyViewHolder");

            swipeLayout  = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
            tap_layout = itemView.findViewById(R.id.tap_layout);
            deleteLayout = itemView.findViewById(R.id.delete_layout);
            iButtonWineMoreVert = itemView.findViewById(R.id.iButtonWineMoreVert);

            // На itemView навешиваем обработчик на короткий клик
            tap_layout.setOnClickListener(this);
            deleteLayout.setOnClickListener(this);
            iButtonWineMoreVert.setOnClickListener(this);

            // Запоминаем контекст
            this.context = context;
            // Запоминаем wineArray - спискок List<Wine> вин
            this.wineArray = wineArray;
            // Находим в объекте-держателе представления imagePhoto
            imagePhoto = itemView.findViewById(R.id.imagePhoto);
            // Находим в объекте-держателе представления tvNameWine
            tvNameWine = itemView.findViewById(R.id.tvNameWine);
            // Находим в объекте-держателе представления imageRating
            imageRating = itemView.findViewById(R.id.imageRating);
            // Находим в объекте-держателе представления tvDateWineInList
            tvDateWine = itemView.findViewById(R.id.tvDateWine);

//            Log.d(TAG, "WineAdapter->MyViewHolder: Конструктор MyViewHolder Конец");

        }

        //============================================================================
        // setData - заполняет объекте-держателе представления
        //============================================================================
        public void setData(long wineId, String nameWine, int rating, String dateWine) {

//            Log.d(TAG, "WineAdapter->MyViewHolder: setData");

            // На экран выводим значение nameWine
            tvNameWine.setText(nameWine);

//            Log.d(TAG, "WineAdapter->MyViewHolder: setData: На экран выводим значение nameWine: " + nameWine);

            // На экран выводим значение даты
            tvDateWine.setText("Дата: " + dateWine);

            // На экран выводим значение оценки
            switch (global_type_of_assessmen) {
                case "type_of_assessmen_wineglass":     // Если в качестве оценки показывать бокальчики
                    switch (rating) {
                        case 0:
                            imageRating.setImageResource(R.drawable.star_on_0_1);
                            break;
                        case 1:
                            imageRating.setImageResource(R.drawable.star_on_1_1);
                            break;
                        case 2:
                            imageRating.setImageResource(R.drawable.star_on_2_1);
                            break;
                        case 3:
                            imageRating.setImageResource(R.drawable.star_on_3_1);
                            break;
                        case 4:
                            imageRating.setImageResource(R.drawable.star_on_4_1);
                            break;
                        case 5:
                            imageRating.setImageResource(R.drawable.star_on_5_1);
                            break;
                    }
                    break;
                case "type_of_assessmen_star":          // Если в качестве оценки показывать звездочки
                    switch (rating) {
                        case 0:
                            imageRating.setImageResource(R.drawable.star_on_0);
                            break;
                        case 1:
                            imageRating.setImageResource(R.drawable.star_on_1);
                            break;
                        case 2:
                            imageRating.setImageResource(R.drawable.star_on_2);
                            break;
                        case 3:
                            imageRating.setImageResource(R.drawable.star_on_3);
                            break;
                        case 4:
                            imageRating.setImageResource(R.drawable.star_on_4);
                            break;
                        case 5:
                            imageRating.setImageResource(R.drawable.star_on_5);
                            break;
                    }
                    break;
            }


            // На экран выводим главное фото

            // По номеру вина определяем путь к главному фото
            final Photo[] mainPhoto = {null};

            theSecondThreadIsRunning = true;
//            Log.d(TAG,"Начало выполнения 2 потока");

            // Выбираем имеющиеся фото для вина wineId
            // Создаем новый поток
            new Thread(new Runnable() {
                // У интерфейса Runnable() необходимо переопределить метод run (в котором будут вычисления)
                public void run() {
                    mainPhoto[0] = (Photo) MainActivity.photoDao.getMainPhotoByWineId(wineId);

                    theSecondThreadIsRunning = false;
//                    Log.d(TAG,"Конец выполнения 2 потока");

                }
            }).start();

            while (theSecondThreadIsRunning){ // линия 1
                // TODO: Здесь пока оправдано
                //  плохай стиль 1) передача через глобальную переменную, 2) задержка потока IU
                // Ждем - выполняется второй поток
            }

            if (mainPhoto[0] != null)       // Если главное фото найднено, Рисуем фотографию
            {
                // Берем абсолютный путь к фото
                String photoAbsolutePath = mainPhoto[0].getPhotoAbsolutePath();

                // Получите размеры представления imageView
                int targetW = imagePhoto.getWidth();
                int targetH = imagePhoto.getHeight();

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
                BitmapFactory.decodeFile(photoAbsolutePath, bmOptions);

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
                Bitmap mBitmap = BitmapFactory.decodeFile(photoAbsolutePath, bmOptions);

                imagePhoto.setImageBitmap(mBitmap);

            }
            else                            // Если главное фото НЕ найднено
            {

                Bitmap mBitmap = null;
                imagePhoto.setImageBitmap(mBitmap);

/*                // Выводим пустое фото
                imagePhoto.setBackgroundResource(R.drawable.ic_wine_empty);*/
            }

//            Log.d(TAG, "WineAdapter->MyViewHolder: setData Конец");

        }

        //============================================================================
        // Нажатие на кнопку "Удаление вина"
        // - по нажатию запускаем строку на редактирование
        // По нажатию на сандвич, запускаем контекстное меню
        //============================================================================
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // Нажатие на кнопку "Удаление вина"
                case R.id.delete_layout:
                    // задаем вопрос и удаляем вино
                    askQuestionAndRemoveWine();
                    break;

                // - по нажатию запускаем строку на редактирование
                case R.id.tap_layout:
                    // Создаем новый интент
                    Intent intent = new Intent(context, EditActivity.class);
                    // Параметры, переданные в интент:
                    //      - Константа WINE_INTENT = "wine_intent"
                    //      - mainArray - это список List<Wine> вин
                    //          - get() - Возвращает элемент в указанной позиции в этом списке
                    //              - getAdapterPosition() - Возвращает позицию адаптера элемента,
                    //              представленного этим ViewHolder
                    intent.putExtra(GlobalConstants.WINE_INTENT, wineArray.get(getAdapterPosition()));
                    // Параметры, переданные в интент:
                    //      - Константа EDIT_STATE = "edit_state"
                    //      - Значение true (т.е. передаем интент для редактирования вина)
                    intent.putExtra(GlobalConstants.EDIT_STATE, true);

                    context.startActivity(intent);
                    break;

                // По нажатию на сандвич, запускаем контекстное меню
                case R.id.iButtonWineMoreVert:
                    PopupMenu popup = new PopupMenu(context, view);
                    popup.setOnMenuItemClickListener((OnMenuItemClickListener) this);
                    popup.inflate(R.menu.menu_wine_adapter);
                    popup.show();
                    break;

            }


        }

        //============================================================================
        // Создание контекстного меню
        //============================================================================
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            // TODO: Нужно создавать контекстное меню из ресурса.
            //  Сейчас меню создается 2-мя способами - это неправильно

            // Создаем контекстное меню
            MenuItem myActionItem1 = menu.add(0,R.id.menu_wine_delete,0,"Удалить");

            // Навешиваем обработчик на нажатия пунтов контекстного меню
            myActionItem1.setOnMenuItemClickListener((MenuItem.OnMenuItemClickListener) this);
        }

        //============================================================================
        // Реакция на нажатие контекстного меню
        //============================================================================
        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {

                case R.id.menu_wine_delete:      // Удалить
                    // задаем вопрос и удаляем вино
                    askQuestionAndRemoveWine();
                    break;

            }
            return true;
        }

        //============================================================================
        // задаем вопрос и удаляем вино
        //============================================================================
        public void askQuestionAndRemoveWine() {
            // Запоминаем позицию вина и номер вина
            int i = getAdapterPosition();
            // Зопоминаем номер вина
            long mWineId = wineArray.get(i).getId();
            // Запоминаем наименование вина
            String mNameWine = wineArray.get(i).getNameWine();
            //Подготовка сообщения для диалога о вине
            String mMessage = "Вино \"" + mNameWine + "\" будет удалено." +
                    "\nУдаляем?";

            // Используйте класс Builder для удобного построения диалогов
            AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogTheme);
            builder
                    .setTitle(R.string.dialog_delete_wine)    // Заголовок диалога
                    .setMessage(mMessage)    // Заголовок диалога
                    // Позитивная кнопка - удалить вино
                    .setPositiveButton(R.string.dialog_delete_wine_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Удаляем вино
                            deleteWine(i, mWineId);
                        }
                    })
                    // Отрицательная кнопка - Отказ
                    .setNegativeButton(R.string.dialog_delete_wine_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Отказ от удаления.
                            // Задвигаем обратно кнопку удаления
                            final String data = Long.toString(wineArray.get(i).getId());
                            binderHelper.closeLayout(data);
                        }
                    });
            // Создаем диалог
            AlertDialog alert = builder.create();
            // Показываем диалог
            alert.show();
        }


        //============================================================================
        // Удаляем вино
        //      i - позицию вина в массиве вин wineArray
        //      mWineId - номер вина в базе данных
        //============================================================================
        public void deleteWine(int i, long mWineId) {

            //Удаляем вино из массива
            wineArray.remove(i);
            // Перерисовываем
            notifyItemRemoved(i);

            // Было удалено вино
            // Вызываем через интерфейс обработчик события deleteWineInterface
            // в качестве обработчика выступает MainActivity,
            // т.е. clickWineShelfListener = MainActivity
            deleteWineInterfaceListener.deleteWineInterface();

            // Удаляем вино во втором потоке
            AppExecuter.getInstance().getSubIO().execute(new Runnable() {
                @Override
                public void run() {
                    MainActivity.myDbManager.deleteWine_Executer(mWineId);
                }
            });

        }

        // endregion

    }

    // endregion

}