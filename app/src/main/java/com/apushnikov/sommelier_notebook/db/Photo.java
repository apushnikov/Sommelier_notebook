package com.apushnikov.sommelier_notebook.db;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

//================================================================
// Класс помечается аннотацией Entity.
// Photo (фото)
//================================================================
@Entity(foreignKeys = @ForeignKey(
        entity = Wine.class,
        parentColumns = "id",
        childColumns = "wineId",
        onDelete = CASCADE))
public class Photo {

    // =====================================================================
    // ПОЛЯ
    // =====================================================================
    // Номер фото
    // С АВТОГЕНЕРАЦИЕЙ!!!
    @PrimaryKey(autoGenerate = true)
    public long id;

    // Номер вина
    @ColumnInfo(index = true)
    public long wineId;

    // Ссылка на фото
    public String photoAbsolutePath;

    // Является ли фото главным (1-является, 0-нет)
    public int mainPhoto;

    // =====================================================================
    // МЕТОДЫ Геттер и Сеттер
    // =====================================================================
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWineId() {
        return wineId;
    }

    public void setWineId(long wineId) {
        this.wineId = wineId;
    }

    public String getPhoto() {
        return photoAbsolutePath;
    }

    public void setPhoto(String photoAbsolutePath) {
        this.photoAbsolutePath = photoAbsolutePath;
    }

    public String getPhotoAbsolutePath() {
        return photoAbsolutePath;
    }

    public void setPhotoAbsolutePath(String photoAbsolutePath) {
        this.photoAbsolutePath = photoAbsolutePath;
    }

    public int getMainPhoto() {
        return mainPhoto;
    }

    public void setMainPhoto(int mainPhoto) {
        this.mainPhoto = mainPhoto;
    }

}
