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

import android.util.Log;
import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SeekBarDialogPreference;
import android.os.SystemProperties;

import java.util.Map;
import java.io.*;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.os.DisplayOutputManager;
import android.graphics.Rect;
import com.droidlogic.app.DisplayPositionManager;

public class HdmiScreenZoomPreference extends SeekBarDialogPreference implements
		SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {

	private static final String TAG = "HdmiScreenZoomPreference";
	private static final int MINIMUN_SCREEN_SCALE = 0;
	private static final int MAXIMUN_SCREEN_SCALE = 20;
    private static final int MIN_ZOOM_VALUE=80;
	private SeekBar mSeekBar;
	private int mValue = 0;
	private Context context;
    private DisplayPositionManager mDisplayPositionManager;
    private int preZoomValue=0;

	public HdmiScreenZoomPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		setDialogLayoutResource(R.layout.preference_dialog_screen_scale);
		setDialogIcon(R.drawable.ic_settings_screen_scale);
        mDisplayPositionManager = new DisplayPositionManager(context);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		mSeekBar = getSeekBar(view);
		preZoomValue= Integer.valueOf(SystemProperties.get("sys.hdmi_screen.scale","100"));
        mSeekBar.setProgress(preZoomValue-MIN_ZOOM_VALUE);
		mSeekBar.setOnSeekBarChangeListener(this);
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromTouch) {
		mValue = progress + MIN_ZOOM_VALUE;
		if (mValue > 100) {
			mValue = 100;
		}
		mDisplayPositionManager.zoomByPercent(mValue);
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// If start tracking, record the initial position
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}


	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			int value = mSeekBar.getProgress() + MIN_ZOOM_VALUE;
			mDisplayPositionManager.zoomByPercent(value);
			SystemProperties.set("sys.hdmi_screen.scale",String.valueOf(value));
		} else {
			SystemProperties.set("sys.hdmi_screen.scale",String.valueOf(preZoomValue));
			mDisplayPositionManager.zoomByPercent(preZoomValue);
		}

		mDisplayPositionManager.saveDisplayPosition();
	}
}
