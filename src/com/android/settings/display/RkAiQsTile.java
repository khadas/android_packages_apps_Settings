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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.service.quicksettings.TileService;
import android.service.quicksettings.Tile;
import android.util.Log;

import com.android.internal.statusbar.IStatusBarService;
import com.android.settings.R;

import static android.content.RKFeatureManager.FEATURE_ROCKCHIP_AI;

public class RkAiQsTile extends TileService {
    private static final String TAG = "RkAiQsTile";
    private static final String PROPERTY_STATE = "persist.sys.rkai.launcher";
    private static final String STATE_ENABLE = "1";
    private static final String STATE_DISABLE = "0";

    private PackageManager mPackageManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mPackageManager = getPackageManager();
    }

    private void refreshTile() {
        if (mPackageManager.hasSystemFeature(FEATURE_ROCKCHIP_AI)) {
            boolean enable = STATE_ENABLE.equals(SystemProperties.get(PROPERTY_STATE));
            getQsTile().setState(enable ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            getQsTile().setSubtitle(enable ? getString(R.string.rkai_on) : getString(R.string.rkai_off));
        } else {
            final ComponentName cn = new ComponentName(getPackageName(), getClass().getName());
            try {
                getPackageManager().setComponentEnabledSetting(
                        cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                final IStatusBarService statusBarService = IStatusBarService.Stub.asInterface(
                        ServiceManager.checkService(Context.STATUS_BAR_SERVICE));
                if (statusBarService != null) {
                    statusBarService.remTile(cn);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to modify QS tile for component " +
                        cn.toString(), e);
            }
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().setSubtitle(getString(R.string.rkai_off));
        }
        getQsTile().updateTile();
    }

    // Called when the user adds your tile.
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.i(TAG, "onTileAdded");
        refreshTile();
    }

    // Called when your app can update your tile.
    @Override
    public void onStartListening() {
        super.onStartListening();
        Log.i(TAG, "onStartListening");
        refreshTile();
    }

    // Called when your app can no longer update your tile.
    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.i(TAG, "onStopListening");
        refreshTile();
    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        super.onClick();
        boolean lastEnable = STATE_ENABLE.equals(SystemProperties.get(PROPERTY_STATE));
        boolean enable = !lastEnable;
        Log.i(TAG, "click to enable=" + enable);
        SystemProperties.set(PROPERTY_STATE, enable ? STATE_ENABLE : STATE_DISABLE);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.rockchip.ailauncher",
                "com.rockchip.ailauncher.FloatballService"));
        if (enable) {
            startService(intent);
        } else {
            stopService(intent);
        }
        refreshTile();
    }

    // Called when the user removes your tile.
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.i(TAG, "onTileRemoved");
    }
}
