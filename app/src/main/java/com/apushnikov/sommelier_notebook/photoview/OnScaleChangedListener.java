package com.apushnikov.sommelier_notebook.photoview;


/**
 * Interface definition for callback to be invoked when attached ImageView scale changes
 * Определение интерфейса для обратного вызова, который будет вызываться
 * при изменении масштаба прикрепленного ImageView
 */
public interface OnScaleChangedListener {

    /**
     * Callback for when the scale changes
     * Обратный звонок при изменении масштаба
     *
     * @param scaleFactor the scale factor (less than 1 for zoom out, greater than 1 for zoom in)
     * @param focusX      focal point X position
     * @param focusY      focal point Y position
     *
     * @param scale Фактор масштабного коэффициента (меньше 1 для уменьшения, больше 1 для увеличения)
     * @param focusX координата X позиции
     * @param focusY координата Y позиции
     */
    void onScaleChange(float scaleFactor, float focusX, float focusY);
}
