package com.apushnikov.sommelier_notebook.db;

//=======================================================================
// Интерфейс - действия, когда вставили винную полку
//=======================================================================
public interface OnDataInsertedWineShelf {
    void onInsertedWineShelf (long ShelfId, WineShelf wineShelf);
}
