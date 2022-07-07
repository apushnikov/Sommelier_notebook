package com.apushnikov.sommelier_notebook.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

//================================================================
// Аннотацией Database помечаем основной класс по работе с базой данных.
// Этот класс должен быть абстрактным и наследовать RoomDatabase.
//
// В параметрах аннотации Database указываем, какие Entity будут использоваться,
// и версию базы. Для каждого Entity класса из списка entities будет создана таблица.
// В Database классе необходимо описать абстрактные методы для получения Dao объектов,
// которые вам понадобятся.
//================================================================
@Database(entities = {Wine.class, WineShelf.class, Photo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WineDao wineDao();
    public abstract WineShelfDao wineShelfDao();
    public abstract PhotoDao photoDao();
}

