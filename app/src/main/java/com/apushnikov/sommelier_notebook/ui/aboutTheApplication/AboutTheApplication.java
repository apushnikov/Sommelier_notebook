package com.apushnikov.sommelier_notebook.ui.aboutTheApplication;

import static com.apushnikov.sommelier_notebook.Global.GlobalConstants.isTheProfessionalVersion;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.billing.makePurchase.MakePurchase;
import com.apushnikov.sommelier_notebook.ui.screenRateAndReview.ScreenRateAndReview;

public class AboutTheApplication extends AppCompatActivity implements View.OnClickListener {

    ImageView imageFullGlass;
    TextView tvIsPremiumText;
    TextView tvIsFreeText;
    Button btGoPremium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_the_application);

        // Инициализируем:
        //      - панель инструментов Toolbar
        initToolbar();

        // Инициализируем переменные экрана
        initVariables();

    }


    //====================================================================================
    // Инициализируем:
    //      - панель инструментов Toolbar
    //====================================================================================
    private void initToolbar() {

        // Инициализация toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAbout);
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

        // Заголовок toolbar
        toolbar.setTitle("О приложении");
    }

    //====================================================================
    // Инициализируем переменные экрана
    //====================================================================
    private void initVariables() {

        imageFullGlass = findViewById(R.id.imageFullGlass);
        tvIsPremiumText = findViewById(R.id.tvIsPremiumText);
        tvIsFreeText = findViewById(R.id.tvIsFreeText);
        btGoPremium = findViewById(R.id.btGoPremium);

//        ImageView ivAuthorView = findViewById(R.id.ivAuthorView);
        ImageView imageAuthorInstagram = findViewById(R.id.imageAuthorInstagram);
//        ImageView ivDeveloperView = findViewById(R.id.ivDeveloperView);
        ImageView imageDeveloperEMail = findViewById(R.id.imageDeveloperEMail);
        ImageView imageDeveloperWhatsApp = findViewById(R.id.imageDeveloperWhatsApp);
//        ImageView imageDeveloperViber = findViewById(R.id.imageDeveloperViber);
//        ImageView imageDeveloperTelegram = findViewById(R.id.imageDeveloperTelegram);
        Button btRateAndReview = findViewById(R.id.btRateAndReview);
//        Button btReview = findViewById(R.id.btReview);
        Button btShareTheApp = findViewById(R.id.btShareTheApp);
        Button btGoPremium = findViewById(R.id.btGoPremium);

//        ivAuthorView.setOnClickListener(this);
        imageAuthorInstagram.setOnClickListener(this);
//        ivDeveloperView.setOnClickListener(this);
        imageDeveloperEMail.setOnClickListener(this);
        imageDeveloperWhatsApp.setOnClickListener(this);
//        imageDeveloperViber.setOnClickListener(this);
//        imageDeveloperTelegram.setOnClickListener(this);
        btRateAndReview.setOnClickListener(this);
//        btReview.setOnClickListener(this);
        btShareTheApp.setOnClickListener(this);
        btGoPremium.setOnClickListener(this);

    }

    //====================================================================
    // onClick
    //====================================================================
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        // по id определеяем кнопку, вызвавшую этот обработчик
        switch (view.getId()) {

            // Автор идеи - Instagram
            case R.id.imageAuthorInstagram:
                // Преобразуем стоковый ресурс (который int) в тип String
                String stringAuthorInstagram = this.getString(R.string.tvAuthorInstagram);
                openWeb(stringAuthorInstagram);
                break;

            // Разработчик - EMail
            case R.id.imageDeveloperEMail:
                send_e_mail();
                break;

            // Разработчик - WhatsApp
            case R.id.imageDeveloperWhatsApp:
                send_WhatsApp();
                break;

/*            // Разработчик - Viber
            case R.id.imageDeveloperViber:

                Toast.makeText(this, "Здесь будет обработка - Viber", Toast.LENGTH_LONG).show();
                break;*/

/*            // Разработчик - Telegram
            case R.id.imageDeveloperTelegram:

                Toast.makeText(this, "Здесь будет обработка - Telegram", Toast.LENGTH_LONG).show();
                break;*/

            // Оценка и отзыв
            case R.id.btRateAndReview:
                goRateAndReview();
                break;

/*            // Оставить отзыв - Отзыв
            case R.id.btReview:

                Toast.makeText(this, "Здесь будет обработка - Отзыв", Toast.LENGTH_LONG).show();
                break;*/

            // Поделитесь приложением
            case R.id.btShareTheApp:

                Toast.makeText(this, "Здесь будет обработка - Поделитесь приложением", Toast.LENGTH_LONG).show();
                break;

            // Перейти на Premium
            case R.id.btGoPremium:
                goPremium();
                break;


        }

    }

    //=============================================================================
    // Открывает Web с указанным адресом
    //=============================================================================
    private void openWeb(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    //=============================================================================
    // Посылает письмо по e-mail
    //=============================================================================
    @SuppressLint("IntentReset")
    private void send_e_mail() {
        Intent i = new Intent(Intent.ACTION_SEND);

        i.setData(Uri.parse("mailto:"));
        i.setType("text/plain");

        // Преобразуем стоковый ресурс (который int) в тип String
        String stringMyEMail = this.getString(R.string.tvMyEMail);
        i.putExtra(Intent.EXTRA_EMAIL, new String[] {stringMyEMail});

        // Преобразуем стоковый ресурс (который int) в тип String
        String stringEXTRA_SUBJECT = this.getString(R.string.tvEXTRA_SUBJECT);
        String stringApp_name = this.getString(R.string.app_name);
        i.putExtra(Intent.EXTRA_SUBJECT, stringEXTRA_SUBJECT + stringApp_name + "\"");

        // Преобразуем стоковый ресурс (который int) в тип String
        String stringEXTRA_TEXT = this.getString(R.string.tvEXTRA_TEXT);
        i.putExtra(Intent.EXTRA_TEXT, stringEXTRA_TEXT);

        startActivity(i);
    }

    //=============================================================================
    // Посылает сообщение по WhatsApp
    //=============================================================================
    private void send_WhatsApp() {

        String packageName = "com.whatsapp";

        try {
            // Преобразуем стоковый ресурс (который int) в тип String
            String stringMyPhoneNumber = this.getString(R.string.tvMyPhoneNumber);
            String smsNumber = stringMyPhoneNumber + "@s.whatsapp.net";

            Uri uri = Uri.parse("smsto:" + smsNumber);
            Intent i = new Intent(Intent.ACTION_SENDTO, uri);
            i.putExtra("jid", smsNumber);
            i.setPackage(packageName);
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.tvMyMakeSureWhatsAppInstalled, Toast.LENGTH_SHORT).show();
        }

    }

    //====================================================================================
    // Перейти на Premium
    //====================================================================================
    public void goPremium() {

        // TODO: Это копия метода goPremium() из MainActivity. Устранить дублирование

        // Создаем новый интент
        Intent intent = new Intent(this, MakePurchase.class);
        // Запускаем активность
        startActivity(intent);

        //Закрываем окно
        finish();
    }

    //====================================================================================
    // Вызвать окно оценки и отзыва
    //====================================================================================
    public void goRateAndReview() {

        // Создаем новый интент
        Intent intent = new Intent(this, ScreenRateAndReview.class);
        // Запускаем активность
        startActivity(intent);

    }



    /**====================================================================================
     * onResume()
     *
     * ====================================================================================
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Проверка премиума
        premiumCheck();
    }

    /**====================================================================================
     * premiumCheck() - Проверка премиума
     *
     * ====================================================================================
     */
    private void premiumCheck() {

        // Если версия профессиональная
        if (isTheProfessionalVersion) {
            // Показываем бокал
            imageFullGlass.setVisibility(View.VISIBLE);
            tvIsPremiumText.setVisibility(View.VISIBLE);
            tvIsFreeText.setVisibility(View.GONE);
            // Запрещаем нажимать на кнопку Премиум
            btGoPremium.setEnabled(false);
        }
        // Если версия НЕ профессиональная
        else {
            // Скрываем бокал
            imageFullGlass.setVisibility(View.GONE);
            tvIsPremiumText.setVisibility(View.GONE);
            tvIsFreeText.setVisibility(View.VISIBLE);
            // Разрешаем нажимать на кнопку Премиум
            btGoPremium.setEnabled(true);
        }

    }

}