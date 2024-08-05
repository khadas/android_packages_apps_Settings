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

import android.content.Context;
import android.os.SystemProperties;
import android.service.quicksettings.TileService;
import android.service.quicksettings.Tile;
import android.util.Log;
import com.android.settings.R;
import java.io.RandomAccessFile;

/** TileService for test */
public class SrTileService extends TileService {

    public static final String PROPERTY_SR_MODE = "persist.sys.svep.mode";
    public static final String TAG = "SrTileService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // Called when the user adds your tile.
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Tile tile = getQsTile();
        tile.setSubtitle(SystemProperties.getInt(PROPERTY_SR_MODE, 0) == 1 ?
            this.getString(R.string.sr_on) : this.getString(R.string.sr_off));
        tile.updateTile();
        tile.setState(SystemProperties.getInt(PROPERTY_SR_MODE, 0) == 1 ?
            Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
    }

    // Called when your app can update your tile.
    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        tile.setSubtitle(SystemProperties.getInt(PROPERTY_SR_MODE, 0) == 1 ?
            this.getString(R.string.sr_on) : this.getString(R.string.sr_off));
        tile.updateTile();
        tile.setState(SystemProperties.getInt(PROPERTY_SR_MODE, 0) == 1 ?
            Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
    }

    // Called when your app can no longer update your tile.
    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        super.onClick();
        int[][] rgb;
        Tile tile = getQsTile();
        if(SystemProperties.getInt(PROPERTY_SR_MODE, 0) == 0) {
            SystemProperties.set(PROPERTY_SR_MODE, "1");
            tile.setSubtitle(this.getString(R.string.sr_on));
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            SystemProperties.set(PROPERTY_SR_MODE, "0");
            tile.setSubtitle(this.getString(R.string.sr_off));
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

    // Called when the user removes your tile.
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
