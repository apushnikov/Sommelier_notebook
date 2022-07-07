package com.apushnikov.sommelier_notebook.photoview;

import android.graphics.RectF;

/**
 * Interface definition for a callback to be invoked when the internal Matrix has changed for
 * this View.
 * Определение интерфейса для обратного вызова, который будет вызываться,
 * когда внутренняя матрица изменилась для этого представления.
 */
public interface OnMatrixChangedListener {

    /**
     * Callback for when the Matrix displaying the Drawable has changed. This could be because
     * the View's bounds have changed, or the user has zoomed.
     * Обратный вызов, когда матрица, отображающая Drawable, изменилась. Это могло произойти
     * из-за того, что границы просмотра изменились, или пользователь увеличил масштаб.
     *
     * @param rect - Rectangle displaying the Drawable's new bounds.
     * @param rect - прямоугольник, отображающий новые границы Drawable.
     */
    void onMatrixChanged(RectF rect);
}
