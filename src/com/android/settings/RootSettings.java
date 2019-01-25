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
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.os.Bundle;
import android.os.UserManager;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateFormat;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import android.util.Log;


public class RootSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, DialogInterface.OnClickListener {


    private static final String KEY_ROOT = "root_key";



    private DialogInterface mWarnRestart;
    private SwitchPreference mRootPref;
    private boolean rootFlag = false;

    @Override
    protected int getMetricsCategory() {
        return InstrumentedFragment.ROOT;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.root_prefs);
        initUI();
    }

    private void initUI() {
        boolean isrooted = SystemProperties.get("persist.sys.is.root", "0").equals("1") ? true : false ;

        mRootPref = (SwitchPreference) findPreference(KEY_ROOT);
        mRootPref.setOnPreferenceChangeListener(this);
        mRootPref.setChecked(isrooted);
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
    public void onDestroy() {
        super.onDestroy();
        if (mWarnRestart != null) {
           mWarnRestart.dismiss();
        }
    }

    private void warnRestart() {
        if (rootFlag) {
             mWarnRestart = new AlertDialog.Builder(getActivity()).setTitle(
                 getResources().getString(R.string.error_title))
                 .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                 .setMessage(getResources().getString(R.string.root_warn))
                 .setPositiveButton(android.R.string.yes, this)
                 .setNegativeButton(android.R.string.no, this)
                 .show();
        } else {
             mWarnRestart = new AlertDialog.Builder(getActivity()).setTitle(
                 getResources().getString(R.string.error_title))
                 .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                 .setMessage(getResources().getString(R.string.unroot_warn))
                 .setPositiveButton(android.R.string.yes, this)
                 .setNegativeButton(android.R.string.no, this)
                 .show();
        }
		
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mWarnRestart) {
            boolean turnOn = which == DialogInterface.BUTTON_POSITIVE;
            if (turnOn) {
				mRootPref.setChecked(rootFlag);
                restartSystem(rootFlag);
            }
		}
    }	

    private void restartSystem(boolean isrooted) {
        SystemProperties.set("persist.sys.is.root", isrooted ? "1" : "0");
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(KEY_ROOT)) {
            rootFlag = (Boolean) newValue;
            mRootPref.setChecked(!rootFlag);
            warnRestart();
        }
        return false;
    }

}
