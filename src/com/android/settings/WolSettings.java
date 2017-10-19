/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;


public class WolSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, Indexable {

    private static final String TAG = "WolSettings";
    private static final String WOL_STATE_SYS = "/sys/class/wol/enable";
    private static final String KEY_WOL = "wol_btn";
    private SwitchPreference mWolPref;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DATE_TIME;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.wol_prefs);

        initUI();
    }

    private void initUI() {

        mWolPref = (SwitchPreference) findPreference(KEY_WOL);
        mWolPref.setOnPreferenceChangeListener(this);
        mWolPref.setChecked(getWolState());
    }

    private boolean getWolState() {
        boolean enabled = false;
        try {
            FileReader fread = new FileReader(WOL_STATE_SYS);
            BufferedReader buffer = new BufferedReader(fread);
            String str = null;
            while ((str = buffer.readLine()) != null) {
                if (str.equals("1")) {
                   enabled = true;
                   break;
                } else {
                   enabled = false;
                }
            }
            buffer.close();
            fread.close();
        } catch (IOException e) {
            Log.e(TAG, "IO Exception");
        }
        return enabled;
    }

    private void setWolState(boolean enabled) {
        try {
            RandomAccessFile rdf = null;
            rdf = new RandomAccessFile(WOL_STATE_SYS, "rw");
            rdf.writeBytes(enabled ? "1" : "0");
            rdf.close();
        } catch (IOException re) {
            Log.e(TAG, "IO Exception");
        }

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(KEY_WOL)) {
            boolean enabled = (boolean) newValue;
            setWolState(enabled);
        }
        return true;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new WolSearchIndexProvider();

    private static class WolSearchIndexProvider extends BaseSearchIndexProvider {

        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(
                Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList<>();
            if (UserManager.isDeviceInDemoMode(context)) {
                return result;
            }

            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.wol_prefs;
            result.add(sir);

            return result;
        }
    }
}
