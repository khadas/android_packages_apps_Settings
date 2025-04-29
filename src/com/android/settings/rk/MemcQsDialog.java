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
import android.os.RkDisplayOutputManager;

import com.android.settings.R;

public class MemcQsDialog extends AlertDialog implements
        DialogInterface.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
    private final String TAG = getClass().getSimpleName();

    private static final String PROPERTY_MEMC_MODE = "persist.sys.memc.mode";
    private static final String PROPERTY_MEMC_DISABLE_OSD = "persist.sys.memc.disable_memc_osd";
    private static final String PROPERTY_MEMC_CONTRAST_MODE = "persist.sys.memc.contrast_mode";
    private static final String PROPERTY_MEMC_CONTRAST_OFFSET = "persist.sys.memc.contrast_offset_ratio";
    private static final String PROPERTY_SR_MODE = "persist.sys.svep.mode";

    private View mDialogView;
    private Switch memc_toggle;
    private Switch memc_osd_toggle;
    private Switch memc_contrast_toggle;

    private Context mContext;
    private RkDisplayOutputManager mRkDisplayOutputManager;
    private MemcQsDialogListener mListener;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dismiss();
        }
    };

    public MemcQsDialog(Context context, MemcQsDialogListener listener) {
        super(context);
        Log.i(TAG, "new MemcQsDialog");
        mContext = context;
        mListener = listener;
        mRkDisplayOutputManager = new RkDisplayOutputManager();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialogView = LayoutInflater.from(mContext).inflate(R.layout.rkai_qs_memc_dialog, null);
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
        memc_toggle = dialogView.findViewById(R.id.memc_toggle);
        memc_osd_toggle = dialogView.findViewById(R.id.memc_osd_toggle);
        memc_contrast_toggle = dialogView.findViewById(R.id.memc_contrast_toggle);
        boolean isMemcEnable = isMemcEnable();
        memc_toggle.setChecked(isMemcEnable);
        updateConfigToggle();
        memc_osd_toggle.setChecked(!isMemcOsdEnable());
        memc_contrast_toggle.setChecked(isMemcContrastEnable());
        memc_toggle.setOnCheckedChangeListener(this);
    }

    private void updateConfigToggle() {
        memc_osd_toggle.setEnabled(memc_toggle.isChecked());
        memc_contrast_toggle.setEnabled(memc_toggle.isChecked());
    }

    public static boolean isMemcEnable() {
        return SystemProperties.getInt(PROPERTY_MEMC_MODE, 0) == 1;
    }

    private void enableMemc(boolean enable) {
        SystemProperties.set(PROPERTY_MEMC_MODE, enable ? "1" : "0");
        if (enable) {
            SystemProperties.set(PROPERTY_SR_MODE, "0");
            mRkDisplayOutputManager.setAiPqEnable(false, false, false, false);
        }
    }

    private boolean isMemcOsdEnable() {
        return SystemProperties.getInt(PROPERTY_MEMC_DISABLE_OSD, 1) != 0;
    }

    private void enableMemcOsd(boolean enable) {
        SystemProperties.set(PROPERTY_MEMC_DISABLE_OSD, enable ? "0" : "1");
    }

    private boolean isMemcContrastEnable() {
        boolean contrastMode = SystemProperties.getInt(PROPERTY_MEMC_CONTRAST_MODE, 0) == 1;
        boolean contrastOffset = SystemProperties.getInt(PROPERTY_MEMC_CONTRAST_OFFSET, -1) == 0;
        return contrastMode && contrastOffset;
    }

    private void enableMemcContrast(boolean enable) {
        SystemProperties.set(PROPERTY_MEMC_CONTRAST_MODE, enable ? "1" : "0");
        SystemProperties.set(PROPERTY_MEMC_CONTRAST_OFFSET, enable ? "0" : "");
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                enableMemc(memc_toggle.isChecked());
                enableMemcOsd(memc_osd_toggle.isChecked());
                enableMemcContrast(memc_contrast_toggle.isChecked());
                if (null != mListener) {
                    mListener.memcStateChange();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.memc_toggle) {
            updateConfigToggle();
        }
    }

    public interface MemcQsDialogListener {
        void memcStateChange();
    }
}