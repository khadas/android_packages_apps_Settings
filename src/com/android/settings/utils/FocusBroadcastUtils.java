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
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Send broadcast to obtain focus.
 */
public class FocusBroadcastUtils {
    public static final String INTENT_ACTION_SETTINGS_HOMEPAGE_REQUEST_FOCUS =
            "com.android.settings.homepage.REQUEST_FOCUS";

    public static final String INTENT_ACTION_SUBSETTINGS_REQUEST_FOCUS =
            "com.android.settings.subsettings.REQUEST_FOCUS";

    public static final String INTENT_ACTION_SAFETY_CENTER_REQUEST_FOCUS =
            "com.android.permissioncontroller.safetycenter.REQUEST_FOCUS";

    public static void requestHomepageRequestFocusReceiver(Context context) {
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(INTENT_ACTION_SETTINGS_HOMEPAGE_REQUEST_FOCUS));
    }

    public static void requestSubSettingsRequestFocusReceiver(Context context) {
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(INTENT_ACTION_SUBSETTINGS_REQUEST_FOCUS));
    }

    public static void requestSafetyCenterRequestFocusReceiver(Context context) {
        Intent intent = new Intent(INTENT_ACTION_SAFETY_CENTER_REQUEST_FOCUS);
        context.sendBroadcast(intent);
    }
}
//--------------------------