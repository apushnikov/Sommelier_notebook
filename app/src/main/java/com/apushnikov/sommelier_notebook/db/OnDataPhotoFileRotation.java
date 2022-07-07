package com.apushnikov.sommelier_notebook.db;

//=======================================================================
// Интерфейс - действия, когда повернули фотографию
//=======================================================================
public interface OnDataPhotoFileRotation {
    void onPhotoFileRotation(String newPhotoPath, int position);
}
