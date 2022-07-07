package com.apushnikov.sommelier_notebook.photoview;

/**
 * Interface definition for a callback to be invoked when the photo is experiencing a drag event
 * Определение интерфейса для обратного вызова, который будет вызываться,
 * когда фотография испытывает событие перетаскивания
 */
public interface OnViewDragListener {

    /**
     * Callback for when the photo is experiencing a drag event. This cannot be invoked when the
     * user is scaling.
     * Обратный вызов, когда фотография испытывает событие перетаскивания.
     * Это не может быть вызвано, когда пользователь масштабирует.
     *
     * @param dx The change of the coordinates in the x-direction
     * @param dy The change of the coordinates in the y-direction
     *
     * @param dx Изменение координат по оси x
     * @param dy Изменение координат в направлении y
     */
    void onDrag(float dx, float dy);
}
