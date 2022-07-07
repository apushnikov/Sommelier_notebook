package com.apushnikov.sommelier_notebook.photoview;

import android.view.View;

/*
* Обратный вызов для получения, когда пользователь нажимает на ImageView.
* Вы получите обратный вызов, если пользователь коснется любого места в представлении,
* нажатие на «пробел» не будет проигнорировано.
*/
public interface OnViewTapListener {

    /**
     * A callback to receive where the user taps on a ImageView. You will receive a callback if
     * the user taps anywhere on the view, tapping on 'whitespace' will not be ignored.
     * Обратный вызов для получения, когда пользователь нажимает на ImageView.
     * Вы получите обратный вызов, если пользователь коснется любого места в представлении,
     * нажатие на «пробел» не будет проигнорировано.
     *
     * @param view - View the user tapped.
     * @param x    - where the user tapped from the left of the View.
     * @param y    - where the user tapped from the top of the View.
     *
     * @param view - просмотр затронутого пользователя.
     * @param x - где пользователь нажал слева от представления.
     * @param y - где пользователь нажал на верхнюю часть представления.
     */
    void onViewTap(View view, float x, float y);
}
