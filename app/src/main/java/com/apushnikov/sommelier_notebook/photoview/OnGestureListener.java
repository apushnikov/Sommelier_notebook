/*
 Copyright 2011, 2012 Chris Banes.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.apushnikov.sommelier_notebook.photoview;

// interface - В прослушивателе жестов
interface OnGestureListener {

    // interface - на перетаскивании
    void onDrag(float dx, float dy);

    // interface - в бросании
    void onFling(float startX, float startY, float velocityX,
                 float velocityY);

    // interface - в масштабе
    void onScale(float scaleFactor, float focusX, float focusY);

    // interface - в масштабе
    void onScale(float scaleFactor, float focusX, float focusY, float dx, float dy);
}