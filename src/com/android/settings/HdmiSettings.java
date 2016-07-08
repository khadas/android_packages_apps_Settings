package com.android.settings;

import com.android.internal.logging.MetricsLogger;
import android.util.Log;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

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
import java.util.ArrayList;
import com.droidlogic.app.HdrManager;
import javax.xml.transform.Result;

import com.android.settings.widget.SwitchBar;

import android.os.AsyncTask;
import android.os.SystemProperties;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;
//import static android.provider.Settings.System.HDMI_LCD_TIMEOUT;
import android.content.ContentResolver;
import android.os.Handler;
import android.database.ContentObserver;
import android.os.RemoteException;
//import android.os.DisplayOutputManager;
import android.os.Message;
public class HdmiSettings extends SettingsPreferenceFragment
		implements OnPreferenceChangeListener {
	private static final String TAG = "HdmiControllerActivity";
	private static final String KEY_HDMI_RESOLUTION = "hdmi_resolution";
	private static final String KEY_HDMI_SCALE="hdmi_screen_zoom";
	private static final String KEY_HDMI_HDR = "hdmi_hdr";
	private static final String KEY_HDMI_DEEPCOLOR = "hdmi_deepcolor_mode";

	private ListPreference mHdmiResolution;
	private ListPreference mHdmiHdr;
	private Preference mHdmiScale;
	private SwitchPreference mHdmiDeeepColorMode;

    private String[] mTitleList;
	private String[] mValueList;

	private Context mContext;
	private IntentFilter mIntentFilter;
    private HdmiOutputManager mOutputManager;
    private HdrManager mHdrManager;
	private static final int MSG_FRESH_UI = 0;

	private String autoBestTitle="Auto switch to best resolution";
	private String autoBestValue="auto";


	private static final String HDR_AUTO="auto";
	private static final String HDR_ON="on";
	private static final String HDR_OFF="off";

	@Override
	protected int getMetricsCategory() {
	    return MetricsLogger.DISPLAY;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity();
		addPreferencesFromResource(R.xml.hdmi_settings);
        mHdrManager = new HdrManager(mContext);
		mOutputManager = new HdmiOutputManager(mContext);
        updateMainScreen();
		initHdmiResolutionList();
		mHdmiHdr = (ListPreference) findPreference(KEY_HDMI_HDR);
		mHdmiHdr.setOnPreferenceChangeListener(this);
		mHdmiResolution = (ListPreference) findPreference(KEY_HDMI_RESOLUTION);
		mHdmiResolution.setOnPreferenceChangeListener(this);
        mHdmiDeeepColorMode = (SwitchPreference) findPreference(KEY_HDMI_DEEPCOLOR);
        mHdmiDeeepColorMode.setOnPreferenceChangeListener(this);
        if(mOutputManager.isDeepColor()){
          mHdmiDeeepColorMode.setChecked(true);
		}else{
          mHdmiDeeepColorMode.setChecked(false);
		}

		int mode = mHdrManager.getHdrMode();
        mHdmiHdr.setValue(String.valueOf(mode));
		mHdmiResolution.setEntries(mTitleList);
		mHdmiResolution.setEntryValues(mValueList);
         if (mOutputManager.getUiMode().equals(mOutputManager.HDMI_MODE)) {
             if (mOutputManager.isBestOutputmode()) {
                    mHdmiResolution.setValue(mValueList[0]);
			 }else{

				int currentModeIndex = mOutputManager.getCurrentModeIndex();
				for(int i=1;i<mTitleList.length;i++){
					if(i == (currentModeIndex+1))
						mHdmiResolution.setValue(mValueList[i]);
				}

			 }
		 }

		mIntentFilter = new IntentFilter("android.intent.action.HDMI_PLUGGED");
		mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
	}

    private void initHdmiResolutionList(){

        ArrayList<String> outputmodeTitleList = mOutputManager.getOutputmodeTitleList();
		ArrayList<String> outputmodeValueList = mOutputManager.getOutputmodeValueList();
		int titleCount=outputmodeTitleList.size()+1;
		int valueCount=outputmodeValueList.size()+1;
		mTitleList=new String[titleCount];
		mValueList=new String[valueCount];
        for(int i=0;i<titleCount;i++){
			   if(i==0)
                 mTitleList[i]=autoBestTitle;
			   else
                 mTitleList[i]=outputmodeTitleList.get(i-1);
		}
		for(int j=0;j<valueCount;j++){
             if(j==0)
				 mValueList[j]=autoBestValue;
			 else
			     mValueList[j]=outputmodeValueList.get(j-1);
		}
	}

	private void setHdmiResolution(String mode){
      if(mode.equals(autoBestValue))
		  mOutputManager.change2BestMode();
	  else
          mOutputManager.change2NewMode(mode);

	  updateMainScreen();
    }

	private void setHdrMode(String mode){
      mHdrManager.setHdrMode(Integer.valueOf(mode));
	}
	private void updateMainScreen() {
		mOutputManager.updateUiMode();
    }

	private Handler mHandler = new Handler() {
		@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_FRESH_UI:
						updateMainScreen();
						 break;
				}
			}
    };
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		final SettingsActivity activity = (SettingsActivity) getActivity();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	private BroadcastReceiver mIntentReceiver=new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			mHandler.sendEmptyMessageDelayed(MSG_FRESH_UI, 1000);
		}
	};
  @Override
  public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	    mContext.registerReceiver(mIntentReceiver, mIntentFilter);
	}

	public void onPause() {
		super.onPause();
		mContext.unregisterReceiver(mIntentReceiver);
	}

	public void onDestroy() {
		super.onDestroy();

    }
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object objValue) {
		// TODO Auto-generated method stub
		String key = preference.getKey();
		Log.d(TAG, key);
		if (KEY_HDMI_RESOLUTION.equals(key)) {
			setHdmiResolution((String)objValue);
		}

		if(KEY_HDMI_HDR.equals(key)){
            setHdrMode((String)objValue);
		}

		if(KEY_HDMI_DEEPCOLOR.equals(key)){
            mOutputManager.change2DeepColorMode();
		}
		return true;
	}
}
