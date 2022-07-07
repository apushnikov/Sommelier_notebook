package com.apushnikov.sommelier_notebook.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

//================================================================
// Класс помечается аннотацией Entity.
// Wine_shelf (винная полка)
//================================================================
@Entity
public class WineShelf implements Serializable {


    // =====================================================================
    // ПОЛЯ
    // =====================================================================

    // Номер винной полки
    // С АВТОГЕНЕРАЦИЕЙ!!!
    @PrimaryKey(autoGenerate = true)
    public long id;

    // Наименование винной полки
    public String nameShelf;

    // Доп.характеристики (например, тема дегустации)
    public String topicShelf;

    // Ссылка на фото
    public String photoAbsolutePathShelf;

    // =====================================================================
    // МЕТОДЫ
    // =====================================================================

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNameShelf() {
        return nameShelf;
    }

    public void setNameShelf(String nameShelf) {
        this.nameShelf = nameShelf;
    }

    public String getTopicShelf() {
        return topicShelf;
    }

    public void setTopicShelf(String topicShelf) {
        this.topicShelf = topicShelf;
    }

    public String getPhotoAbsolutePathShelf() {
        return photoAbsolutePathShelf;
    }

    public void setPhotoAbsolutePathShelf(String photoAbsolutePathShelf) {
        this.photoAbsolutePathShelf = photoAbsolutePathShelf;
    }
}
