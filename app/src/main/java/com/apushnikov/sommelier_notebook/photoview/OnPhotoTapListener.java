package com.apushnikov.sommelier_notebook.photoview;

import android.widget.ImageView;

/**
 * A callback to be invoked when the Photo is tapped with a single
 * tap.
 * Обратный вызов, который будет вызываться при касании фотографии одним касанием
 */
public interface OnPhotoTapListener {

    /**
     * A callback to receive where the user taps on a photo. You will only receive a callback if
     * the user taps on the actual photo, tapping on 'whitespace' will be ignored.
     * Обратный вызов для получения, когда пользователь нажимает на фотографию.
     * Вы получите обратный вызов только в том случае, если пользователь коснется фактической
     * фотографии, нажатие на «пробел» будет проигнорировано.
     *
     * @param view ImageView the user tapped.
     * @param x    where the user tapped from the of the Drawable, as percentage of the
     *             Drawable width.
     * @param y    where the user tapped from the top of the Drawable, as percentage of the
     *             Drawable height.
     *
     * @param view ImageView, на который нажал пользователь.
     * @param x, где пользователь нажал на Drawable, в процентах от ширины Drawable.
     * @param y, где пользователь нажимал сверху Drawable в процентах от высоты Drawable.
     */
    void onPhotoTap(ImageView view, float x, float y);
}
