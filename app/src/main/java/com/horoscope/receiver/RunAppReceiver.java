package com.horoscope.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.horoscope.exception.AppNotInstalledException;

import java.util.List;

/**
 * Created by Костя on 04.03.2015.
 */
public class RunAppReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "RunAppReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        String app = intent.getStringExtra("app");
        Log.d(LOG_TAG, "onReceive. app = " + app);
        if (!isAppStarted(app, context)) {
            try {
                Log.i(LOG_TAG, "startNewApp " + app);
                startNewApp(context, app);
            } catch (AppNotInstalledException e) {
                Log.w(LOG_TAG, "App not installed. Do nothing. App: " + app, e);
            }
        }
    }

    private void startNewApp(Context context, String packageName) throws AppNotInstalledException {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            throw new AppNotInstalledException();
        } else {
             /* We found the activity now start the activity */
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private boolean isAppStarted(String action, Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo procInfo : procInfos) {
            if (procInfo.processName.equals(action)) {
                return true;
            }
        }
        return false;
    }

}
