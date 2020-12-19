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

//import com.android.internal.logging.MetricsProto.MetricsEvent;
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


public class FanSettings extends SettingsPreferenceFragment implements
        SwitchBar.OnSwitchChangeListener {
    private static final String TAG = FanSettings.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final String PROP_FAN_ENABLE = "persist.sys.fan.enable";
    private static final String PROP_FAN_MODE  = "persist.sys.fan.mode";
    private static final String PROP_FAN_LEVEL = "persist.sys.fan.level";
    private static final String PROP_FAN_INDEX = "persist.sys.fna.index";


    private static final String SYS_FAN_MODE = "/sys/class/hwmon/hwmon1/mode";
    private static final String SYS_FAN_LEVEL = "/sys/class/thermal/cooling_device0/cur_state";
    private static final String SYS_FAN_ENABLE = "/sys/class/hwmon/hwmon1/enable";

    private static final int MANUAL_MODE = 0;
    private static final int AUTO_MODE = 1;
    private static final int DEFAULT_MODE = AUTO_MODE;

    private static final boolean STATE_DISABLE = false;
    private static final boolean STATE_ENABLE  = true;
    private static final boolean STATE_DEFAULT = STATE_ENABLE;


    private Context mContext;
    private SwitchBar mSwitchBar;
    private boolean mRefreshing = false;


    private static final int INDEX_AUTO = 0;
    private static final int INDEX_LEVEL_1 = 1;
    private static final int INDEX_LEVEL_2 = 2;
    private static final int INDEX_LEVEL_3 = 3;
    private static final int INDEX_LEVEL_4 = 4;
    private static final int INDEX_LEVEL_5 = 5;

    private static final int INDEX_FAN[] = {
            INDEX_AUTO,
            INDEX_LEVEL_1,
            INDEX_LEVEL_2,
            INDEX_LEVEL_3,
            INDEX_LEVEL_4,
            INDEX_LEVEL_5,
    };

    public class FanInfo {
        private int index;
        private String title;
        private boolean isChecked;

        public FanInfo(int index,String title, boolean isChecked) {
            this.index = index;
            this.title = title;
            this.isChecked = isChecked;
        }
    }

    public static class InitFanReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(DEBUG) Log.d(TAG,"BOOT UPDATE");
            boolean enable = getFanEnableProp();
            if (enable) {
                setFanEnable(enable);
                boolean auto = getFanModeProp();
                setFanMode(auto);
                if (!auto) {
                    int level = getFanLevelProp();
                    setFanLevel(level);
				}
			}
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
    public int getMetricsCategory() {
        //return InstrumentedFragment.COOLINGFAN;
		return 6;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
            setFanEnable(isChecked);
            setFanEnableProp(isChecked);
            if (isChecked) {
               boolean auto = getFanModeProp();
               setFanMode(auto);
               if (!auto) {
                  int level = getFanLevelProp();
                  setFanLevel(level);
               }
            }
            initDisplayInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSwitchBar.removeOnSwitchChangeListener(this);
        mSwitchBar.hide();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView emptyView = (TextView) getView().findViewById(android.R.id.empty);
        TextPaint tp = emptyView.getPaint();
        tp.setFakeBoldText(true);
        emptyView.setText(R.string.fan_settings_disabled_prompt);
        emptyView.setTextSize(20);
        setEmptyView(emptyView);

        final SettingsActivity sa = (SettingsActivity) getActivity();
        mSwitchBar = sa.getSwitchBar();
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.show();
    }


    private void initDisplayInfo() {
        mRefreshing = true;
        boolean fanEnabled = SystemProperties.getBoolean(PROP_FAN_ENABLE,STATE_DEFAULT);
        if (mSwitchBar.isChecked() != fanEnabled) {
            mSwitchBar.setChecked(fanEnabled);
        }

        if (getPreferenceScreen() == null) {
            setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));
        }
        getPreferenceScreen().removeAll();

        if (fanEnabled) {
            int activeIndex = SystemProperties.getInt(PROP_FAN_INDEX, 0);
            String[] list= mContext.getResources().getStringArray(R.array.fan_title_list);
            for (int i =0; i < INDEX_FAN.length; i++) {
                FanInfo info = new FanInfo(i, list[i], i == activeIndex ? true : false);
                getPreferenceScreen().addPreference(new FanInfoPreference(getPrefContext(), info));
            }

        }
        mRefreshing = false;
    }

    private static int setFanEnable(boolean enable) {

        if (DEBUG) Log.d(TAG,"setFanEnable: " + enable);

        File file = new File(SYS_FAN_ENABLE);
        if((file == null) || !file.exists()){
            Log.e(TAG, "" + SYS_FAN_ENABLE + " no exist");
            return -1;
        }

        try {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(enable ? "1" : "0");
            pWriter.flush();
            pWriter.close();
            fout.close();

        } catch (IOException e) {

            Log.e(TAG, "setFanEnable ERR: " + e);
            return -1;
        }
        return 0;
    }


    private static int setFanMode(boolean auto) {

		if (DEBUG) Log.d(TAG,"setFanMode: " + auto);

        File file = new File(SYS_FAN_MODE);
        if((file == null) || !file.exists()){
            Log.e(TAG, "" + SYS_FAN_MODE + " no exist");
            return -1;
        }

        try {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(auto ? "1" : "0");
            pWriter.flush();
            pWriter.close();
            fout.close();

        } catch (IOException e) {

            Log.e(TAG, "setFanMode ERR: " + e);
            return -1;
        }
        return 0;
    }


    private static int setFanLevel(int level) {

        if (DEBUG) Log.d(TAG,"setFanLevel: " + level);

        File file = new File(SYS_FAN_LEVEL);
        if((file == null) || !file.exists()){
            Log.e(TAG, "" + SYS_FAN_LEVEL + " no exist");
            return -1;
        }

        try {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(level);
            pWriter.flush();
            pWriter.close();
            fout.close();

        } catch (IOException e) {

            Log.e(TAG, "setFanMode ERR: " + e);
            return -1;
        }
        return 0;
    }

    private static void setFanModeProp(boolean auto) {

        SystemProperties.set(PROP_FAN_MODE, auto ? "1" : "0");

    }
    private static boolean getFanModeProp() {
        int auto = SystemProperties.getInt(PROP_FAN_MODE, DEFAULT_MODE);
        return auto == 1 ? true : false;
    }

    private static void setFanLevelProp(int level) {

        SystemProperties.set(PROP_FAN_LEVEL, String.valueOf(level));
    }

    private static int  getFanLevelProp() {

        int level = SystemProperties.getInt(PROP_FAN_LEVEL, INDEX_AUTO);
        return level;
    }

    private static void setFanEnableProp(boolean enable) {
        SystemProperties.set(PROP_FAN_ENABLE, enable ? "true" : "false");

    }

    private static boolean getFanEnableProp() {
        boolean enable = SystemProperties.getBoolean(PROP_FAN_ENABLE, STATE_DEFAULT);
        return enable;
    }

    private static void setFanIndexProp(int index) {
        SystemProperties.set(PROP_FAN_INDEX, String.valueOf(index));
    }

    private static int getFanIndexProp() {
        int index = SystemProperties.getInt(PROP_FAN_INDEX, INDEX_AUTO);
        return index;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        initDisplayInfo();
    }

    private class FanInfoPreference extends Preference {

        private final FanInfo mInfo;

        public FanInfoPreference(Context context, FanInfo info) {
            super(context);
            mInfo = info;
            setLayoutResource(R.layout.fan_info_row);
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
                FanInfoPreference preference =
                        (FanInfoPreference) getPreferenceScreen().getPreference(i);
                preference.mInfo.isChecked = false;
                preference.notifyChanged();
            }
            mInfo.isChecked = true;

            final int index = mInfo.index;
            switch (index) {
            case INDEX_AUTO:
                    setFanMode(true);
                    setFanModeProp(true);
                    break;
            case INDEX_LEVEL_1:
            case INDEX_LEVEL_2:
            case INDEX_LEVEL_3:
            case INDEX_LEVEL_4:
            case INDEX_LEVEL_5:
                    setFanMode(false);
                    setFanLevel(index);
                    setFanModeProp(false);
                    setFanLevelProp(index);
                    break;
            default:
                    break;
            }
            setFanIndexProp(mInfo.index);
            notifyChanged();
        }
    }
}
