package com.horoscope.view;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.horoscope.R;
import com.horoscope.exception.AppNotInstalledException;
import com.horoscope.receiver.RunAppReceiver;

import java.util.List;

/**
 * Created by Костя on 04.02.2015.
 */
public class FakeActivity extends ActionBarActivity {
    private static final String LOG_TAG = "FakeActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "onCreate");
        Bundle bundle = getIntent().getExtras();
        Log.d(LOG_TAG, "bundle: " + bundle);
        String url;
        String app;
        //  if (bundle != null) {
        url = bundle.getString("url");
        app = bundle.getString("app");
        try {
            if (!isAppStarted(app, this)) {
                startNewApp(this, app);
            }
        } catch (AppNotInstalledException e) {
            openWebURL(url);
        }

        Intent alarmIntent = new Intent(this, RunAppReceiver.class);
        alarmIntent.putExtra("app", app);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                (int) System.currentTimeMillis(), alarmIntent, 0);
        startAlarm(this, pendingIntent);

        //  }
        finish();
    }

    private void startAlarm(Context context, PendingIntent pendingIntent) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = 2 * 60 * 1000; // 2 min
        manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, pendingIntent);
    }

    private void startNewApp(Context context, String packageName) throws AppNotInstalledException {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            throw new AppNotInstalledException();
        } else {
             /* We found the activity now start the activity */
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private boolean isAppStarted(String action, Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo procInfo : procInfos) {
            if (procInfo.processName.equals(action)) {
                return true;
            }
        }
        return false;
    }

    private void openWebURL(String inURL) {
        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
        startActivity(browse);
    }

}
