package com.horoscope.view;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.horoscope.R;
import com.horoscope.receiver.ServerDataReceiver;
import com.horoscope.sync.adapter.SyncUtils;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.horoscope.receiver.ServerDataReceiver.ACTION_ALARM_RECEIVER;
import static com.horoscope.receiver.ServerDataReceiver.BASE_URL;
import static com.horoscope.receiver.ServerDataReceiver.TIMEOUT_HEAD_TXT;
import static com.horoscope.receiver.ServerDataReceiver.getImei;
import static com.horoscope.receiver.ServerDataReceiver.isInternetAvailable;
import static com.horoscope.receiver.ServerDataReceiver.readFileFromServer;
import static com.horoscope.util.HoroscopeSigns.AQUARIUS;
import static com.horoscope.util.HoroscopeSigns.ARIES;
import static com.horoscope.util.HoroscopeSigns.CANCER;
import static com.horoscope.util.HoroscopeSigns.CAPRICORN;
import static com.horoscope.util.HoroscopeSigns.GEMINI;
import static com.horoscope.util.HoroscopeSigns.LEO;
import static com.horoscope.util.HoroscopeSigns.LIBRA;
import static com.horoscope.util.HoroscopeSigns.PISCES;
import static com.horoscope.util.HoroscopeSigns.SAGITTARIUS;
import static com.horoscope.util.HoroscopeSigns.SCORPIO;
import static com.horoscope.util.HoroscopeSigns.TAURUS;
import static com.horoscope.util.HoroscopeSigns.VIRGO;
import static com.horoscope.view.ForecastActivity.ZODIAC_SIGN;
//test

public class AllSignsActivity extends ActionBarActivity {
    public static final int INTERVAL = 86400000; // one day in millis
    private static final String LOG_TAG = "AllSignsActivity";

    ListView list;
    String[] web = {
            "Овен. 21 марта — 19 апреля",
            "Телец. 20 апреля — 20 мая",
            "Близнецы. 21 мая — 20 июня",
            "Рак. 21 июня — 22 июля",
            "Лев. 23 июля — 22 августа",
            "Дева. 23 августа — 22 сентября",
            "Весы. 23 сентября — 22 октября",
            "Скорпион. 23 октября — 21 ноября",
            "Стрелец. 22 ноября — 21 декабря",
            "Козерог. 22 декабря — 19 января",
            "Водолей. 20 января — 18 февраля",
            "Рыбы. 19 февраля — 20 марта"
    };
    Integer[] imageId = {
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
            R.drawable.ic_action_refresh,
    };
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Picasso.with(this).setIndicatorsEnabled(true);
        SyncUtils.CreateSyncAccount(this);
        setTitleWithDate();
        setCustomListAdapter();
        Intent alarmIntent = new Intent(this, ServerDataReceiver.class);
        alarmIntent.setAction(ACTION_ALARM_RECEIVER);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        new GetIntervalTask().execute();

    }

    private void setCustomListAdapter() {
        CustomList adapter = new
                CustomList(AllSignsActivity.this, web, imageId);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Bundle bundle = new Bundle();
                switch (position) {
                    case 0:
                        SyncUtils.TriggerRefresh(ARIES);
                        bundle.putString(ZODIAC_SIGN, ARIES);
                        goToForecastActivity(bundle);
                        break;
                    case 1:
                        SyncUtils.TriggerRefresh(TAURUS);
                        bundle.putString(ZODIAC_SIGN, TAURUS);
                        goToForecastActivity(bundle);
                        break;
                    case 2:
                        SyncUtils.TriggerRefresh(GEMINI);
                        bundle.putString(ZODIAC_SIGN, GEMINI);
                        goToForecastActivity(bundle);
                        break;
                    case 3:
                        SyncUtils.TriggerRefresh(CANCER);
                        bundle.putString(ZODIAC_SIGN, CANCER);
                        goToForecastActivity(bundle);
                        break;
                    case 4:
                        SyncUtils.TriggerRefresh(LEO);
                        bundle.putString(ZODIAC_SIGN, LEO);
                        goToForecastActivity(bundle);
                        break;
                    case 5:
                        SyncUtils.TriggerRefresh(VIRGO);
                        bundle.putString(ZODIAC_SIGN, VIRGO);
                        goToForecastActivity(bundle);
                        break;
                    case 6:
                        SyncUtils.TriggerRefresh(LIBRA);
                        bundle.putString(ZODIAC_SIGN, LIBRA);
                        goToForecastActivity(bundle);
                        break;
                    case 7:
                        SyncUtils.TriggerRefresh(SCORPIO);
                        bundle.putString(ZODIAC_SIGN, SCORPIO);
                        goToForecastActivity(bundle);
                        break;
                    case 8:
                        SyncUtils.TriggerRefresh(SAGITTARIUS);
                        bundle.putString(ZODIAC_SIGN, SAGITTARIUS);
                        goToForecastActivity(bundle);
                        break;
                    case 9:
                        SyncUtils.TriggerRefresh(CAPRICORN);
                        bundle.putString(ZODIAC_SIGN, CAPRICORN);
                        goToForecastActivity(bundle);
                        break;
                    case 10:
                        SyncUtils.TriggerRefresh(AQUARIUS);
                        bundle.putString(ZODIAC_SIGN, AQUARIUS);
                        goToForecastActivity(bundle);
                        break;
                    case 11:
                        SyncUtils.TriggerRefresh(PISCES);
                        bundle.putString(ZODIAC_SIGN, PISCES);
                        goToForecastActivity(bundle);
                        break;
                    default:
                        Log.wtf(LOG_TAG, "Wrong Position. Position is " + position);
                        break;
                }
            }
        });
    }

    private void setTitleWithDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        setTitle(getString(R.string.app_name) + "на " + dateFormat.format(date));
    }

    private void goToForecastActivity(Bundle bundle) {
        Intent intent = new Intent(this, ForecastActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void startAlarm(int startInterval) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + startInterval, INTERVAL, pendingIntent);
    }

    private boolean isAlarmNotWorking(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.mycompany.myAppName", Context.MODE_PRIVATE);
        if (prefs.getBoolean("alarmfirstrun", true)) {
            prefs.edit().putBoolean("alarmfirstrun", false).commit();
            return true;
        } else {
            return false;
        }
    }

    private class GetIntervalTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (isInternetAvailable(getApplicationContext())) {
                    return Integer.valueOf(readFileFromServer(new URL(BASE_URL + TIMEOUT_HEAD_TXT + getImei(getApplicationContext()))).get(0));
                }
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "MalformedURLException", e);
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer interval) {
            if (isAlarmNotWorking(getApplicationContext())) {
                Log.i(LOG_TAG, "Start alarm with startInterval " + interval);
                startAlarm(interval * 60 * 1000); //  minutes to millis
            } else {
                Log.i(LOG_TAG, "Alarm is already working. Do nothing.");
            }
        }
    }

}
