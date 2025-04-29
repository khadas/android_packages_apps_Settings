/*
 * Copyright 2025 Rockchip Electronics S.LSI Co. LTD
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
import android.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class AipqSettingsPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {
    private final String mAipqSettingsKey;

    public AipqSettingsPreferenceController(Context context, String key) {
        super(context);
        mAipqSettingsKey = key;
    }

    @Override
    public boolean isAvailable() {
        return AipqSettings.isAvailable();
    }

    @Override
    public String getPreferenceKey() {
        return mAipqSettingsKey;
    }
}