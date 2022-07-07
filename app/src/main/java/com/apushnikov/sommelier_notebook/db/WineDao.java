package com.apushnikov.sommelier_notebook.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

//================================================================
// В объекте WineDao мы будем описывать методы для работы с базой вина.
//================================================================
@Dao
public interface WineDao {

    // Метод getAll позволяют получить полный список вин
    @Query("SELECT * FROM wine")
    List<Wine> getAll();

    // Метод getById позволяет получить вино по id.
    @Query("SELECT * FROM wine WHERE id = :id")
    Wine getById(long id);

    // Метод getByShelfId позволяет получить список вин по номеру винной полки shelfId.
    @Query("SELECT * FROM wine WHERE shelfId = :shelfId")
    List<Wine> getByShelfId(long shelfId);

    // Метод countByShelfId позволяет получить количество вин по номеру винной полки shelfId.
    @Query("SELECT COUNT(*) FROM wine WHERE shelfId = :shelfId")
    long countByShelfId(long shelfId);

    // Метод insert для вставки
    @Insert
    long insert(Wine wine);

    // Метод update для обнавления
    // Поиск по первичному ключу
    // Обновляются ВСЕ поля
    @Update
    void update(Wine wine);

    // Метод delete для удаления
    // Поиск по первичному ключу
    @Delete
    void delete(Wine wine);

    // Метод delete для удаления списка вин
    // Поиск по первичному ключу
    @Delete
    void delete(List<Wine> wines);

}
