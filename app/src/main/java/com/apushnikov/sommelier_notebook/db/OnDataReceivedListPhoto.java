package com.apushnikov.sommelier_notebook.db;

import java.util.List;

//=======================================================================
// Интерфейс - действия, когда мы считали список фотографий
//=======================================================================
public interface OnDataReceivedListPhoto {
    void onReceivedListPhoto (List<Photo> list);
}
