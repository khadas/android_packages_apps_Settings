/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.settings.display;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.settings.display.SrTileService;
import static android.content.RKFeatureManager.FEATURE_ROCKCHIP_AI;

public class BootReceiver extends BroadcastReceiver {

    public static final String PROPERTY_SHOW_SR_SETTING = "ro.vendor.sr_settings";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            final PackageManager pm = context.getPackageManager();
            if ("true".equals(SystemProperties.get(PROPERTY_SHOW_SR_SETTING)) &&
                !pm.hasSystemFeature(FEATURE_ROCKCHIP_AI)) {
                final ComponentName cn = new ComponentName(context, SrTileService.class);
                pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            } else {
                final ComponentName cn = new ComponentName(context, SrTileService.class);
                pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        }
    }
}
