package com.apushnikov.sommelier_notebook.photoview;

import android.widget.ImageView;

/**
 * Callback when the user tapped outside of the photo
 * Обратный вызов, когда пользователь нажал за пределами фотографии
 */
public interface OnOutsidePhotoTapListener {

    /**
     * The outside of the photo has been tapped
     * Было нажатие вне фотографии
     */
    void onOutsidePhotoTap(ImageView imageView);
}
