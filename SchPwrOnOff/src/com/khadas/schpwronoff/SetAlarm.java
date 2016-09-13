package com.khadas.schpwronoff;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TimePicker;
import android.widget.Toast;

import android.util.Log;

/**
 * Manages each alarm
 */
public class SetAlarm extends PreferenceActivity
    implements TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "SetAlarm";
    private Preference mTimePref;
    private RepeatPreference mRepeatPref;
    private MenuItem mTestAlarmItem;

    private int mId;
    private boolean mEnabled;
    private int mHour;
    private int mMinutes;
	private static final int MENU_BACK = android.R.id.home;
    private static final int MENU_REVET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private String mPrevTitle;
    private static final int DIALOG_TIMEPICKER = 1;

    
    /**
     * Set an alarm. Requires an Alarms.ALARM_ID to be passed in as an extra.
     * FIXME: Pass an Alarm object like every other Activity.
     * @param savedInstanceState Bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.schpwr_alarm_prefs);

        PreferenceScreen view = getPreferenceScreen();
        mTimePref = view.findPreference("time");
        mRepeatPref = (RepeatPreference) view.findPreference("setRepeat");

        mId = getIntent().getIntExtra(Alarms.ALARM_ID, 0);
        Log.d(TAG, "onCreate " + "bundle extra is " + mId);

        mPrevTitle = getTitle().toString();
        if (mId == 1) {
            setTitle(R.string.schedule_power_on_set);
        } else {
            setTitle(R.string.schedule_power_off_set);
        }
        Log.d(TAG, "In SetAlarm, alarm id = " + mId);

        // load alarm details from database
        Alarm alarm = Alarms.getAlarm(getContentResolver(), mId);
        if (alarm != null) {
            mEnabled = alarm.mEnabled;
            mHour = alarm.mHour;
            mMinutes = alarm.mMinutes;
            if (mRepeatPref != null) {
                mRepeatPref.setDaysOfWeek(alarm.mDaysOfWeek);
            }
        }
        updateTime();
        //setHasOptionsMenu(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mTimePref) {
            Log.d(TAG, "showDialog(DIALOG_TIMEPICKER)");
            showDialog(DIALOG_TIMEPICKER);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_TIMEPICKER:
        	 Log.d(TAG, "onCreateDialog(DIALOG_TIMEPICKER)");
       	return new TimePickerDialog(this, this, mHour, mMinutes, DateFormat.is24HourFormat(this));

        default:
            return null;    
        }
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_REVET, 0, R.string.revert).setEnabled(true)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(Menu.NONE, MENU_SAVE, 0, R.string.done).setEnabled(true).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_REVET:
            finish();
            return true;
        case MENU_SAVE:
            Log.d(TAG, "option save menu");
            saveAlarm();
            finish();
            return true;
		case MENU_BACK:
			finish();
			return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinutes = minute;
        updateTime();
        // If the time has been changed, enable the alarm.
        mEnabled = true;
    }

    private void updateTime() {
        Log.d(TAG, "updateTime " + mId);
        mTimePref.setSummary(Alarms.formatTime(this, mHour, mMinutes, mRepeatPref.getDaysOfWeek()));
    }

    private void saveAlarm() {
        final String alert = Alarms.ALARM_ALERT_SILENT;
        mEnabled |= mRepeatPref.mIsPressedPositive;
        Alarms.setAlarm(this, mId, mEnabled, mHour, mMinutes, mRepeatPref.getDaysOfWeek(), true, "", alert);

        if (mEnabled) {
            popAlarmSetToast(this.getApplicationContext(), mHour, mMinutes, mRepeatPref.getDaysOfWeek(), mId);
        }
    }

    /**
     * Display a toast that tells the user how long until the alarm goes off. This helps prevent "am/pm" mistakes.
     */
    static void popAlarmSetToast(Context context, int hour, int minute, Alarm.DaysOfWeek daysOfWeek, int mId) {
        String toastText = formatToast(context, hour, minute, daysOfWeek, mId);
        Log.d(TAG, "toast text: " + toastText);
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
    }

    /**
     * format "Alarm set for 2 days 7 hours and 53 minutes from now"
     */
    static String formatToast(Context context, int hour, int minute, Alarm.DaysOfWeek daysOfWeek, int id) {
        long alarm = Alarms.calculateAlarm(hour, minute, daysOfWeek).getTimeInMillis();
        long delta = alarm - System.currentTimeMillis();

        final int millisUnit = 1000;
        final int timeUnit = 60;
        final int dayOfHoursUnit = 24;

        long hours = delta / (millisUnit * timeUnit * timeUnit);
        long minutes = delta / (millisUnit * timeUnit) % timeUnit;
        long days = hours / dayOfHoursUnit;
        hours = hours % dayOfHoursUnit;

        String daySeq = (days == 0) ? "" : (days == 1) ? context.getString(R.string.day) : context.getString(R.string.days,
                Long.toString(days));

        String minSeq = (minutes == 0) ? "" : (minutes == 1) ? context.getString(R.string.minute) : context.getString(
                R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" : (hours == 1) ? context.getString(R.string.hour) : context.getString(
                R.string.hours, Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        final int dispMinutesOffset = 4;
        final int pwrOnOFFStringOffset = 8;

        int index = (dispDays ? 1 : 0) | (dispHour ? 2 : 0) | (dispMinute ? dispMinutesOffset : 0);

        String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        if (id == 2) {
            index += pwrOnOFFStringOffset;
        }
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }
    
    @Override
     public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
       Log.d(TAG, "onConfigurationChanged: " + newConfig.orientation + ",remove timer picker dialog");
       removeDialog(DIALOG_TIMEPICKER);
    }
}
