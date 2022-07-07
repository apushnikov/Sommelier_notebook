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

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;

// Compat - Совместимость
class Compat {

    private static final int SIXTY_FPS_INTERVAL = 1000 / 60;

    public static void postOnAnimation(View view, Runnable runnable) {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            // Вызывает выполнение Runnable на следующем временном шаге анимации.
            // Runnable будет запущен в потоке пользовательского интерфейса.
            postOnAnimationJellyBean(view, runnable);
        } else {
            // Заставляет добавить Runnable в очередь сообщений, чтобы он был запущен по
            // истечении указанного времени. Runnable будет запущен в потоке пользовательского интерфейса.
            //
            //Параметры:
            //действие - Runnable, который будет выполнен.
            //delayMillis - задержка (в миллисекундах) до выполнения Runnable.
            //Возврат:
            //Значение true, если Runnable был успешно помещен в очередь сообщений.
            // Возвращает false в случае сбоя, обычно из-за того, что петлитель, обрабатывающий
            // очередь сообщений, завершает работу. Обратите внимание, что результат «истина»
            // не означает, что Runnable будет обработан - если цикл завершится до того,
            // как наступит время доставки сообщения, сообщение будет отброшено.
            view.postDelayed(runnable, SIXTY_FPS_INTERVAL);
        }
    }

    @TargetApi(16)
    private static void postOnAnimationJellyBean(View view, Runnable runnable) {
        // Вызывает выполнение Runnable на следующем временном шаге анимации.
        // Runnable будет запущен в потоке пользовательского интерфейса.
        //
        //Параметры:
        //действие - Runnable, который будет выполнен.
        view.postOnAnimation(runnable);
    }
}
