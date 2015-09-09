/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.turbidity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TurbidityStartActivity extends BaseActivity {

    private boolean mAlarmStarted;
    private Button buttonStartTimer;
    private TextView textStatus;
    private EditText editInterval;
    private EditText editImageCount;

    private int mDelayHour;
    private int mDelayMinute;
    private int mImageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turbidity_start);

        setTitle(R.string.startColiformsTest);

        textStatus = (TextView) findViewById(R.id.textStatus);
        buttonStartTimer = (Button) findViewById(R.id.buttonManageTimer);
        editInterval = (EditText) findViewById(R.id.editInterval);
        editImageCount = (EditText) findViewById(R.id.editImageCount);

        if (isAlarmRunning()) {
            setStartStatus();
        } else {
            setStopStatus();
        }

        buttonStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PendingIntent pendingIntent = getPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                if (mAlarmStarted) {
                    setStopStatus();

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();

                    buttonStartTimer.setEnabled(true);

                } else {

                    setStartStatus();

                    mImageCount = Integer.parseInt(editImageCount.getText().toString());

                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime() + 10000, pendingIntent);
                    } else {
                        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime() + 10000,
                                ((mDelayHour * 60) + mDelayMinute) * 60000, pendingIntent);
                    }
                }
            }
        });

        mDelayHour = PreferencesUtil.getInt(getBaseContext(),
                getString(R.string.cameraRepeatIntervalHourKey), 0);
        mDelayMinute = PreferencesUtil.getInt(getBaseContext(),
                getString(R.string.cameraRepeatIntervalMinuteKey), 1);

        mImageCount = PreferencesUtil.getInt(getBaseContext(),
                getString(R.string.cameraRepeatImageCountKey), 10);

        editInterval.setText(String.format("%02d : %02d", mDelayHour, mDelayMinute));
        editImageCount.setText(String.valueOf(mImageCount));

        final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                editInterval.setText(String.format("%02d : %02d", hour, minute));
                mDelayHour = hour;
                mDelayMinute = minute;
                PreferencesUtil.setInt(getBaseContext(),
                        getString(R.string.cameraRepeatIntervalHourKey), hour);
                PreferencesUtil.setInt(getBaseContext(),
                        getString(R.string.cameraRepeatIntervalMinuteKey), minute);
            }
        };

        final TimePickerDialog timePickerDialog = new TimePickerDialog(this, time,
                mDelayHour, mDelayMinute, true);

        editInterval.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    timePickerDialog.show();
                }
            }
        });

        editInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerDialog.show();
            }
        });
    }

    private void setStartStatus() {
        textStatus.setText("Status: Active");
        editInterval.setEnabled(false);
        editImageCount.setEnabled(false);
        buttonStartTimer.setText(R.string.stop);
        mAlarmStarted = true;
    }

    private void setStopStatus() {
        textStatus.setText("Status: Inactive");
        editInterval.setEnabled(true);
        editImageCount.setEnabled(true);
        buttonStartTimer.setText(R.string.start);
        mAlarmStarted = false;
    }

    private PendingIntent getPendingIntent(int flag) {
        Intent intent = new Intent(this, TurbidityStartReceiver.class);
        intent.setAction(TurbidityConfig.ACTION_ALARM_RECEIVER);
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(new Date());
        intent.putExtra("startDateTime", date);
        return PendingIntent.getBroadcast(this, TurbidityConfig.INTENT_REQUEST_CODE, intent, flag);
    }

    private boolean isAlarmRunning() {
        return getPendingIntent(PendingIntent.FLAG_NO_CREATE) != null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        Resources res = getResources();
        Configuration conf = res.getConfiguration();

        //set the title to the test contaminant name
        ((TextView) findViewById(R.id.textTitle)).setText(testInfo.getName(conf.locale.getLanguage()));
    }
}