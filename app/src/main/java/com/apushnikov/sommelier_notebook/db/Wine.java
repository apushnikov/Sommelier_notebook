package com.apushnikov.sommelier_notebook.db;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;

//================================================================
// Класс помечается аннотацией Entity.
// Wine (вино) - это описание одной записи вина
// Включаем интерфейс сериализации, для того, что бы передавать в интенте сразу весь объект
//================================================================
@Entity(foreignKeys = @ForeignKey(
        entity = WineShelf.class,
        parentColumns = "id",
        childColumns = "shelfId",
        onDelete = CASCADE))
public class Wine implements Serializable {

    // =====================================================================
    // ПОЛЯ
    // =====================================================================

    // Номер винной карточки
    // С АВТОГЕНЕРАЦИЕЙ!!!
    @PrimaryKey(autoGenerate = true)
    public long id;

    // Номер винной полки (ссылка на винную полку)
    @ColumnInfo(index = true)
    public long shelfId;

    // Наименование вина
    public String nameWine;

    // Дата дегустации
    // День
    public int dateWineDay;
    // Месяц - диапазон от 1 до 12 (при передаче в окно выбора даты нужно уменьшать на 1)
    public int dateWineMonth;
    // Год
    public int dateWineYear;

    // Место дегустации
    public String tastingPlace;

    // ================= Общая информация (раздел) =========================
    // Страна
    public String country;

    // Регион
    public String region;

    // TODO: выбор из выпадающего списка: красное, белое, розовое, … (уточнить список)
    //  или набор радиокнопок
    // Сорт
    public int sort = 0;

    // Сорт винограда
    public String grapeSort;

    // Год урожая
    public int year;

    // Крепость
    public float strength;

    // Цена
    public float price;

    // Производитель
    public String producer;

    // Дистрибьютор
    public String distributor;
    // =====================================================================

    // ====================== Дегустационные заметки (раздел) ==============
    // ====================== Внешний вид (подраздел) ======================
    // Внешний вид
    public String appearance;
    // =====================================================================

    // ====================== Аромат (подраздел) ===========================
    // Аромат (характеристики)
    public String aroma;
    // =====================================================================

    // ====================== Вкус/Послевкусие (подраздел) =================
    // Вкусовые характеристики
    public String taste;
    // =====================================================================

    // ====================== Дегустационные заметки продолжение (раздел) ==
    // Потенциал хранения
    public String storagePotential;

    // Температура подачи
    public String servingTemperature;

    // Гастропары (гастрономические компаньоны)
    public String gastronomicPartners;

    // Место покупки (где куплено)
    public String tastingPurchase;

    // Заметки/впечатления
    public String notes;

    // Моя оценка (пятибальная шкала) – набор звездочек
    public int rating = 0;
    // =====================================================================

    // =====================================================================
    // МЕТОДЫ
    // =====================================================================


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getShelfId() {
        return shelfId;
    }

    public void setShelfId(long shelfId) {
        this.shelfId = shelfId;
    }

    public String getNameWine() {
        return nameWine;
    }

    public void setNameWine(String nameWine) {
        this.nameWine = nameWine;
    }

    public int getDateWineDay() {
        return dateWineDay;
    }

    public void setDateWineDay(int dateWineDay) {
        this.dateWineDay = dateWineDay;
    }

    public int getDateWineMonth() {
        return dateWineMonth;
    }

    public void setDateWineMonth(int dateWineMonth) {
        this.dateWineMonth = dateWineMonth;
    }

    public int getDateWineYear() {
        return dateWineYear;
    }

    public void setDateWineYear(int dateWineYear) {
        this.dateWineYear = dateWineYear;
    }

    public String getTastingPlace() {
        return tastingPlace;
    }

    public void setTastingPlace(String tastingPlace) {
        this.tastingPlace = tastingPlace;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getGrapeSort() {
        return grapeSort;
    }

    public void setGrapeSort(String grapeSort) {
        this.grapeSort = grapeSort;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }

    public String getAppearance() {
        return appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public String getAroma() {
        return aroma;
    }

    public void setAroma(String aroma) {
        this.aroma = aroma;
    }

    public String getTaste() {
        return taste;
    }

    public void setTaste(String taste) {
        this.taste = taste;
    }

    public String getStoragePotential() {
        return storagePotential;
    }

    public void setStoragePotential(String storagePotential) {
        this.storagePotential = storagePotential;
    }

    public String getServingTemperature() {
        return servingTemperature;
    }

    public void setServingTemperature(String servingTemperature) {
        this.servingTemperature = servingTemperature;
    }

    public String getGastronomicPartners() {
        return gastronomicPartners;
    }

    public void setGastronomicPartners(String gastronomicPartners) {
        this.gastronomicPartners = gastronomicPartners;
    }

    public String getTastingPurchase() {
        return tastingPurchase;
    }

    public void setTastingPurchase(String tastingPurchase) {
        this.tastingPurchase = tastingPurchase;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
