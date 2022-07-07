/*
 Copyright 2011, 2012 Chris Banes.
 <p/>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p/>
 http://www.apache.org/licenses/LICENSE-2.0
 <p/>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.apushnikov.sommelier_notebook.photoview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * Does a whole lot of gesture detecting.
 * Обнаруживает множество жестов.
 */
class CustomGestureDetector {

    private static final int INVALID_POINTER_ID = -1;

    private int mActivePointerId = INVALID_POINTER_ID;
    private int mActivePointerIndex = 0;
    private final ScaleGestureDetector mDetector;

    // Трекер скорости
    // Помощник для отслеживания скорости сенсорных событий, для реализации бросков и других
    // подобных жестов. Используйте get для получения нового экземпляра класса, когда вы
    // собираетесь начать отслеживание. Поместите в него события движения, которые вы получаете,
    // с помощью addMovement (MotionEvent). Если вы хотите определить скорость, вызовите
    // computeCurrentVelocity (int), а затем вызовите getXVelocity (int) и getYVelocity (int),
    // чтобы получить скорость для каждого идентификатора указателя.
    private VelocityTracker mVelocityTracker;

    private boolean mIsDragging;
    private float mLastTouchX;
    private float mLastTouchY;
    private final float mTouchSlop;
    private final float mMinimumVelocity;
    // В прослушивателе жестов
    private OnGestureListener mListener;

    // Конструктор
    // (Обнаруживает множество жестов.)
    CustomGestureDetector(Context context, OnGestureListener listener) {
        final ViewConfiguration configuration = ViewConfiguration
                .get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();

        mListener = listener;
        ScaleGestureDetector.OnScaleGestureListener mScaleListener =
                new ScaleGestureDetector.OnScaleGestureListener() {
            private float lastFocusX, lastFocusY = 0;

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();

                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                    return false;
             
                if (scaleFactor >= 0) {
                    mListener.onScale(scaleFactor,
                            detector.getFocusX(),
                            detector.getFocusY(),
                            detector.getFocusX() - lastFocusX,
                            detector.getFocusY() - lastFocusY
                    );
                    lastFocusX = detector.getFocusX();
                    lastFocusY = detector.getFocusY();
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                lastFocusX = detector.getFocusX();
                lastFocusY = detector.getFocusY();
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                // NO-OP
            }
        };
        mDetector = new ScaleGestureDetector(context, mScaleListener);
    }

    private float getActiveX(MotionEvent ev) {
        try {
            return ev.getX(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getX();
        }
    }

    private float getActiveY(MotionEvent ev) {
        try {
            return ev.getY(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getY();
        }
    }

    // масштабируется
    public boolean isScaling() {
        return mDetector.isInProgress();
    }

    // перетаскивает
    public boolean isDragging() {
        return mIsDragging;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        try {
            // Принимает MotionEvents и отправляет события в ScaleGestureDetector.OnScaleGestureListener,
            // когда это необходимо.
            // Приложения должны передавать этому методу полный и согласованный поток событий.
            // Полный и последовательный поток событий включает в себя все MotionEvents
            // от начального ACTION_DOWN до конечного ACTION_UP или ACTION_CANCEL.
            //
            // Параметры:
            //      событие - событие для обработки
            // Возврат:
            //      Значение true, если событие было обработано и детектор хочет получить
            //      остальные события MotionEvents в этом потоке событий.
            mDetector.onTouchEvent(ev);
            return processTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // Fix for support lib bug, happening when onDestroy is called
            // Исправлена ошибка поддержки библиотеки, возникающая при вызове onDestroy.
            return true;
        }
    }

    private boolean processTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);

                mVelocityTracker = VelocityTracker.obtain();
                if (null != mVelocityTracker) {
                    mVelocityTracker.addMovement(ev);
                }

                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);
                mIsDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = getActiveX(ev);
                final float y = getActiveY(ev);
                final float dx = x - mLastTouchX, dy = y - mLastTouchY;

                if (!mIsDragging) {
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    // Используйте Pythagoras, чтобы узнать, больше ли длина перетаскивания,
                    // чем длина касания.
                    mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                }

                if (mIsDragging) {
                    mListener.onDrag(dx, dy);
                    mLastTouchX = x;
                    mLastTouchY = y;

                    if (null != mVelocityTracker) {
                        mVelocityTracker.addMovement(ev);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                // Recycle Velocity Tracker
                // Трекер скорости рециркуляции
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                if (mIsDragging) {
                    if (null != mVelocityTracker) {
                        mLastTouchX = getActiveX(ev);
                        mLastTouchY = getActiveY(ev);

                        // Compute velocity within the last 1000ms
                        // Вычислить скорость за последние 1000 мс
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);

                        final float vX = mVelocityTracker.getXVelocity(), vY = mVelocityTracker
                                .getYVelocity();

                        // If the velocity is greater than minVelocity, call
                        // listener
                        // Если скорость больше minVelocity, вызвать слушателя
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            mListener.onFling(mLastTouchX, mLastTouchY, -vX,
                                    -vY);
                        }
                    }
                }

                // Recycle Velocity Tracker
                // Трекер скорости рециркуляции
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex = Util.getPointerIndex(ev.getAction());
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    // Это был наш активный указатель, который поднимался вверх. Выберите новый
                    // активный указатель и отрегулируйте его соответствующим образом.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                }
                break;
        }

        mActivePointerIndex = ev
                .findPointerIndex(mActivePointerId != INVALID_POINTER_ID ? mActivePointerId
                        : 0);
        return true;
    }
}
