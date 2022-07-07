package com.apushnikov.sommelier_notebook.db;

import java.util.List;

//=======================================================================
// Интерфейс - действия, когда мы считали массив вин
//=======================================================================
public interface OnDataReceivedListWine {
    void onReceivedListWine (List<Wine> list);
}
