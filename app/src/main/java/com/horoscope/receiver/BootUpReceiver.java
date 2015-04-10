package com.horoscope.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Костя on 14.01.2015.
 */
public class BootUpReceiver extends BroadcastReceiver {
    private PendingIntent pendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        //todo
        if (!isAlarmWorking(context)) {
            Intent alarmIntent = new Intent(context, ServerDataReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            startAlarm(context);
        }
    }

    public void startAlarm(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = 86400000; // One day in millis
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }

    private boolean isAlarmWorking(Context context) {
        Intent intent = new Intent(context, ServerDataReceiver.class);//the same as up
        intent.setAction(ServerDataReceiver.ACTION_ALARM_RECEIVER);//the same as up
        return PendingIntent.getBroadcast(context, 101, intent, PendingIntent.FLAG_NO_CREATE) != null;//just changed the flag
    }

}
