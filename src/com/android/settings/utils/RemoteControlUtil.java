/*
 * Copyright 2025 Rockchip Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.utils;

//---------rk-code----------
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import com.android.internal.util.Preconditions;

public class RemoteControlUtil {

    /**
     * enable remote control support
     */
    public static boolean isSupportRemoteControl(Context context) {
         boolean result = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_APP_FOCUS);
         Log.i("RemoteControlUtil", "isSupportRemoteControl result = " + result);
         return result;
    }

    /**
     * inject click event
     */
    public static void injectClick(float x, float y, Activity activity) {
        Log.i("RemoteControlUtil", "injectClick");
        new Thread(() -> {
            InputManager inputManager = Preconditions.checkNotNull(activity.getSystemService(InputManager.class));;
            long now = SystemClock.uptimeMillis();
            int action = MotionEvent.ACTION_DOWN;
            MotionEvent clickDown = MotionEvent.obtain(now, now, action, x, y, 0);
            inputManager.injectInputEvent(clickDown, 0);
            now = SystemClock.uptimeMillis();
            action = MotionEvent.ACTION_UP;
            MotionEvent clickUp = MotionEvent.obtain(now, now, action, x, y, 0);
            inputManager.injectInputEvent(clickUp, 0);
        }).start();
    }
}
//--------------------------