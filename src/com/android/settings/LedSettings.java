/*
 * Copyright (C) 2012 The Android Open Source Project
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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemProperties;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.text.TextPaint;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.LinearLayout;

//import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.dream.DreamBackend;
import com.android.settingslib.dream.DreamBackend.DreamInfo;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;


public class LedSettings extends SettingsPreferenceFragment
         implements Preference.OnPreferenceChangeListener {
    private static final String TAG = LedSettings.class.getSimpleName();
    private static final String LED_RADIO_GROUP = "led";
    private static final boolean DEBUG = false;
    private static final String PROP_LED_WHITE_TRIGGER = "persist.sys.white.led.trigger";
	private static final String PROP_LED_RED_TRIGGER = "persist.sys.red.led.trigger";
    //private static final String PROP_LED_RED_MODE = "persist.sys.red.led.mode";
    private static final String SYS_LED_WHITE_TRIGGER = "/sys/class/leds/sys_led/trigger";
    private static final String SYS_LED_RED_TRIGGER = "/sys/class/leds/red_led/trigger";	
    //private static final String SYS_LED_RED_MODE = "/sys/class/redled/mode";
    private static final String KEY_LED_WHITE = "whiteLed";
    private static final String KEY_LED_RED = "redLed";
    //private BoardInfo mBoardInfo;
    private static int mLedType;

    private Context mContext;

    private static final int INDEX_HEARTBEAT = 0;
    private static final int INDEX_ON = 1;
    private static final int INDEX_OFF = 2;
    private static final int DEFAULT_MODE = INDEX_ON;
    public static final int LED_WHITE = 0;
    public static final int LED_RED   = 1;

    private static final int INDEX_LED[] = {
            INDEX_HEARTBEAT,
            INDEX_ON,
            INDEX_OFF,
    };

    private static final String ModeList[] = {
            "heartbeat",
            "default-on",
            "off",
    };

    public static LedSettings newInstance() {
        return new LedSettings();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        int mode = 0;
        setPreferencesFromResource(R.xml.leds, null);
        String[] list= mContext.getResources().getStringArray(R.array.led_title_entries);
        final ListPreference whitePref = (ListPreference) findPreference(KEY_LED_WHITE);
        final ListPreference redPref = (ListPreference) findPreference(KEY_LED_RED);
		mode = getLedModeProp(LED_WHITE);
		whitePref.setValue(Integer.toString(mode));
		whitePref.setSummary(list[mode]);
		whitePref.setOnPreferenceChangeListener(this);

		mode = getLedModeProp(LED_RED);
		redPref.setValue(Integer.toString(mode));
		redPref.setSummary(list[mode]);
		redPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFromBackend();
    }

    public static int setLedMode(int type, int mode) {

        if (DEBUG) Log.d(TAG,"setLedMode: " + mode);

        try {
            BufferedWriter bufWriter = null;
			if (type == LED_WHITE) {
			   bufWriter = new BufferedWriter(new FileWriter(SYS_LED_WHITE_TRIGGER));
			   bufWriter.write(ModeList[mode]);
			} else {
			   bufWriter = new BufferedWriter(new FileWriter(SYS_LED_RED_TRIGGER));
			   //bufWriter.write(String.valueOf(INDEX_LED[mode]));
			   bufWriter.write(ModeList[mode]);
			}
            bufWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"can't write the led node");
            return -1;
        }
        return 0;
    }

    public static void setLedModeProp(int type, int mode) {

        if (type == LED_WHITE)
            SystemProperties.set(PROP_LED_WHITE_TRIGGER, String.valueOf(mode));
        else
            //SystemProperties.set(PROP_LED_RED_MODE, String.valueOf(mode));
			SystemProperties.set(PROP_LED_RED_TRIGGER, String.valueOf(mode));		
    }

    public static int getLedModeProp(int type) {

        int mode;
        if (type == LED_WHITE)
            mode = SystemProperties.getInt(PROP_LED_WHITE_TRIGGER, DEFAULT_MODE);
        else
            //mode = SystemProperties.getInt(PROP_LED_RED_MODE, DEFAULT_MODE);
			mode = SystemProperties.getInt(PROP_LED_RED_TRIGGER, INDEX_OFF);
        return mode;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int mode = Integer.parseInt((String) newValue);
        switch (preference.getKey()) {
             case KEY_LED_WHITE:
                  setLedMode(LED_WHITE, mode);
                  setLedModeProp(LED_WHITE, mode);
                  break;
             case KEY_LED_RED:
                  setLedMode(LED_RED, mode);
                  setLedModeProp(LED_RED, mode);
                  break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    private void refreshFromBackend() {
        int mode;
        if (getActivity() == null) {
            Log.d(TAG, "No activity, not refreshing");
            return;
        }
        String[] list= mContext.getResources().getStringArray(R.array.led_title_entries);
        final ListPreference whitePref = (ListPreference) findPreference(KEY_LED_WHITE);
        if (whitePref != null) {
            mode = getLedModeProp(LED_WHITE);
            whitePref.setValue(Integer.toString(mode));
            whitePref.setSummary(list[mode]);
        }
        final ListPreference redPref = (ListPreference) findPreference(KEY_LED_RED);
        if (redPref != null) {
            mode = getLedModeProp(LED_RED);
            redPref.setValue(Integer.toString(mode));
            redPref.setSummary(list[mode]);
        }
    }

    @Override
    public int getMetricsCategory() {
        //return MetricsProto.MetricsEvent.KHADAS_LED;
		return 7;
    }
}
