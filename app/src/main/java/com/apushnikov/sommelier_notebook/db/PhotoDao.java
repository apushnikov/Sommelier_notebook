package com.apushnikov.sommelier_notebook.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

//================================================================
// В объекте PhotoDao мы будем описывать методы для работы с фотографиями.
//================================================================
@Dao
public interface PhotoDao {

    // Метод getAll позволяют получить полный фотографий
    @Query("SELECT * FROM photo")
    List<Photo> getAll();

    // Метод getById позволяет получить фотографию по id.
    @Query("SELECT * FROM photo WHERE id = :id")
    Photo getById(long id);

    // Метод getByWineId позволяет получить список фотографий по номеру винна wineId.
    @Query("SELECT * FROM photo WHERE wineId = :wineId")
    List<Photo> getByWineId(long wineId);

    // Метод getByShelfId позволяет получить список фотографий по номеру винной полки shelfId.
    @Query("SELECT photo.* FROM photo,wine WHERE photo.wineId = wine.id AND wine.shelfId = :shelfId")
    List<Photo> getByShelfId(long shelfId);



    // Метод getMainPhotoByWineId позволяет фото, который является главным фото по номеру винна wineId.
    @Query("SELECT * FROM photo WHERE wineId = :wineId AND mainPhoto = 1")
    Photo getMainPhotoByWineId(long wineId);

    // Метод insert для вставки
    @Insert
    void insert(Photo photo);

    // Метод insert для вставки списка фото
    @Insert
    void insert(List<Photo> photos);

    // Метод update для обнавления
    // Поиск по первичному ключу
    // Обновляются ВСЕ поля
    @Update
    void update(Photo photo);

    // Метод delete для удаления
    // Поиск по первичному ключу
    @Delete
    void delete(Photo photo);

    // Метод delete для удаления списка фото
    // Поиск по первичному ключу
    @Delete
    void delete(List<Photo> photos);

}
