/*
 * Copyright 2024 Rockchip Electronics S.LSI Co. LTD
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
package com.android.settings.rk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.settings.R;

public class SrQsDialog extends AlertDialog implements
        DialogInterface.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
    private final String TAG = getClass().getSimpleName();

    private static final String PROPERTY_SR_MODE = "persist.sys.svep.mode";
    private final String PROPERTY_SR_DISABLE_OSD = "persist.sys.svep.disable_sr_osd";
    private final String PROPERTY_SR_CONTRAST_MODE = "persist.sys.svep.contrast_mode";
    private final String PROPERTY_SR_CONTRAST_OFFSET = "persist.sys.svep.contrast_offset_ratio";

    private View mDialogView;
    private Switch sr_toggle;
    private Switch sr_osd_toggle;
    private Switch sr_contrast_toggle;

    private Context mContext;
    private SrQsDialogListener mListener;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dismiss();
        }
    };

    public SrQsDialog(Context context, SrQsDialogListener listener) {
        super(context);
        mContext = context;
        mListener = listener;

        Log.i(TAG, "new SrQsDialog");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialogView = LayoutInflater.from(mContext).inflate(R.layout.rkai_qs_sr_dialog, null);
        getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_SUB_PANEL);
        setView(mDialogView);
        setButton(Dialog.BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
        setButton(Dialog.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        initView(mDialogView);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unRegisterReceiver() {
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
        registerReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
        unRegisterReceiver();
    }

    private void initView(View dialogView) {
        sr_toggle = dialogView.findViewById(R.id.sr_toggle);
        sr_osd_toggle = dialogView.findViewById(R.id.sr_osd_toggle);
        sr_contrast_toggle = dialogView.findViewById(R.id.sr_contrast_toggle);

        boolean isSrEnable = isSrEnable();
        sr_toggle.setChecked(isSrEnable);
        updateConfigToggle();
        sr_osd_toggle.setChecked(!isSrOsdEnable());
        sr_contrast_toggle.setChecked(isSrContrastEnable());

        sr_toggle.setOnCheckedChangeListener(this);
    }

    private void updateConfigToggle() {
        sr_osd_toggle.setEnabled(sr_toggle.isChecked());
        sr_contrast_toggle.setEnabled(sr_toggle.isChecked());
    }

    public static boolean isSrEnable() {
        return SystemProperties.getInt(PROPERTY_SR_MODE, 0) == 1;
    }

    private void enableSr(boolean enable) {
        SystemProperties.set(PROPERTY_SR_MODE, enable ? "1" : "0");
    }

    private boolean isSrOsdEnable() {
        return SystemProperties.getInt(PROPERTY_SR_DISABLE_OSD, 1) != 0;
    }

    private void enableSrOsd(boolean enable) {
        SystemProperties.set(PROPERTY_SR_DISABLE_OSD, enable ? "0" : "1");
    }

    private boolean isSrContrastEnable() {
        boolean contrastMode = SystemProperties.getInt(PROPERTY_SR_CONTRAST_MODE, 0) == 1;
        boolean contrastOffset = SystemProperties.getInt(PROPERTY_SR_CONTRAST_OFFSET, -1) == 0;
        return contrastMode && contrastOffset;
    }

    private void enableSrContrast(boolean enable) {
        SystemProperties.set(PROPERTY_SR_CONTRAST_MODE, enable ? "1" : "0");
        SystemProperties.set(PROPERTY_SR_CONTRAST_OFFSET, enable ? "0" : "");
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                enableSr(sr_toggle.isChecked());
                enableSrOsd(sr_osd_toggle.isChecked());
                enableSrContrast(sr_contrast_toggle.isChecked());
                if (null != mListener) {
                    mListener.srStateChange();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.sr_toggle) {
            updateConfigToggle();
        }
    }

    public interface SrQsDialogListener {
        void srStateChange();
    }
}