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
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
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

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.dream.DreamBackend;
import com.android.settingslib.dream.DreamBackend.DreamInfo;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;


public class LedSettings extends SettingsPreferenceFragment {

    private static final String TAG = LedSettings.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final String PROP_LED_TRIGGER = "persist.sys.led.trigger";
    private static final String SYS_LED_TRIGGER = "/sys/class/leds/led-sys/trigger";


    private Context mContext;


    private static final int INDEX_BREATHE = 0;
    private static final int INDEX_HEARTBEAT = 1;
    private static final int INDEX_ON = 2;
    private static final int INDEX_OFF = 3;

    private static final int DEFAULT_MODE = INDEX_BREATHE;

    private static final int INDEX_LED[] = {
        INDEX_BREATHE,
        INDEX_HEARTBEAT,
        INDEX_ON,
        INDEX_OFF,
    };

    private static final String ModeList[] = {
        "breathe",
        "heartbeat",
        "default-on",
        "off",
    };

    public class LedInfo {
        private int index;
        private String title;
        private boolean isChecked;

        public LedInfo(int index,String title, boolean isChecked) {
            this.index = index;
            this.title = title;
            this.isChecked = isChecked;
        }
    }

    public static class InitLedReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(DEBUG) Log.d(TAG,"BOOT UPDATE");
              setLedMode(getLedModeProp());
        }
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_dreams;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.KHADAS_LED;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        initDisplayInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    private void initDisplayInfo() {
        if (getPreferenceScreen() == null) {
            setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));
        }
        int activeIndex = getLedModeProp();
        String[] list= mContext.getResources().getStringArray(R.array.led_title_list);
        for (int i =0; i < INDEX_LED.length; i++) {
              LedInfo info = new LedInfo(i, list[i], i == activeIndex ? true : false);
              getPreferenceScreen().addPreference(new LedInfoPreference(getPrefContext(), info));
         }

    }

    private static int setLedMode(int mode) {

        if (DEBUG) Log.d(TAG,"setLedMode: " + mode);
        File file = new File(SYS_LED_TRIGGER);
        if((file == null) || !file.exists()){
            Log.e(TAG, "" + SYS_LED_TRIGGER + " no exist");
            return -1;
        }
        try {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(ModeList[mode]);
            pWriter.flush();
            pWriter.close();
            fout.close();

        } catch (IOException e) {

            Log.e(TAG, "setLedMode ERR: " + e);
            return -1;
        }
        return 0;
    }


    private static void setLedModeProp(int mode) {

        SystemProperties.set(PROP_LED_TRIGGER, String.valueOf(mode));
    }

    private static int getLedModeProp() {

        int mode = SystemProperties.getInt(PROP_LED_TRIGGER, DEFAULT_MODE);
        return mode;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class LedInfoPreference extends Preference {

        private final LedInfo mInfo;

        public LedInfoPreference(Context context, LedInfo info) {
            super(context);
            mInfo = info;
            setLayoutResource(R.layout.led_info_row);
            setTitle(mInfo.title);
        }

        public void onBindViewHolder(final PreferenceViewHolder holder) {
            super.onBindViewHolder(holder);

            RadioButton radioButton = (RadioButton) holder.findViewById(android.R.id.button1);
            radioButton.setChecked(mInfo.isChecked);
            radioButton.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    holder.itemView.onTouchEvent(event);
                    return false;
                }
            });
     }

        @Override
        public void performClick() {
            if (mInfo.isChecked)
                return;
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                LedInfoPreference preference =
                        (LedInfoPreference) getPreferenceScreen().getPreference(i);
                preference.mInfo.isChecked = false;
                preference.notifyChanged();
            }
            mInfo.isChecked = true;
            final int index = mInfo.index;
            setLedMode(index);
            setLedModeProp(index);
            notifyChanged();
        }
    }
}
