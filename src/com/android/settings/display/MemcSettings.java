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
import android.os.Bundle;
import android.os.RkDisplayOutputManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.SettingsPreferenceFragment;

public class MemcSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "MemcSettings";
    private static final String KEY_MEMC_SETTING = "memc_setting";
    private static final String KEY_MEMC_OSD_SETTING = "memc_osd_setting";
    private static final String KEY_MEMC_CONTRAST_SETTING = "memc_contrast_setting";
    private static final String PROPERTY_MEMC_MODE = "persist.sys.memc.mode";
    private static final String PROPERTY_MEMC_DISABLE_OSD = "persist.sys.memc.disable_memc_osd";
    private static final String PROPERTY_MEMC_CONTRAST_MODE = "persist.sys.memc.contrast_mode";
    private static final String PROPERTY_SR_MODE = "persist.sys.svep.mode";

    private SwitchPreference mMemcSetting;
    private SwitchPreference mMemcOsdSetting;
    private SwitchPreference mMemcContrastSetting;
    private RkDisplayOutputManager mRkDisplayOutputManager;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
   }

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        getActivity().setTitle(context.getString(R.string.memc_title));
        addPreferencesFromResource(R.xml.memc_settings);
        mRkDisplayOutputManager = new RkDisplayOutputManager();
        mMemcSetting = findPreference(KEY_MEMC_SETTING);
        if (mMemcSetting != null) {
            mMemcSetting.setChecked(isMemcEnable());
            mMemcSetting.setOnPreferenceChangeListener(this);
        }
        mMemcOsdSetting = findPreference(KEY_MEMC_OSD_SETTING);
        if (mMemcOsdSetting != null) {
            mMemcOsdSetting.setEnabled(isMemcEnable());
            mMemcOsdSetting.setChecked(isMemcOsdEnable());
            mMemcOsdSetting.setOnPreferenceChangeListener(this);
        }
        mMemcContrastSetting = findPreference(KEY_MEMC_CONTRAST_SETTING);
        if (mMemcContrastSetting != null) {
            mMemcContrastSetting.setEnabled(isMemcEnable());
            mMemcContrastSetting.setChecked(isMemcContrastEnable());
            mMemcContrastSetting.setOnPreferenceChangeListener(this);
        }
    }

    @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView----------------------------------------");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        Log.i(TAG, "onPreferenceClick " + key);
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object obj) {
        String key = preference.getKey();
        Log.i(TAG, key + " onPreferenceChange:" + obj);
        boolean isChecked = (boolean) obj;
        if (KEY_MEMC_SETTING.equals(key)) {
            if (isChecked) {
                SystemProperties.set(PROPERTY_MEMC_MODE, "1");
                SystemProperties.set(PROPERTY_SR_MODE, "0");
                mRkDisplayOutputManager.setAiPqEnable(false, false,
                    false, false);
            } else {
                SystemProperties.set(PROPERTY_MEMC_MODE, "0");
            }
            mMemcOsdSetting.setEnabled(isChecked);
            mMemcContrastSetting.setEnabled(isChecked);
        } else if (KEY_MEMC_OSD_SETTING.equals(key)) {
            if (isChecked) {
                SystemProperties.set(PROPERTY_MEMC_DISABLE_OSD, "0");
            } else {
                SystemProperties.set(PROPERTY_MEMC_DISABLE_OSD, "1");
            }
        } else if (KEY_MEMC_CONTRAST_SETTING.equals(key)) {
            if (isChecked) {
                SystemProperties.set(PROPERTY_MEMC_CONTRAST_MODE, "1");
            } else {
                SystemProperties.set(PROPERTY_MEMC_CONTRAST_MODE, "0");
            }
        }
        return true;
    }

    public static boolean isMemcEnable() {
       return SystemProperties.getInt(PROPERTY_MEMC_MODE, 0) == 1;
    }
 
    private boolean isMemcOsdEnable() {
        return SystemProperties.getInt(PROPERTY_MEMC_DISABLE_OSD, 0) == 0;
    }

    private boolean isMemcContrastEnable() {
       boolean contrastMode = SystemProperties.getInt(PROPERTY_MEMC_CONTRAST_MODE, 0) == 1;
        return contrastMode;
    }

    public static boolean isAvailable() {
        return "true".equals(SystemProperties.get("ro.vendor.memc_settings"));
    }
 }