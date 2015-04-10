package com.horoscope.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.horoscope.view.FakeActivity;

import java.util.List;

/**
 * Created by Костя on 13.02.2015.
 */
public class RunUrlsOrAppsReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "RunUrlsOrAppsReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "RunUrlsOrAppsReceiver. onReceive");
        List<String> urlList = intent.getStringArrayListExtra("urlList");
        List<String> appsList = intent.getStringArrayListExtra("appsList");
        int position = getPositionFromPreferences(context);
        // set position to beginning, so we rotating links/apps
        if (position > urlList.size() - 1) {
            savePositionToPreferences(context, 0);
            position = 0;
        }
        Log.i(LOG_TAG, "test url" + urlList + "position  " + position);
        Bundle bundle = new Bundle();
        bundle.putString("url", urlList.get(position));
        bundle.putString("app", appsList.get(position));
        Intent myIntent = new Intent(context, FakeActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.putExtras(bundle);
        context.startActivity(myIntent);
        position++;
        savePositionToPreferences(context, position);
        Log.d(LOG_TAG, "urlList.size() = " + urlList.size() + "position = " + position);
    }


    private void savePositionToPreferences(Context context, int position) {
        SharedPreferences prefs = context.getSharedPreferences("com.mycompany.myAppName", Context.MODE_PRIVATE);
        prefs.edit().putInt("position", position).commit();
    }

    private int getPositionFromPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("com.mycompany.myAppName", Context.MODE_PRIVATE);
        return preferences.getInt("position", 0);
    }

}
