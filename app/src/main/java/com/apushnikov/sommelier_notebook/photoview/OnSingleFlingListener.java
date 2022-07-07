package com.apushnikov.sommelier_notebook.photoview;

import android.view.MotionEvent;

/**
 * A callback to be invoked when the ImageView is flung with a single
 * touch
 * Обратный вызов, который будет вызываться, когда ImageView бросается одним касанием
 */
public interface OnSingleFlingListener {

    /**
     * A callback to receive where the user flings on a ImageView. You will receive a callback if
     * the user flings anywhere on the view.
     * Обратный вызов для получения информации о том, где пользователь запускает ImageView.
     * Вы получите обратный вызов, если пользователь перейдет в любую точку представления.
     *
     * @param e1        MotionEvent the user first touch.
     * @param e2        MotionEvent the user last touch.
     * @param velocityX distance of user's horizontal fling.
     * @param velocityY distance of user's vertical fling.
     *
     * @param e1 MotionEvent пользователя первым прикосновением.
     * @param e2 MotionEvent последнее касание пользователя.
     * @param velocityX расстояние горизонтального броска пользователя.
     * @param velocityY расстояние вертикального броска пользователя.
     */
    boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
}
