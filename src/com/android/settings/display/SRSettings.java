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

public class SRSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "SRSettings";
    private static final String KEY_SR_SETTING = "sr_setting";
    private static final String KEY_SR_OSD_SETTING = "sr_osd_setting";
    private static final String KEY_SR_CONTRAST_SETTING = "sr_contrast_setting";

    private static final String PROPERTY_SR_MODE = "persist.sys.svep.mode";
    private static final String PROPERTY_SR_DISABLE_OSD = "persist.sys.svep.disable_sr_osd";
    private static final String PROPERTY_SR_CONTRAST_MODE = "persist.sys.svep.contrast_mode";
    private static final String PROPERTY_MEMC_MODE = "persist.sys.memc.mode";

    private SwitchPreference mSrSetting;
    private SwitchPreference mSrOsdSetting;
    private SwitchPreference mSrContrastSetting;
    private RkDisplayOutputManager mRkDisplayOutputManager;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        getActivity().setTitle(context.getString(R.string.sr_title));
        addPreferencesFromResource(R.xml.sr_settings);
        mRkDisplayOutputManager = new RkDisplayOutputManager();
        mSrSetting = findPreference(KEY_SR_SETTING);
        if (mSrSetting != null) {
            mSrSetting.setChecked(isSrEnable());
            mSrSetting.setOnPreferenceChangeListener(this);
        }
        mSrOsdSetting = findPreference(KEY_SR_OSD_SETTING);
        if (mSrOsdSetting != null) {
            Log.d(TAG, "isSrOsdEnable " + isSrOsdEnable());
            mSrOsdSetting.setEnabled(isSrEnable());
            mSrOsdSetting.setChecked(isSrOsdEnable());
            mSrOsdSetting.setOnPreferenceChangeListener(this);
        }
        mSrContrastSetting = findPreference(KEY_SR_CONTRAST_SETTING);
        if (mSrContrastSetting != null) {
            mSrContrastSetting.setEnabled(isSrEnable());
            mSrContrastSetting.setChecked(isSrContrastEnable());
            mSrContrastSetting.setOnPreferenceChangeListener(this);
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
        if (KEY_SR_SETTING.equals(key)) {
            if (isChecked) {
                SystemProperties.set(PROPERTY_SR_MODE, "1"); 
                SystemProperties.set(PROPERTY_MEMC_MODE, "0");
                mRkDisplayOutputManager.setAiPqEnable(false, false,
                    false, false);
            } else {
                SystemProperties.set(PROPERTY_SR_MODE, "0");
            }
            mSrOsdSetting.setEnabled(isChecked);
            mSrContrastSetting.setEnabled(isChecked);
        } else if (KEY_SR_OSD_SETTING.equals(key)) {
            if (isChecked) {
                SystemProperties.set(PROPERTY_SR_DISABLE_OSD, "0");
            } else {
                SystemProperties.set(PROPERTY_SR_DISABLE_OSD, "1");
            }
        } else if (KEY_SR_CONTRAST_SETTING.equals(key)) {
            if (isChecked) {
                SystemProperties.set(PROPERTY_SR_CONTRAST_MODE, "1");
            } else {
                SystemProperties.set(PROPERTY_SR_CONTRAST_MODE, "0");
            }
        }
        return true;
    }

    public static boolean isSrEnable() {
        return SystemProperties.getInt(PROPERTY_SR_MODE, 0) == 1;
    }

    private boolean isSrOsdEnable() {
        return SystemProperties.getInt(PROPERTY_SR_DISABLE_OSD, 0) == 0;
    }

    private boolean isSrContrastEnable() {
        boolean contrastMode = SystemProperties.getInt(PROPERTY_SR_CONTRAST_MODE, 0) == 1;
        return contrastMode;
    }

    public static boolean isAvailable() {
        return "true".equals(SystemProperties.get("ro.vendor.sr_settings"));
    }
 }