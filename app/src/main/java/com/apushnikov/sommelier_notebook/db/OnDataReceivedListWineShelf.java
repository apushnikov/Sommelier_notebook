package com.apushnikov.sommelier_notebook.db;

import java.util.List;

//=======================================================================
// Интерфейс - действия, когда мы считали массив винных полок
//=======================================================================
public interface OnDataReceivedListWineShelf {
    void onReceivedListWineShelf (List<WineShelf> list);
}
