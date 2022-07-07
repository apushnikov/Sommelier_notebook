/**
 The MIT License (MIT)

 Copyright (c) 2016 Chau Thai

 Разрешение предоставляется бесплатно любому лицу, получившему копию.
 этого программного обеспечения и связанных файлов документации («Программное обеспечение»)
 для работы с
 в Программном обеспечении без ограничений, включая, помимо прочего, права
 использовать, копировать, изменять, объединять, публиковать, распространять,
 сублицензировать и / или продавать
 копий Программного обеспечения и разрешить лицам, которым Программное обеспечение
 предоставлены для этого при соблюдении следующих условий:

 Вышеупомянутое уведомление об авторских правах и это уведомление о разрешении должны быть
 включены во все
 копии или существенные части Программного обеспечения.

 ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ ПРЕДОСТАВЛЯЕТСЯ «КАК ЕСТЬ», БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, ЯВНЫХ ИЛИ
 ПОДРАЗУМЕВАЕМЫЕ, ВКЛЮЧАЯ, НО НЕ ОГРАНИЧИВАЯСЬ, ГАРАНТИИ КОММЕРЧЕСКОЙ ЦЕННОСТИ,
 ПРИГОДНОСТЬ ДЛЯ КОНКРЕТНОЙ ЦЕЛИ И ЗАЩИТА ОТ ПРАВ. НИ В КОЕМ СЛУЧАЕ
 АВТОРЫ ИЛИ ДЕРЖАТЕЛИ АВТОРСКИХ ПРАВ НЕСУТ ОТВЕТСТВЕННОСТЬ ЗА ЛЮБЫЕ ПРЕТЕНЗИИ, УБЫТКИ ИЛИ ДРУГИЕ
 ОТВЕТСТВЕННОСТЬ, ВЫЯВЛЯЮЩАЯСЯ ЛИ В РЕЗУЛЬТАТЕ ДОГОВОРА, ПРАКТИКИ ИЛИ ИНЫМ ОБРАЗОМ,
 ВНЕЗАПНО ИЛИ В СВЯЗИ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ ИЛИ ИСПОЛЬЗОВАНИЕМ ИЛИ ДРУГИМИ ДЕЛАМИ
 ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ.
 */

package com.apushnikov.sommelier_notebook.swipe;

import android.os.Bundle;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ViewBinderHelper provides a quick and easy solution to restore the open/close state
 * of the items in RecyclerView, ListView, GridView or any view that requires its child view
 * to bind the view to a data object.
 * * ViewBinderHelper предоставляет быстрое и простое решение для восстановления
 * открытого / закрытого состояния элементов в RecyclerView, ListView, GridView или
 * любом представлении, которое требует, чтобы его дочернее представление связывало
 * представление с объектом данных.
 *
 * <p>When you bind you data object to a view, use {@link #bind(SwipeRevealLayout, String)} to
 * save and restore the open/close state of the view.</p>
 * <p> При привязке объекта данных к представлению используйте {@link #bind (SwipeRevealLayout, String)}
 * для сохранения и восстановления открытого / закрытого состояния представления. </p>
 *
 * <p>Optionally, if you also want to save and restore the open/close state when the device's
 * orientation is changed, call {@link #saveStates(Bundle)} in {@link android.app.Activity#onSaveInstanceState(Bundle)}
 * and {@link #restoreStates(Bundle)} in {@link android.app.Activity#onRestoreInstanceState(Bundle)}</p>
 * <p> При желании, если вы также хотите сохранить и восстановить состояние открытия / закрытия
 * при изменении ориентации устройства, вызовите {@link #saveStates (Bundle)}
 * в {@link android.app.Activity # onSaveInstanceState (Bundle)} и {@link #restoreStates (Bundle)}
 * в {@link android.app.Activity # onRestoreInstanceState (Bundle)} </p>
 */
public class ViewBinderHelper {
    private static final String BUNDLE_MAP_KEY = "ViewBinderHelper_Bundle_Map_Key";

    private Map<String, Integer> mapStates = Collections.synchronizedMap(new HashMap<String, Integer>());
    private Map<String, SwipeRevealLayout> mapLayouts = Collections.synchronizedMap(new HashMap<String, SwipeRevealLayout>());
    private Set<String> lockedSwipeSet = Collections.synchronizedSet(new HashSet<String>());

    // Если установлено значение true, то одновременно может быть открыта только одна строка.
    private volatile boolean openOnlyOne = false;
    private final Object stateChangeLock = new Object();

    /**
     * Help to save and restore open/close state of the swipeLayout. Call this method
     * when you bind your view holder with the data object.
     * Помогите сохранить и восстановить открытое / закрытое состояние swipeLayout.
     * Вызовите этот метод при привязке держателя представления к объекту данных.
     *
     * @param swipeLayout swipeLayout of the current view.
     * @param id a string that uniquely defines the data object of the current view.
     * @param swipeLayout swipeLayout текущего представления. @param id - строка,
     * которая однозначно определяет объект данных текущего представления.
     */
    public void bind(final SwipeRevealLayout swipeLayout, final String id) {
        if (swipeLayout.shouldRequestLayout()) {
            swipeLayout.requestLayout();
        }

        mapLayouts.values().remove(swipeLayout);
        mapLayouts.put(id, swipeLayout);

        swipeLayout.abort();
        swipeLayout.setDragStateChangeListener(new SwipeRevealLayout.DragStateChangeListener() {
            @Override
            public void onDragStateChanged(int state) {
                mapStates.put(id, state);

                if (openOnlyOne) {
                    // Закрываем остальные открытые строки
                    closeOthers(id, swipeLayout);
                }
            }
        });

        // первая привязка.
        if (!mapStates.containsKey(id)) {
            mapStates.put(id, SwipeRevealLayout.STATE_CLOSE);
            swipeLayout.close(false);
        }

        // не в первый раз, то закрытие или открытие зависит от текущего состояния.
        else {
            int state = mapStates.get(id);

            if (state == SwipeRevealLayout.STATE_CLOSE || state == SwipeRevealLayout.STATE_CLOSING ||
                    state == SwipeRevealLayout.STATE_DRAGGING) {
                swipeLayout.close(false);
            } else {
                swipeLayout.open(false);
            }
        }

        // установить блокировку смахивания
        swipeLayout.setLockDrag(lockedSwipeSet.contains(id));
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     * Только если вам нужно восстановить открытое / закрытое состояние при изменении ориентации.
     * Вызовите этот метод в {@link android.app.Activity # onSaveInstanceState (Bundle)}.
     */
    public void saveStates(Bundle outState) {
        if (outState == null)
            return;

        Bundle statesBundle = new Bundle();
        for (Map.Entry<String, Integer> entry : mapStates.entrySet()) {
            statesBundle.putInt(entry.getKey(), entry.getValue());
        }

        outState.putBundle(BUNDLE_MAP_KEY, statesBundle);
    }


    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onRestoreInstanceState(Bundle)}
     * Только если вам нужно восстановить открытое / закрытое состояние при изменении ориентации.
     * Вызовите этот метод в {@link android.app.Activity # onRestoreInstanceState (Bundle)}.
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void restoreStates(Bundle inState) {
        if (inState == null)
            return;

        if (inState.containsKey(BUNDLE_MAP_KEY)) {
            HashMap<String, Integer> restoredMap = new HashMap<>();

            Bundle statesBundle = inState.getBundle(BUNDLE_MAP_KEY);
            Set<String> keySet = statesBundle.keySet();

            if (keySet != null) {
                for (String key : keySet) {
                    restoredMap.put(key, statesBundle.getInt(key));
                }
            }

            mapStates = restoredMap;
        }
    }

    /**
     * Lock swipe for some layouts.
     * @param id a string that uniquely defines the data object.
     * Заблокируйте смахивание для некоторых макетов. @param id - строка, которая однозначно
     * определяет объект данных.
     */
    public void lockSwipe(String... id) {
        setLockSwipe(true, id);
    }

    /**
     * Unlock swipe for some layouts.
     * @param id a string that uniquely defines the data object.
     * Заблокируйте смахивание для некоторых макетов. @param id - строка, которая однозначно
     * определяет объект данных.
     */
    public void unlockSwipe(String... id) {
        setLockSwipe(false, id);
    }

    /**
     * @param openOnlyOne Если установлено значение true, то одновременно может быть
     *                    открыта только одна строка.
     */
    public void setOpenOnlyOne(boolean openOnlyOne) {
        this.openOnlyOne = openOnlyOne;
    }

    /**
     * Откройте конкретный макет.
     * @param id уникальный идентификатор, который идентифицирует объект данных, привязанный к макету.
     */
    public void openLayout(final String id) {
        synchronized (stateChangeLock) {
            mapStates.put(id, SwipeRevealLayout.STATE_OPEN);

            if (mapLayouts.containsKey(id)) {
                final SwipeRevealLayout layout = mapLayouts.get(id);
                layout.open(true);
            }
            else if (openOnlyOne) {
                // Закрываем остальные открытые строки
                closeOthers(id, mapLayouts.get(id));
            }
        }
    }

    /**
     * Закройте конкретный макет.
     * @param id уникальный идентификатор, который идентифицирует объект данных, привязанный к макету.
     */
    public void closeLayout(final String id) {
        synchronized (stateChangeLock) {
            mapStates.put(id, SwipeRevealLayout.STATE_CLOSE);

            if (mapLayouts.containsKey(id)) {
                final SwipeRevealLayout layout = mapLayouts.get(id);
                layout.close(true);
            }
        }
    }

    /**
     * Закройте другие смахивающие макеты.
     * @param id макет, связанный с этим идентификатором объекта данных, будет исключен.
     * @param swipeLayout будут исключены.
     */
    private void closeOthers(String id, SwipeRevealLayout swipeLayout) {
        synchronized (stateChangeLock) {
            // закройте другие строки, если openOnlyOne истинно.
            if (getOpenCount() > 1) {
                for (Map.Entry<String, Integer> entry : mapStates.entrySet()) {
                    if (!entry.getKey().equals(id)) {
                        entry.setValue(SwipeRevealLayout.STATE_CLOSE);
                    }
                }

                for (SwipeRevealLayout layout : mapLayouts.values()) {
                    if (layout != swipeLayout) {
                        layout.close(true);
                    }
                }
            }
        }
    }

    private void setLockSwipe(boolean lock, String... id) {
        if (id == null || id.length == 0)
            return;

        if (lock)
            lockedSwipeSet.addAll(Arrays.asList(id));
        else
            lockedSwipeSet.removeAll(Arrays.asList(id));

        for (String s : id) {
            SwipeRevealLayout layout = mapLayouts.get(s);
            if (layout != null) {
                layout.setLockDrag(lock);
            }
        }
    }

    // Определяет количество открытых макетов
    private int getOpenCount() {
        int total = 0;

        for (int state : mapStates.values()) {
            if (state == SwipeRevealLayout.STATE_OPEN || state == SwipeRevealLayout.STATE_OPENING) {
                total++;
            }
        }

        return total;
    }
}
