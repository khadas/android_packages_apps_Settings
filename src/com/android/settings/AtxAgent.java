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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

import java.util.List;

/**
 * Listens to {@link Intent.ACTION_PRE_BOOT_COMPLETED} and {@link Intent.ACTION_USER_INITIALIZED}
 * performs setup steps for a managed profile (disables the launcher icon of the Settings app,
 * adds cross-profile intent filters for the appropriate Settings activities), and disables the
 * webview setting for non-admin users.
 */
public class AtxAgent extends BroadcastReceiver {
    private static final String TAG = "AtxAgent";

    @Override
    public void onReceive(Context context, Intent broadcast) {
			Log.d(TAG, "AtxAgent Receive");
			Intent intent = new Intent(context, AtxAgentService.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent);
        }

}
