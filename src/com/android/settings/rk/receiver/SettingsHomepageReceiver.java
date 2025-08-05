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

package com.android.settings.rk.receiver;

//---------rk-code----------
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.settings.homepage.SettingsHomepageActivity;
import java.lang.ref.WeakReference;

public class SettingsHomepageReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION_SETTINGS_HOMEPAGE_REQUEST_FOCUS = "com.android.settings.SETTINGS_HOME_REQUEST_FOCUS";
    private WeakReference<SettingsHomepageActivity> homepageActivityRef;

    public SettingsHomepageReceiver(SettingsHomepageActivity homepageActivity) {
        this.homepageActivityRef = new WeakReference<>(homepageActivity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (INTENT_ACTION_SETTINGS_HOMEPAGE_REQUEST_FOCUS.equals(action)) {
            if (homepageActivityRef != null && homepageActivityRef.get() != null) {
                homepageActivityRef.get().doRequestFocus();
            }
        }
    }

    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_SETTINGS_HOMEPAGE_REQUEST_FOCUS);
        context.registerReceiver(this, intentFilter, Context.RECEIVER_EXPORTED);
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }
}
//--------------------------