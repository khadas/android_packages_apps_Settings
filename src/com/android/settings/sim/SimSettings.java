/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.sim;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.SearchIndexableResource;
import android.telephony.PhoneStateListener;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.R;
import android.os.SystemProperties;
import com.android.internal.telephony.TelephonyProperties;

import java.util.ArrayList;
import java.util.List;

public class SimSettings extends RestrictedSettingsFragment implements Indexable {
    private static final String TAG = "SimSettings";
    private static final boolean DBG = false;

    private static final String DISALLOW_CONFIG_SIM = "no_config_sim";
    private static final String SIM_CARD_CATEGORY = "sim_cards";
    private static final String KEY_CELLULAR_DATA = "sim_cellular_data";
    private static final String KEY_CALLS = "sim_calls";
    private static final String KEY_SMS = "sim_sms";
    public static final String EXTRA_SLOT_ID = "slot_id";

    /**
     * By UX design we use only one Subscription Information(SubInfo) record per SIM slot.
     * mAvalableSubInfos is the list of SubInfos we present to the user.
     * mSubInfoList is the list of all SubInfos.
     * mSelectableSubInfos is the list of SubInfos that a user can select for data, calls, and SMS.
     */
    private List<SubscriptionInfo> mAvailableSubInfos = null;
    private List<SubscriptionInfo> mSubInfoList = null;
    private List<SubscriptionInfo> mSelectableSubInfos = null;
    private PreferenceScreen mSimCards = null;
    private SubscriptionManager mSubscriptionManager;
    private int mNumSlots;
    private Context mContext;

    public SimSettings() {
        super(DISALLOW_CONFIG_SIM);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.SIM;
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        mContext = getActivity();

        mSubscriptionManager = SubscriptionManager.from(getActivity());
        final TelephonyManager tm =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        addPreferencesFromResource(R.xml.sim_settings);

        mNumSlots = tm.getSimCount();
        mSimCards = (PreferenceScreen)findPreference(SIM_CARD_CATEGORY);
        mAvailableSubInfos = new ArrayList<SubscriptionInfo>(mNumSlots);
        mSelectableSubInfos = new ArrayList<SubscriptionInfo>();
        SimSelectNotification.cancelNotification(getActivity());
    }

    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener
            = new SubscriptionManager.OnSubscriptionsChangedListener() {
        @Override
        public void onSubscriptionsChanged() {
            if (DBG) log("onSubscriptionsChanged:");
            updateSubscriptions();
        }
    };

    private void updateSubscriptions() {
        mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
        for (int i = 0; i < mNumSlots; ++i) {
            Preference pref = mSimCards.findPreference("sim" + i);
            if (pref instanceof SimPreference) {
                mSimCards.removePreference(pref);
            }
        }
        mAvailableSubInfos.clear();
        mSelectableSubInfos.clear();

        for (int i = 0; i < mNumSlots; ++i) {
            final SubscriptionInfo sir = mSubscriptionManager
                    .getActiveSubscriptionInfoForSimSlotIndex(i);
            SimPreference simPreference = new SimPreference(mContext, sir, i);
            simPreference.setOrder(i-mNumSlots);
            mSimCards.addPreference(simPreference);
            mAvailableSubInfos.add(sir);
            if (sir != null) {
                mSelectableSubInfos.add(sir);
            }
        }
        updateAllOptions();
    }

    private void updateAllOptions() {
        updateSimSlotValues();
        updateActivitesCategory();
    }

    private void updateSimSlotValues() {
        final int prefSize = mSimCards.getPreferenceCount();
        for (int i = 0; i < prefSize; ++i) {
            Preference pref = mSimCards.getPreference(i);
            if (pref instanceof SimPreference) {
                ((SimPreference)pref).update();
            }
        }
    }

    private void updateActivitesCategory() {
        updateCellularDataValues();
        updateCallValues();
        updateSmsValues();
    }

    private void updateSmsValues() {
        final Preference simPref = findPreference(KEY_SMS);
        final SubscriptionInfo sir = mSubscriptionManager.getDefaultSmsSubscriptionInfo();
        simPref.setTitle(R.string.sms_messages_title);
        if (DBG) log("[updateSmsValues] mSubInfoList=" + mSubInfoList);

        if (sir != null) {
            simPref.setSummary(sir.getDisplayName());
        } else if (sir == null) {
            simPref.setSummary(R.string.sim_selection_required_pref);
        }
        simPref.setEnabled(mSelectableSubInfos.size() >= 1);
    }

    private void updateCellularDataValues() {
        final Preference simPref = findPreference(KEY_CELLULAR_DATA);
        final SubscriptionInfo sir = mSubscriptionManager.getDefaultDataSubscriptionInfo();
        simPref.setTitle(R.string.cellular_data_title);
        if (DBG) log("[updateCellularDataValues] mSubInfoList=" + mSubInfoList);

        if (sir != null) {
            simPref.setSummary(sir.getDisplayName());
        } else if (sir == null) {
            simPref.setSummary(R.string.sim_selection_required_pref);
        }
        simPref.setEnabled(mSelectableSubInfos.size() >= 1);
    }

    private void updateCallValues() {
        final Preference simPref = findPreference(KEY_CALLS);
        final TelecomManager telecomManager = TelecomManager.from(mContext);
        final PhoneAccountHandle phoneAccount =
            telecomManager.getUserSelectedOutgoingPhoneAccount();
        final List<PhoneAccountHandle> allPhoneAccounts =
            telecomManager.getCallCapablePhoneAccounts();

        simPref.setTitle(R.string.calls_title);
        simPref.setSummary(phoneAccount == null
                ? mContext.getResources().getString(R.string.sim_calls_ask_first_prefs_title)
                : (String)telecomManager.getPhoneAccount(phoneAccount).getLabel());
        simPref.setEnabled(allPhoneAccounts.size() > 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
        final TelephonyManager tm =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        updateSubscriptions();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSubscriptionManager.removeOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        // Disable Sim selection for Data when voice call is going on as changing the default data
        // sim causes a modem reset currently and call gets disconnected
        // ToDo : Add subtext on disabled preference to let user know that default data sim cannot
        // be changed while call is going on
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (DBG) log("PhoneStateListener.onCallStateChanged: state=" + state);
             final Preference pref = findPreference(KEY_CELLULAR_DATA);
            if (pref != null) {
                final boolean ecbMode = SystemProperties.getBoolean(
                        TelephonyProperties.PROPERTY_INECM_MODE, false);
                pref.setEnabled((state == TelephonyManager.CALL_STATE_IDLE) && !ecbMode);
            }
        }
    };

    @Override
    public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            final Preference preference) {
        final Context context = mContext;
        Intent intent = new Intent(context, SimDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (preference instanceof SimPreference) {
            Intent newIntent = new Intent(context, SimPreferenceDialog.class);
            newIntent.putExtra(EXTRA_SLOT_ID, ((SimPreference)preference).getSlotId());
            startActivity(newIntent);
        } else if (findPreference(KEY_CELLULAR_DATA) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.DATA_PICK);
            context.startActivity(intent);
        } else if (findPreference(KEY_CALLS) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.CALLS_PICK);
            context.startActivity(intent);
        } else if (findPreference(KEY_SMS) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.SMS_PICK);
            context.startActivity(intent);
        }

        return true;
    }

    private class SimPreference extends Preference {
        private SubscriptionInfo mSubInfoRecord;
        private int mSlotId;
        Context mContext;
        private int[] mTintArr;
        private String[] mColorStrings;
        private int mTintSelectorPos;

        public SimPreference(Context context, SubscriptionInfo subInfoRecord, int slotId) {
            super(context);

            mContext = context;
            mSubInfoRecord = subInfoRecord;
            mSlotId = slotId;
            setKey("sim" + mSlotId);
            update();
        }

        public void update() {
            final Resources res = mContext.getResources();

            setTitle(String.format(mContext.getResources()
                    .getString(R.string.sim_editor_title), (mSlotId + 1)));
            if (mSubInfoRecord != null) {
                if (TextUtils.isEmpty(getPhoneNumber(mSubInfoRecord))) {
                    setSummary(mSubInfoRecord.getDisplayName());
                } else {
                    setSummary(mSubInfoRecord.getDisplayName() + " - " +
                            getPhoneNumber(mSubInfoRecord));
                    setEnabled(true);
                }
                setIcon(new BitmapDrawable(res, (mSubInfoRecord.createIconBitmap(mContext))));
            } else {
                setSummary(R.string.sim_slot_empty);
                setFragment(null);
                setEnabled(false);
            }
        }

        public SubscriptionInfo getSubInfoRecord() {
            return mSubInfoRecord;
        }

        public void createEditDialog(SimPreference simPref) {
            final Resources res = getResources();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            final View dialogLayout = getActivity().getLayoutInflater().inflate(
                    R.layout.multi_sim_dialog, null);
            builder.setView(dialogLayout);

            EditText nameText = (EditText)dialogLayout.findViewById(R.id.sim_name);
            nameText.setText(mSubInfoRecord.getDisplayName());

            final Spinner tintSpinner = (Spinner) dialogLayout.findViewById(R.id.spinner);
            SelectColorAdapter adapter = new SelectColorAdapter(getContext(),
                     R.layout.settings_color_picker_item, mColorStrings);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            tintSpinner.setAdapter(adapter);

            for (int i = 0; i < mTintArr.length; i++) {
                if (mTintArr[i] == mSubInfoRecord.getIconTint()) {
                    tintSpinner.setSelection(i);
                    mTintSelectorPos = i;
                    break;
                }
            }

            tintSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                    int pos, long id){
                    tintSpinner.setSelection(pos);
                    mTintSelectorPos = pos;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            TextView numberView = (TextView)dialogLayout.findViewById(R.id.number);
            final String rawNumber = getPhoneNumber(mSubInfoRecord);
            if (TextUtils.isEmpty(rawNumber)) {
                numberView.setText(res.getString(com.android.internal.R.string.unknownName));
            } else {
                numberView.setText(PhoneNumberUtils.formatNumber(rawNumber));
            }

            final TelephonyManager tm =
                        (TelephonyManager) getActivity().getSystemService(
                        Context.TELEPHONY_SERVICE);
            String simCarrierName = tm.getSimOperatorNameForSubscription(mSubInfoRecord
                        .getSubscriptionId());
            TextView carrierView = (TextView)dialogLayout.findViewById(R.id.carrier);
            carrierView.setText(!TextUtils.isEmpty(simCarrierName) ? simCarrierName :
                    getContext().getString(com.android.internal.R.string.unknownName));

            builder.setTitle(String.format(res.getString(R.string.sim_editor_title),
                    (mSubInfoRecord.getSimSlotIndex() + 1)));

            builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    final EditText nameText = (EditText)dialogLayout.findViewById(R.id.sim_name);

                    String displayName = nameText.getText().toString();
                    int subId = mSubInfoRecord.getSubscriptionId();
                    mSubInfoRecord.setDisplayName(displayName);
                    mSubscriptionManager.setDisplayName(displayName, subId,
                            SubscriptionManager.NAME_SOURCE_USER_INPUT);
                    /*
                    Utils.findRecordBySubId(getActivity(), subId).setDisplayName(displayName);

                    final int tintSelected = tintSpinner.getSelectedItemPosition();
                    int subscriptionId = mSubInfoRecord.getSubscriptionId();
                    int tint = mTintArr[tintSelected];
                    mSubInfoRecord.setIconTint(tint);
                    mSubscriptionManager.setIconTint(tint, subscriptionId);
                    Utils.findRecordBySubId(getActivity(), subscriptionId).setIconTint(tint);
                    */
                    updateAllOptions();
                    update();
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        }

        private class SelectColorAdapter extends ArrayAdapter<CharSequence> {
            private Context mContext;
            private int mResId;

            public SelectColorAdapter(
                Context context, int resource, String[] arr) {
                super(context, resource, arr);
                mContext = context;
                mResId = resource;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View rowView;
                final ViewHolder holder;
                Resources res = getResources();
                int iconSize = res.getDimensionPixelSize(R.dimen.color_swatch_size);
                int strokeWidth = res.getDimensionPixelSize(R.dimen.color_swatch_stroke_width);

                if (convertView == null) {
                    // Cache views for faster scrolling
                    rowView = inflater.inflate(mResId, null);
                    holder = new ViewHolder();
                    ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
                    drawable.setIntrinsicHeight(iconSize);
                    drawable.setIntrinsicWidth(iconSize);
                    drawable.getPaint().setStrokeWidth(strokeWidth);
                    holder.label = (TextView) rowView.findViewById(R.id.color_text);
                    holder.icon = (ImageView) rowView.findViewById(R.id.color_icon);
                    holder.swatch = drawable;
                    rowView.setTag(holder);
                } else {
                    rowView = convertView;
                    holder = (ViewHolder) rowView.getTag();
                }

                holder.label.setText(getItem(position));
                holder.swatch.getPaint().setColor(mTintArr[position]);
                holder.swatch.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setImageDrawable(holder.swatch);
                return rowView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View rowView = getView(position, convertView, parent);
                final ViewHolder holder = (ViewHolder) rowView.getTag();

                if (mTintSelectorPos == position) {
                    holder.swatch.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
                } else {
                    holder.swatch.getPaint().setStyle(Paint.Style.STROKE);
                }
                holder.icon.setVisibility(View.VISIBLE);
                return rowView;
            }

            private class ViewHolder {
                TextView label;
                ImageView icon;
                ShapeDrawable swatch;
            }
        }

        private int getSlotId() {
            return mSlotId;
        }
    }

    // Returns the line1Number. Line1number should always be read from TelephonyManager since it can
    // be overridden for display purposes.
    private String getPhoneNumber(SubscriptionInfo info) {
        final TelephonyManager tm =
            (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1NumberForSubscriber(info.getSubscriptionId());
    }

    private void log(String s) {
        Log.d(TAG, s);
    }

    /**
     * For search
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    if (Utils.showSimCardTile(context)) {
                        SearchIndexableResource sir = new SearchIndexableResource(context);
                        sir.xmlResId = R.xml.sim_settings;
                        result.add(sir);
                    }

                    return result;
                }
            };
}
