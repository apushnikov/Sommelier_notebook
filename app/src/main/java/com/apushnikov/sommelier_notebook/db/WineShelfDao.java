package com.apushnikov.sommelier_notebook.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

//================================================================
// В объекте WineShelfDao мы будем описывать методы для работы с базой винных полок.
//================================================================
@Dao
public interface WineShelfDao {

    // Метод getAll позволяют получить полный список винных полок
    @Query("SELECT * FROM wineShelf")
    List<WineShelf> getAll();

    // Метод getById позволяет получить винную полку по id.
    @Query("SELECT * FROM wineShelf WHERE id = :id")
    WineShelf getById(long id);

    // Метод getMinShelfId - Берем минимальное значение номера винной полки
    @Query("SELECT MIN(id) FROM wineShelf")
    long getMinShelfId();

    // Метод insert для вставки
    @Insert
    long insert(WineShelf wineShelf);

    // Метод update для обнавления
    // Поиск по первичному ключу
    // Обновляются ВСЕ поля
    @Update
    void update(WineShelf wineShelf);

    // Метод delete для удаления
    // Поиск по первичному ключу
    @Delete
    void delete(WineShelf wineShelf);

}
