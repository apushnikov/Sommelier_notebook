/*
 Copyright 2011, 2012 Chris Banes.
 <p>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p>
 http://www.apache.org/licenses/LICENSE-2.0
 <p>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.apushnikov.sommelier_notebook.photoview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.OverScroller;

/**
 * The component of {@link PhotoView} which does the work allowing for zooming, scaling, panning, etc.
 * It is made public in case you need to subclass something other than AppCompatImageView and still
 * gain the functionality that {@link PhotoView} offers
 *
 * Компонент {@link PhotoView}, который выполняет работу, позволяющую масштабировать,
 * масштабировать, панорамировать и т. Д.
 * Он становится общедоступным, если вам нужно создать подкласс чего-либо,
 * кроме AppCompatImageView, и при этом получить функциональность, которую предлагает {@link PhotoView}.
 */
public class PhotoViewAttacher implements View.OnTouchListener,
    View.OnLayoutChangeListener {

    private static float DEFAULT_MAX_SCALE = 3.0f;      // Максимальный масштаб по умолчанию
    private static float DEFAULT_MID_SCALE = 1.75f;     // Средний масштаб по умолчанию
    private static float DEFAULT_MIN_SCALE = 1.0f;      // Минимальный масштаб по умолчанию
    private static int DEFAULT_ZOOM_DURATION = 200;

    private static final int HORIZONTAL_EDGE_NONE = -1;
    private static final int HORIZONTAL_EDGE_LEFT = 0;
    private static final int HORIZONTAL_EDGE_RIGHT = 1;
    private static final int HORIZONTAL_EDGE_BOTH = 2;
    private static final int VERTICAL_EDGE_NONE = -1;
    private static final int VERTICAL_EDGE_TOP = 0;
    private static final int VERTICAL_EDGE_BOTTOM = 1;
    private static final int VERTICAL_EDGE_BOTH = 2;
    private static int SINGLE_TOUCH = 1;

    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private int mZoomDuration = DEFAULT_ZOOM_DURATION;
    private float mMinScale = DEFAULT_MIN_SCALE;        // Минимальный масштаб
    private float mMidScale = DEFAULT_MID_SCALE;        // Средний масштаб
    private float mMaxScale = DEFAULT_MAX_SCALE;        // Максимальный масштаб

    private boolean mAllowParentInterceptOnEdge = true;
    private boolean mBlockParentIntercept = false;

    private ImageView mImageView;

    // Gesture Detectors
    // Детекторы жестов
    private GestureDetector mGestureDetector;
    private CustomGestureDetector mScaleDragDetector;

    // These are set so we don't keep allocating them on the heap
    // Они настроены, поэтому мы не размещаем их в куче.
    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();
    private final Matrix mSuppMatrix = new Matrix();
    private final RectF mDisplayRect = new RectF();
    private final float[] mMatrixValues = new float[9];

    // Listeners (Слушатели)
    private OnMatrixChangedListener mMatrixChangeListener;  // когда внутренняя матрица изменилась
    private OnPhotoTapListener mPhotoTapListener;   // при касании фотографии одним касанием
    private OnOutsidePhotoTapListener mOutsidePhotoTapListener; // пользователь нажал за пределами фотографии
    private OnViewTapListener mViewTapListener; // когда пользователь нажимает на ImageView
    private View.OnClickListener mOnClickListener;  // Обычный View.OnClickListener
    private OnLongClickListener mLongClickListener; // Обычный OnLongClickListener
    private OnScaleChangedListener mScaleChangeListener;    // при изменении масштаба прикрепленного ImageView
    private OnSingleFlingListener mSingleFlingListener; // когда ImageView бросается одним касанием
    private OnViewDragListener mOnViewDragListener; // когда фотография испытывает событие перетаскивания

    private FlingRunnable mCurrentFlingRunnable;
    private int mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
    private int mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
    private float mBaseRotation;

    // будет ли масштабироваться изображение или нет
    private boolean mZoomEnabled = true;    // По умолчанию - да, будет
    private ScaleType mScaleType = ScaleType.FIT_CENTER;

    private OnGestureListener onGestureListener = new OnGestureListener() {
        @Override
        public void onDrag(float dx, float dy) {
            if (mScaleDragDetector.isScaling()) {
                return; // Do not drag if we are already scaling
                // Не перетаскивать, если мы уже масштабируем
            }
            if (mOnViewDragListener != null) {
                mOnViewDragListener.onDrag(dx, dy);
            }
            mSuppMatrix.postTranslate(dx, dy);
            // Вспомогательный метод, который просто проверяет матрицу, а затем отображает результат
            checkAndDisplayMatrix();

            /*
             * Here we decide whether to let the ImageView's parent to start taking
             * over the touch event.
             * Здесь мы решаем, разрешить ли родительскому элементу ImageView
             * начать прием события касания.
             *
             * First we check whether this function is enabled. We never want the
             * parent to take over if we're scaling. We then check the edge we're
             * on, and the direction of the scroll (i.e. if we're pulling against
             * the edge, aka 'overscrolling', let the parent take over).
             * Сначала проверяем, включена ли эта функция. Мы никогда не хотим, чтобы родитель
             * взял на себя ответственность, если мы масштабируемся. Затем мы проверяем край,
             * на котором мы находимся, и направление прокрутки (то есть, если мы тянем за край,
             * иначе говоря, «перескроллинг», пусть родитель берет верх).
             */
            ViewParent parent = mImageView.getParent();
            if (mAllowParentInterceptOnEdge && !mScaleDragDetector.isScaling() && !mBlockParentIntercept) {
                if (mHorizontalScrollEdge == HORIZONTAL_EDGE_BOTH
                        || (mHorizontalScrollEdge == HORIZONTAL_EDGE_LEFT && dx >= 1f)
                        || (mHorizontalScrollEdge == HORIZONTAL_EDGE_RIGHT && dx <= -1f)
                        || (mVerticalScrollEdge == VERTICAL_EDGE_TOP && dy >= 1f)
                        || (mVerticalScrollEdge == VERTICAL_EDGE_BOTTOM && dy <= -1f)) {
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(false);
                    }
                }
            } else {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
        }

        @Override
        public void onFling(float startX, float startY, float velocityX, float velocityY) {
            mCurrentFlingRunnable = new FlingRunnable(mImageView.getContext());
            mCurrentFlingRunnable.fling(getImageViewWidth(mImageView),
                getImageViewHeight(mImageView), (int) velocityX, (int) velocityY);
            mImageView.post(mCurrentFlingRunnable);
        }

        @Override
        public void onScale(float scaleFactor, float focusX, float focusY) {
            onScale(scaleFactor, focusX, focusY, 0, 0);
        }

        @Override
        public void onScale(float scaleFactor, float focusX, float focusY, float dx, float dy) {
            if (getScale() < mMaxScale || scaleFactor < 1f) {
                if (mScaleChangeListener != null) {
                    mScaleChangeListener.onScaleChange(scaleFactor, focusX, focusY);
                }
                mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                mSuppMatrix.postTranslate(dx, dy);
                // Вспомогательный метод, который просто проверяет матрицу, а затем отображает результат
                checkAndDisplayMatrix();
            }
        }
    };

    // Конструктор
    public PhotoViewAttacher(ImageView imageView) {
        mImageView = imageView;
        imageView.setOnTouchListener(this);
        imageView.addOnLayoutChangeListener(this);
        if (imageView.isInEditMode()) {
            return;
        }
        mBaseRotation = 0.0f;
        // Create Gesture Detectors...
        // Создание детекторов жестов ...
        mScaleDragDetector = new CustomGestureDetector(imageView.getContext(), onGestureListener);
        mGestureDetector = new GestureDetector(imageView.getContext(), new GestureDetector.SimpleOnGestureListener() {

            // forward long click listener
            // пересылка долгого прослушивания кликов
            @Override
            public void onLongPress(MotionEvent e) {
                if (mLongClickListener != null) {
                    mLongClickListener.onLongClick(mImageView);
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                float velocityX, float velocityY) {
                if (mSingleFlingListener != null) {
                    if (getScale() > DEFAULT_MIN_SCALE) {
                        return false;
                    }
                    if (e1.getPointerCount() > SINGLE_TOUCH
                        || e2.getPointerCount() > SINGLE_TOUCH) {
                        return false;
                    }
                    return mSingleFlingListener.onFling(e1, e2, velocityX, velocityY);
                }
                return false;
            }
        });
        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(mImageView);
                }
                final RectF displayRect = getDisplayRect();
                final float x = e.getX(), y = e.getY();
                if (mViewTapListener != null) {
                    mViewTapListener.onViewTap(mImageView, x, y);
                }
                if (displayRect != null) {
                    // Check to see if the user tapped on the photo
                    // Убедитесь, что пользователь нажал на фото
                    if (displayRect.contains(x, y)) {
                        float xResult = (x - displayRect.left)
                            / displayRect.width();
                        float yResult = (y - displayRect.top)
                            / displayRect.height();
                        if (mPhotoTapListener != null) {
                            mPhotoTapListener.onPhotoTap(mImageView, xResult, yResult);
                        }
                        return true;
                    } else {
                        if (mOutsidePhotoTapListener != null) {
                            mOutsidePhotoTapListener.onOutsidePhotoTap(mImageView);
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent ev) {
                try {
                    float scale = getScale();
                    float x = ev.getX();
                    float y = ev.getY();
                    if (scale < getMediumScale()) {
                        setScale(getMediumScale(), x, y, true);
                    } else if (scale >= getMediumScale() && scale < getMaximumScale()) {
                        setScale(getMaximumScale(), x, y, true);
                    } else {
                        setScale(getMinimumScale(), x, y, true);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Can sometimes happen when getX() and getY() is called
                    // Иногда может произойти при вызове getX () и getY ()
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                // Wait for the confirmed onDoubleTap() instead
                // Вместо этого дождитесь подтверждения onDoubleTap ()
                return false;
            }
        });
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener) {
        this.mGestureDetector.setOnDoubleTapListener(newOnDoubleTapListener);
    }

    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangeListener) {
        this.mScaleChangeListener = onScaleChangeListener;
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        this.mSingleFlingListener = onSingleFlingListener;
    }

    @Deprecated
    public boolean isZoomEnabled() {
        return mZoomEnabled;
    }

    public RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    // Устанавливаем матрицу отображения
    //      Параметры - Matrix finalMatrix
    public boolean setDisplayMatrix(Matrix finalMatrix) {
        // Если finalMatrix нулевая - генерируем ошибку
        if (finalMatrix == null) {
            throw new IllegalArgumentException("Матрица не может быть нулевой");
        }
        // getDrawable() - Получает текущий объект Drawable или значение null,
        // если объект Drawable не назначен.
        //
        //Возврат:
        //возможность рисования представления или null, если рисование не было назначено.
        if (mImageView.getDrawable() == null) {
            return false;
        }
        // метод set - (глубоко) скопируйте матрицу src в эту матрицу.
        // Если src имеет значение null, сбросить эту матрицу до единичной матрицы.
        mSuppMatrix.set(finalMatrix);
        // Вспомогательный метод, который просто проверяет матрицу, а затем отображает результат
        checkAndDisplayMatrix();
        return true;
    }

    public void setBaseRotation(final float degrees) {
        mBaseRotation = degrees % 360;
        // Обновляет базовую матрицу (Если масштабируется) или сбрасывает матрицу (Если не масштабируется)
        update();
        setRotationBy(mBaseRotation);
        // Вспомогательный метод, который просто проверяет матрицу, а затем отображает результат
        checkAndDisplayMatrix();
    }

    public void setRotationTo(float degrees) {
        mSuppMatrix.setRotate(degrees % 360);
        // Вспомогательный метод, который просто проверяет матрицу, а затем отображает результат
        checkAndDisplayMatrix();
    }

    public void setRotationBy(float degrees) {
        mSuppMatrix.postRotate(degrees % 360);
        // Вспомогательный метод, который просто проверяет матрицу, а затем отображает результат
        checkAndDisplayMatrix();
    }

    // Возвращает минимальный масштаб
    public float getMinimumScale() {
        return mMinScale;
    }

    // Возвращает средний масштаб
    public float getMediumScale() {
        return mMidScale;
    }

    // Возвращает максимальный масштаб
    public float getMaximumScale() {
        return mMaxScale;
    }

    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow
            (getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int
        oldRight, int oldBottom) {
        // Update our base matrix, as the bounds have
        // Обновите нашу базовую матрицу, так как границы изменились
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            // Обновите базовую матрицу, используя текущий доступный для рисования
            // Рассчитать матрицу для FIT_CENTER
            updateBaseMatrix(mImageView.getDrawable());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        boolean handled = false;
        if (mZoomEnabled && Util.hasDrawable((ImageView) v)) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ViewParent parent = v.getParent();
                    // First, disable the Parent from intercepting the touch
                    // event
                    // Во-первых, отключите родительский элемент от перехвата события касания
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    // If we're flinging, and the user presses down, cancel
                    // fling
                    // Если мы бросаем, и пользователь нажимает, отмените бросок
                    cancelFling();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    // If the user has zoomed less than min scale, zoom back
                    // to min scale
                    // Если пользователь увеличил масштаб меньше минимальной шкалы,
                    // вернитесь к минимальной шкале
                    if (getScale() < mMinScale) {
                        RectF rect = getDisplayRect();
                        if (rect != null) {
                            v.post(new AnimatedZoomRunnable(getScale(), mMinScale,
                                rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    } else if (getScale() > mMaxScale) {
                        RectF rect = getDisplayRect();
                        if (rect != null) {
                            v.post(new AnimatedZoomRunnable(getScale(), mMaxScale,
                                rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    }
                    break;
            }
            // Try the Scale/Drag detector
            // Попробуйте детектор масштабирования / перетаскивания
            if (mScaleDragDetector != null) {
                boolean wasScaling = mScaleDragDetector.isScaling();
                boolean wasDragging = mScaleDragDetector.isDragging();
                handled = mScaleDragDetector.onTouchEvent(ev);
                boolean didntScale = !wasScaling && !mScaleDragDetector.isScaling();
                boolean didntDrag = !wasDragging && !mScaleDragDetector.isDragging();
                mBlockParentIntercept = didntScale && didntDrag;
            }
            // Check to see if the user double tapped
            // Проверьте, не нажал ли пользователь дважды
            if (mGestureDetector != null && mGestureDetector.onTouchEvent(ev)) {
                handled = true;
            }

        }
        return handled;
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAllowParentInterceptOnEdge = allow;
    }

    // Устанавливает минимальный масштаб
    public void setMinimumScale(float minimumScale) {
        Util.checkZoomLevels(minimumScale, mMidScale, mMaxScale);
        mMinScale = minimumScale;
    }

    // Устанавливает средний масштаб
    public void setMediumScale(float mediumScale) {
        Util.checkZoomLevels(mMinScale, mediumScale, mMaxScale);
        mMidScale = mediumScale;
    }

    // Устанавливает максимальный масштаб
    public void setMaximumScale(float maximumScale) {
        Util.checkZoomLevels(mMinScale, mMidScale, maximumScale);
        mMaxScale = maximumScale;
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        Util.checkZoomLevels(minimumScale, mediumScale, maximumScale);
        mMinScale = minimumScale;
        mMidScale = mediumScale;
        mMaxScale = maximumScale;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        mMatrixChangeListener = listener;
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mPhotoTapListener = listener;
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener mOutsidePhotoTapListener) {
        this.mOutsidePhotoTapListener = mOutsidePhotoTapListener;
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        mViewTapListener = listener;
    }

    public void setOnViewDragListener(OnViewDragListener listener) {
        mOnViewDragListener = listener;
    }

    public void setScale(float scale) {
        setScale(scale, false);
    }

    // Устанавливает масшаб
    public void setScale(float scale, boolean animate) {
        setScale(scale,
            (mImageView.getRight()) / 2,
            (mImageView.getBottom()) / 2,
            animate);
    }

    // Устанавливает масшаб
    public void setScale(float scale, float focalX, float focalY,
        boolean animate) {
        // Check to see if the scale is within bounds
        // Проверьте, находится ли масштаб в допустимых пределах
        if (scale < mMinScale || scale > mMaxScale) {
            throw new IllegalArgumentException("Scale must be within the range of minScale and maxScale");
            // Масштаб должен находиться в диапазоне minScale и maxScale.
        }
        if (animate) {
            mImageView.post(new AnimatedZoomRunnable(getScale(), scale,
                focalX, focalY));
        } else {
            mSuppMatrix.setScale(scale, scale, focalX, focalY);
            // Вспомогательный метод, который просто проверяет матрицу, а затем отображает результат
            checkAndDisplayMatrix();
        }
    }

    /**
     * Set the zoom interpolator
     * Установите интерполятор масштабирования
     *
     * @param interpolator the zoom interpolator
     * @param interpolator интерполятор масштабирования
     */
    public void setZoomInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public void setScaleType(ScaleType scaleType) {
        if (Util.isSupportedScaleType(scaleType) && scaleType != mScaleType) {
            mScaleType = scaleType;
            // Обновляет базовую матрицу (Если масштабируется) или сбрасывает матрицу (Если не масштабируется)
            update();
        }
    }

    // Возвращает - масштабируемо или нет
    public boolean isZoomable() {
        return mZoomEnabled;
    }

    // Устанавливает - будет ли масштабироваться изображение или нет
    public void setZoomable(boolean zoomable) {
        // Устанавливает - будет ли масштабироваться изображение или нет
        mZoomEnabled = zoomable;
        // Обновляет базовую матрицу (Если масштабируется) или сбрасывает матрицу (Если не масштабируется)
        update();
    }

    // Обновляет базовую матрицу (Если масштабируется) или сбрасывает матрицу (Если не масштабируется)
    public void update() {
        // Если масштабируется
        if (mZoomEnabled) {
            // Update the base matrix using the current drawable
            // Обновите базовую матрицу, используя текущий доступный для рисования
            // Рассчитать матрицу для FIT_CENTER
            updateBaseMatrix(mImageView.getDrawable());
        } else {
            // Reset the Matrix...
            // Сбросить матрицу ...
            resetMatrix();
        }
    }

    /**
     * Get the display matrix
     * Получите матрицу дисплея
     *
     * @param matrix target matrix to copy to
     * @param matrix целевая матрица для копирования в
     */
    public void getDisplayMatrix(Matrix matrix) {
        matrix.set(getDrawMatrix());
    }

    /**
     * Get the current support matrix
     * Получить текущую матрицу поддержки
     */
    public void getSuppMatrix(Matrix matrix) {
        matrix.set(mSuppMatrix);
    }

    private Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    public Matrix getImageMatrix() {
        return mDrawMatrix;
    }

    public void setZoomTransitionDuration(int milliseconds) {
        this.mZoomDuration = milliseconds;
    }

    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     * Вспомогательный метод, который «распаковывает» матрицу и возвращает требуемое значение
     *
     * @param matrix     Matrix to unpack
     * @param whichValue Which value from Matrix.M* to return
     * @return returned value
     *
     * @param matrix Матрица для распаковки
     * @param whichValue Какое значение из Matrix.M * возвращать
     * @return возвращаемое значение
     */
    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    /**
     * Resets the Matrix back to FIT_CENTER, and then displays its contents
     * Сбрасывает матрицу обратно в FIT_CENTER, а затем отображает ее содержимое
     */
    private void resetMatrix() {
        mSuppMatrix.reset();
        setRotationBy(mBaseRotation);
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();
    }

    private void setImageViewMatrix(Matrix matrix) {
        mImageView.setImageMatrix(matrix);
        // Call MatrixChangedListener if needed
        // При необходимости вызовите MatrixChangedListener
        if (mMatrixChangeListener != null) {
            RectF displayRect = getDisplayRect(matrix);
            if (displayRect != null) {
                mMatrixChangeListener.onMatrixChanged(displayRect);
            }
        }
    }

    /**
     * Helper method that simply checks the Matrix, and then displays the result
     * Вспомогательный метод, который просто проверяет матрицу, а затем отображает результат
     */
    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(getDrawMatrix());
        }
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     * Вспомогательный метод, который сопоставляет предоставленную матрицу текущему Drawable
     *
     * @param matrix - Matrix to map Drawable against
     * @return RectF - Displayed Rectangle
     *
     * @param matrix - матрица для сопоставления Drawable с
     * @return RectF - отображаемый прямоугольник
     */
    private RectF getDisplayRect(Matrix matrix) {
        Drawable d = mImageView.getDrawable();
        if (d != null) {
            mDisplayRect.set(0, 0, d.getIntrinsicWidth(),
                d.getIntrinsicHeight());
            matrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    /**
     * Calculate Matrix for FIT_CENTER
     * Рассчитать матрицу для FIT_CENTER
     *
     * @param drawable - Drawable being displayed
     * @param drawable - Отображаемый объект Drawable
     */
    private void updateBaseMatrix(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        final float viewWidth = getImageViewWidth(mImageView);
        final float viewHeight = getImageViewHeight(mImageView);
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();
        mBaseMatrix.reset();
        final float widthScale = viewWidth / drawableWidth;
        final float heightScale = viewHeight / drawableHeight;
        if (mScaleType == ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F,
                (viewHeight - drawableHeight) / 2F);

        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                (viewHeight - drawableHeight * scale) / 2F);

        } else if (mScaleType == ScaleType.CENTER_INSIDE) {
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                (viewHeight - drawableHeight * scale) / 2F);

        } else {
            RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
            RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);
            if ((int) mBaseRotation % 180 != 0) {
                mTempSrc = new RectF(0, 0, drawableHeight, drawableWidth);
            }
            switch (mScaleType) {
                case FIT_CENTER:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);
                    break;
                case FIT_START:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.START);
                    break;
                case FIT_END:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.END);
                    break;
                case FIT_XY:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.FILL);
                    break;
                default:
                    break;
            }
        }
        resetMatrix();
    }

    private boolean checkMatrixBounds() {
        final RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }
        final float height = rect.height(), width = rect.width();
        float deltaX = 0, deltaY = 0;
        final int viewHeight = getImageViewHeight(mImageView);
        if (height <= viewHeight) {
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rect.top;
                    break;
                case FIT_END:
                    deltaY = viewHeight - height - rect.top;
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rect.top;
                    break;
            }
            mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
        } else if (rect.top > 0) {
            mVerticalScrollEdge = VERTICAL_EDGE_TOP;
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            mVerticalScrollEdge = VERTICAL_EDGE_BOTTOM;
            deltaY = viewHeight - rect.bottom;
        } else {
            mVerticalScrollEdge = VERTICAL_EDGE_NONE;
        }
        final int viewWidth = getImageViewWidth(mImageView);
        if (width <= viewWidth) {
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rect.left;
                    break;
                case FIT_END:
                    deltaX = viewWidth - width - rect.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;
                    break;
            }
            mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
        } else if (rect.left > 0) {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_LEFT;
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mHorizontalScrollEdge = HORIZONTAL_EDGE_RIGHT;
        } else {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_NONE;
        }
        // Finally actually translate the matrix
        // Наконец-то переведите матрицу
        mSuppMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    private int getImageViewWidth(ImageView imageView) {
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    private int getImageViewHeight(ImageView imageView) {
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    private void cancelFling() {
        if (mCurrentFlingRunnable != null) {
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }

    private class AnimatedZoomRunnable implements Runnable {

        private final float mFocalX, mFocalY;
        private final long mStartTime;
        private final float mZoomStart, mZoomEnd;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom,
            final float focalX, final float focalY) {
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {
            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getScale();
            onGestureListener.onScale(deltaScale, mFocalX, mFocalY);
            // We haven't hit our target scale yet, so post ourselves again
            // Мы еще не достигли целевого масштаба, так что публикуйте себя еще раз
            if (t < 1f) {
                Compat.postOnAnimation(mImageView, this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }
    }

    private class FlingRunnable implements Runnable {

        private final OverScroller mScroller;
        private int mCurrentX, mCurrentY;

        public FlingRunnable(Context context) {
            mScroller = new OverScroller(context);
        }

        public void cancelFling() {
            mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX,
            int velocityY) {
            final RectF rect = getDisplayRect();
            if (rect == null) {
                return;
            }
            final int startX = Math.round(-rect.left);
            final int minX, maxX, minY, maxY;
            if (viewWidth < rect.width()) {
                minX = 0;
                maxX = Math.round(rect.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }
            final int startY = Math.round(-rect.top);
            if (viewHeight < rect.height()) {
                minY = 0;
                maxY = Math.round(rect.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }
            mCurrentX = startX;
            mCurrentY = startY;
            // If we actually can move, fling the scroller
            // Если мы действительно можем двигаться, бросьте скроллер
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX,
                    maxX, minY, maxY, 0, 0);
            }
        }

        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return; // remaining post that should not be handled
                // оставшаяся запись, которую не следует обрабатывать
            }
            if (mScroller.computeScrollOffset()) {
                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();
                mSuppMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
                // Вспомогательный метод, который просто проверяет матрицу, а затем отображает результат
                checkAndDisplayMatrix();
                mCurrentX = newX;
                mCurrentY = newY;
                // Post On animation
                // Опубликовать в анимации
                Compat.postOnAnimation(mImageView, this);
            }
        }
    }
}
