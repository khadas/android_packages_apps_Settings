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

import android.service.quicksettings.TileService;
import android.service.quicksettings.Tile;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.rk.CmdUtils;
import com.android.settings.rk.SrQsDialog;

/**
 * TileService for test
 */
public class SrTileService extends TileService implements
        SrQsDialog.SrQsDialogListener {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
    }

    // Called when the user adds your tile.
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.i(TAG, "onTileAdded()");
        refreshTile();
    }

    // Called when your app can update your tile.
    @Override
    public void onStartListening() {
        super.onStartListening();
        Log.i(TAG, "onStartListening()");
        refreshTile();
    }

    // Called when your app can no longer update your tile.
    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.i(TAG, "onStopListening()");
        refreshTile();
    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        super.onClick();
        Log.i(TAG, "onClick()");
        //showDialog(new SrQsDialog(this)); may cause SrTileService has leaked IntentReceiver
        SrQsDialog srQsDialog = new SrQsDialog(this, this);
        srQsDialog.show();
    }

    // Called when the user removes your tile.
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.i(TAG, "onTileRemoved()");
    }

    private void refreshTile() {
        try {
            Tile tile = getQsTile();
            boolean isSrEnable = SrQsDialog.isSrEnable();
            tile.setSubtitle(isSrEnable ? getString(R.string.sr_on) : getString(R.string.sr_off));
            tile.setState(isSrEnable ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.updateTile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void srStateChange() {
        Log.i(TAG, "srStateChange");
        refreshTile();
        CmdUtils.execCmd("cmd statusbar collapse");
    }
}