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

import android.content.Context;
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
import java.lang.reflect.Method;

public class PqCfgDialog extends BaseCfgDialog implements
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = PqCfgDialog.class.getSimpleName();
    private static final String PROPERTY_MEMC_MODE = "persist.sys.memc.mode";
    private static final String PROPERTY_SR_MODE = "persist.sys.svep.mode";

    private View mDialogView;
    private Switch pq_toggle;
    private Switch aipq_sd_toggle;
    private Switch aipq_sr_toggle;
    private Switch aipq_memc_toggle;
    private Switch aipq_dc_toggle;

    private PqCfgDialogListener mListener;
    private static Object mRkDOM = instanceRkDOM();

    public PqCfgDialog(Context context, PqCfgDialogListener listener) {
        super(context);
        mListener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialogView = LayoutInflater.from(mContext).inflate(R.layout.pq_cfg_dialog, null);
        getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_SUB_PANEL);
        setView(mDialogView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        initView(mDialogView);
    }

    private void initView(View dialogView) {
        pq_toggle = dialogView.findViewById(R.id.pq_toggle);
        aipq_sd_toggle = dialogView.findViewById(R.id.aipq_sd_toggle);
        aipq_sr_toggle = dialogView.findViewById(R.id.aipq_sr_toggle);
        aipq_memc_toggle = dialogView.findViewById(R.id.aipq_memc_toggle);
        aipq_dc_toggle = dialogView.findViewById(R.id.aipq_dc_toggle);
        boolean isPqEnable = getPqEnable();
        pq_toggle.setChecked(isPqEnable);
        boolean[] aiPqEnable = getAiPqEnable();
        if (null == aiPqEnable) {
            aipq_sd_toggle.setEnabled(false);
            aipq_sr_toggle.setEnabled(false);
            aipq_memc_toggle.setEnabled(false);
            aipq_dc_toggle.setEnabled(false);
        } else {
            aipq_sd_toggle.setChecked(aiPqEnable[0]);
            aipq_sr_toggle.setChecked(aiPqEnable[1]);
            aipq_memc_toggle.setChecked(aiPqEnable[2]);
            aipq_dc_toggle.setChecked(aiPqEnable[3]);
        }
        pq_toggle.setOnCheckedChangeListener(this);
        aipq_sd_toggle.setOnCheckedChangeListener(this);
    }

    public static boolean getPqEnable() {
        boolean ret;
        try {
            Method method = mRkDOM.getClass().getDeclaredMethod(
                "getPqEnable");
            ret = (boolean) method.invoke(mRkDOM);
        } catch (Exception e) {
            ret = false;
            e.printStackTrace();
        }
        Log.i(TAG, "getPqEnable , ret=" + ret);
        return ret;
    }

    public static int setPqEnable(boolean enable) {
        int ret;
        try {
            Method method = mRkDOM.getClass().getDeclaredMethod(
                "setPqEnable", boolean.class);
            ret = (int) method.invoke(mRkDOM, enable);
        } catch (Exception e) {
            ret = -1;
            e.printStackTrace();
        }
        Log.i(TAG, "setPqEnable " + enable + ", ret=" + ret);
        return ret;
    }

    private static Object instanceRkDOM() {
        Object rkDom = null;
        try {
            rkDom = Class.forName("android.os.RkDisplayOutputManager").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rkDom;
    }

    public int setAiPqEnable(boolean aisd, boolean aisr, boolean aimemc, boolean aidc) {
        int ret;
        try {
            Method method = mRkDOM.getClass().getDeclaredMethod("setAiPqEnable",
                boolean.class, boolean.class, boolean.class, boolean.class);
            ret = (int) method.invoke(mRkDOM, aisd, aisr, aimemc, aidc);
        } catch (Exception e) {
            ret = -1;
            e.printStackTrace();
        }
        if (aisd || aisr || aimemc || aidc) {
            SystemProperties.set(PROPERTY_SR_MODE, "0");
            SystemProperties.set(PROPERTY_MEMC_MODE, "0");
        }
        Log.i(TAG, "setAiPqEnable "
            + "(aisd=" + aisd + ", aisr=" + aisr + ", aimemc=" + aimemc + ", aidc=" + aidc + ")"
            + " ,ret=" + ret);
        return ret;
    }

    public static boolean[] getAiPqEnable() {
        boolean[] ret;
        try {
            Method method = mRkDOM.getClass().getDeclaredMethod("getAiPqEnable");
            ret = (boolean[]) method.invoke(mRkDOM);
        } catch (Exception e) {
            ret = null;
            e.printStackTrace();
        }
        if (null == ret) {
            Log.e(TAG, "getAiPqEnable , ret is null");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (boolean temp : ret) {
                sb.append(temp + ", ");
            }
            sb.append(")");
            Log.i(TAG, "getAiPqEnable , ret=" + sb);
            if (ret.length != 4) {
                Log.e(TAG, "getAiPqEnable force return null, length err");
                ret = null;
            }
        }
        return ret;
    }

    @Override
    void buttonPosClick() {
        PqCfgDialog.setPqEnable(pq_toggle.isChecked());
        setAiPqEnable(aipq_sd_toggle.isEnabled() && aipq_sd_toggle.isChecked(),
            aipq_sr_toggle.isChecked(),
            aipq_memc_toggle.isChecked(), aipq_dc_toggle.isChecked());
        if (null != mListener) {
            mListener.pqStateChange();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.pq_toggle) {
            if (!pq_toggle.isChecked()) {
                aipq_sd_toggle.setChecked(false);
            }
        } else if (id == R.id.aipq_sd_toggle) {
            if (aipq_sd_toggle.isChecked()) {
                pq_toggle.setChecked(true);
            }
        }
    }

    public interface PqCfgDialogListener {
        void pqStateChange();
    }

}