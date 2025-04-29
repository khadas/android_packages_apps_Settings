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

public class AipqSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "AipqSettings";
    private static final String KEY_HWPQ_SETTING = "hwpq_setting";
    private static final String KEY_AISD_SETTING = "aisd_setting";
    private static final String KEY_AISR_SETTING = "aisr_setting";
    private static final String KEY_AIMEMC_SETTING = "aimemc_setting";
    private static final String KEY_AIDC_SETTING = "aidc_setting";
    private static final String PROPERTY_SR_MODE = "persist.sys.svep.mode";
    private static final String PROPERTY_MEMC_MODE = "persist.sys.memc.mode";

    private RkDisplayOutputManager mRkDisplayOutputManager;
    private SwitchPreference mHwpqSetting;
    private SwitchPreference mAisdSetting;
    private SwitchPreference mAisrSetting;
    private SwitchPreference mAimemcSetting;
    private SwitchPreference mAidcSetting;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        getActivity().setTitle(context.getString(R.string.aipq_title));
        addPreferencesFromResource(R.xml.aipq_settings);
        mRkDisplayOutputManager = new RkDisplayOutputManager();
        boolean[] aipqStatus = mRkDisplayOutputManager.getAiPqEnable();
        mHwpqSetting = findPreference(KEY_HWPQ_SETTING);
        if (mHwpqSetting != null) {
            mHwpqSetting.setOnPreferenceChangeListener(this);
        }
        mHwpqSetting.setChecked(mRkDisplayOutputManager.getPqEnable());
        mAisdSetting = findPreference(KEY_AISD_SETTING);
        if (mAisdSetting!= null) {
            mAisdSetting.setOnPreferenceChangeListener(this);
        }
        mAisdSetting.setChecked(aipqStatus[0]);
        mAisrSetting = findPreference(KEY_AISR_SETTING);
        if (mAisrSetting != null) {
            mAisrSetting.setOnPreferenceChangeListener(this);
        }
        mAisrSetting.setChecked(aipqStatus[1]);
        mAimemcSetting = findPreference(KEY_AIMEMC_SETTING);
        if (mAimemcSetting!= null) {
            mAimemcSetting.setOnPreferenceChangeListener(this);
        }
        mAimemcSetting.setChecked(aipqStatus[2]);
        mAidcSetting = findPreference(KEY_AIDC_SETTING);
        if (mAidcSetting!= null) {
            mAidcSetting.setOnPreferenceChangeListener(this);
        }
        mAidcSetting.setChecked(aipqStatus[3]);
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        Log.i(TAG, key + " onPreferenceChange:" + newValue);
        boolean isChecked = (boolean) newValue;
        if (KEY_HWPQ_SETTING.equals(key)) {
            mRkDisplayOutputManager.setPqEnable(isChecked);
            if (!isChecked) {
                mAisdSetting.setChecked(false);
                mRkDisplayOutputManager.setAiPqEnable(false, mAisrSetting.isChecked(),
                mAimemcSetting.isChecked(), mAidcSetting.isChecked());
            }
        } else if (KEY_AISD_SETTING.equals(key)) {
            if (isChecked) {
                mHwpqSetting.setChecked(true);
                mRkDisplayOutputManager.setPqEnable(true);
                SystemProperties.set(PROPERTY_SR_MODE, "0");
                SystemProperties.set(PROPERTY_MEMC_MODE, "0");
            }
            mRkDisplayOutputManager.setAiPqEnable(isChecked, mAisrSetting.isChecked(),
                mAimemcSetting.isChecked(), mAidcSetting.isChecked());
        } else if (KEY_AISR_SETTING.equals(key)) {
            if (isChecked) {
                SystemProperties.set(PROPERTY_SR_MODE, "0");
                SystemProperties.set(PROPERTY_MEMC_MODE, "0");
            }
            mRkDisplayOutputManager.setAiPqEnable(mAisdSetting.isChecked(), isChecked,
                mAimemcSetting.isChecked(), mAidcSetting.isChecked());
        } else if  (KEY_AIMEMC_SETTING.equals(key)) {
            if (isChecked) {
                SystemProperties.set(PROPERTY_SR_MODE, "0");
                SystemProperties.set(PROPERTY_MEMC_MODE, "0");
            }
            mRkDisplayOutputManager.setAiPqEnable(mAisdSetting.isChecked(), mAisrSetting.isChecked(),
                isChecked, mAidcSetting.isChecked());
        } else if (KEY_AIDC_SETTING.equals(key)) {
            if (isChecked) {
                SystemProperties.set(PROPERTY_SR_MODE, "0");
                SystemProperties.set(PROPERTY_MEMC_MODE, "0");
            }
            mRkDisplayOutputManager.setAiPqEnable(mAisdSetting.isChecked(), mAisrSetting.isChecked(),
                mAimemcSetting.isChecked(), isChecked);
        }
        return true;
    }

    public static boolean isAvailable() {
        return "true".equals(SystemProperties.get("ro.vendor.aipq_settings"));
    }
}