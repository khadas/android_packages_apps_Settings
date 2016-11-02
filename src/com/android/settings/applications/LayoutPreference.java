/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.settings.applications;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.util.Log;
import android.widget.Button;
import android.content.Intent;
import android.content.BroadcastReceiver;

import com.android.settings.R;
import com.android.settings.Utils;

public class LayoutPreference extends Preference implements View.OnKeyListener{

    private View mRootView;
    private Button leftButton;
    private Button rightButton;
    private Context mContext;
    private int mButtonSelected = -1;

    public LayoutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSelectable(true);
        mContext = context;
        final TypedArray a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.Preference, 0, 0);
        int layoutResource = a.getResourceId(com.android.internal.R.styleable.Preference_layout,
                0);
        if (layoutResource == 0) {
            throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
        }
        // Need to create view now so that findViewById can be called immediately.
        final View view = LayoutInflater.from(getContext())
                .inflate(layoutResource, null, false);

        final ViewGroup allDetails = (ViewGroup) view.findViewById(R.id.all_details);
        leftButton = (Button) view.findViewById(R.id.left_button);
        rightButton = (Button) view.findViewById(R.id.right_button);
        if (allDetails != null) {
            Utils.forceCustomPadding(allDetails, true /* additive padding */);
        }
        mRootView = view;
        setShouldDisableView(false);
        mButtonSelected = -1;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return mRootView;
    }

    @Override
    protected void onBindView(View view) {
        // Do nothing.
    }

    public View findViewById(int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

       if(rightButton == null || leftButton == null)
            return false;

       int enabled_color = mContext.getColor(R.color.uninstall_button_enabled_color);
       int disabled_color = mContext.getColor(R.color.uninstall_button_diabled_color);
       int selected_color = mContext.getColor(R.color.uninstall_button_selected_color);

       if((keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) && rightButton.isEnabled()){
		    if(leftButton.isEnabled()) {
               leftButton.setBackgroundColor(enabled_color);
            } else {
               leftButton.setBackgroundColor(disabled_color);
            }
            rightButton.setBackgroundColor(selected_color);
            mButtonSelected = 0;
       } else if((keyCode == KeyEvent.KEYCODE_DPAD_LEFT) && leftButton.isEnabled()){
            leftButton.setBackgroundColor(selected_color);
            if(rightButton.isEnabled()){
               rightButton.setBackgroundColor(enabled_color);
		    } else {
               rightButton.setBackgroundColor(disabled_color);
		    }
            mButtonSelected = 1;
       } else if((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) && (event.getAction()==1) && (mButtonSelected != -1)) {
            Intent intent = new Intent(InstalledAppDetails.ACTION_BUTTON);
            if(leftButton.isEnabled() && mButtonSelected == 1){
               intent.putExtra("isleft", mButtonSelected);
               mContext.sendBroadcast(intent);
            } else if(rightButton.isEnabled() && mButtonSelected ==0){
               intent.putExtra("isleft", mButtonSelected);
               mContext.sendBroadcast(intent);
            }
       } else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if(leftButton.isEnabled()) {
               leftButton.setBackgroundColor(enabled_color);
            } else {
               leftButton.setBackgroundColor(disabled_color);
            }

            if(rightButton.isEnabled()){
               rightButton.setBackgroundColor(enabled_color);
            } else {
               rightButton.setBackgroundColor(disabled_color);
            }
            mButtonSelected = -1;
       }
       return false;
    }
}
