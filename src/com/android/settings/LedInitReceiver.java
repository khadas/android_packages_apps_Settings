/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class LedInitReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            return;
        }
        Log.d("LedInitReceiver","BOOT UPDATE");
        int mode;
        mode = LedSettings.getLedModeProp(LedSettings.LED_WHITE);
        LedSettings.setLedMode(LedSettings.LED_WHITE,mode);
        mode = LedSettings.getLedModeProp(LedSettings.LED_RED);
        LedSettings.setLedMode(LedSettings.LED_RED,mode);
    }
}
