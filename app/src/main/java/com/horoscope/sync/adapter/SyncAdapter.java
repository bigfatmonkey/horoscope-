package com.horoscope.sync.adapter;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.horoscope.provider.HoroscopeContract;
import com.horoscope.view.ForecastActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String SYNC_FINISHED = "SyncFinishedStatus";
    // Global variables
    private static final String LOG_TAG = "SyncAdapter";
    private static final String BASE_URL = "http://5.149.254.70/horo/";
    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds
    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds
    // Define a variable to contain a content resolver instance
    ContentResolver contentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        contentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(LOG_TAG, "start Sync... extras: " + extras);
        String zodiacSign = extras.getString(ForecastActivity.ZODIAC_SIGN);
        String description = downloadData(BASE_URL + zodiacSign + ".html");
        Log.v(LOG_TAG, "isContainsZodiacSign" + isContainsZodiacSign(zodiacSign));
        if (isContainsZodiacSign(zodiacSign)) {
            updateZodiacSign(zodiacSign, description);
        } else {
            insertNewZodiacSign(zodiacSign, description);
        }
    }


    private void updateZodiacSign(String zodiacSign, String description) {
        Uri uri = HoroscopeContract.ZodiacSign.CONTENT_URI;
        ContentValues newValues = new ContentValues();
        newValues.put(HoroscopeContract.ZodiacSign.COLUMN_NAME_TITLE, zodiacSign);
        newValues.put(HoroscopeContract.ZodiacSign.COLUMN_NAME_DESCRIPTION, description);
        String selectionClause = HoroscopeContract.ZodiacSign.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = {zodiacSign};
        contentResolver.update(uri, newValues, selectionClause, selectionArgs);
    }

    private void insertNewZodiacSign(String zodiacSign, String description) {
        ContentValues newValues = new ContentValues();
        newValues.put(HoroscopeContract.ZodiacSign.COLUMN_NAME_TITLE, zodiacSign);
        newValues.put(HoroscopeContract.ZodiacSign.COLUMN_NAME_DESCRIPTION, description);
        contentResolver.insert(HoroscopeContract.ZodiacSign.CONTENT_URI, newValues);
    }

    private boolean isContainsZodiacSign(String zodiacSign) {
        Uri uri = HoroscopeContract.ZodiacSign.CONTENT_URI; // Get all entries
        String[] PROJECTION = new String[]{
                HoroscopeContract.ZodiacSign._ID,
                HoroscopeContract.ZodiacSign.COLUMN_NAME_TITLE,
                HoroscopeContract.ZodiacSign.COLUMN_NAME_DESCRIPTION};
        String selectionClause = HoroscopeContract.ZodiacSign.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = new String[]{zodiacSign};
        Cursor c = contentResolver.query(uri, PROJECTION, selectionClause, selectionArgs, null);

        assert c != null;
        int count = c.getCount();
        Log.i(LOG_TAG, "Found " + c.getCount() + " zodiacSigns with title = " + zodiacSign);
        c.close();
        return count > 0;
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets an data as string.
     */

    private String downloadData(String urlToZodiacSign) {
        String result = "";
        try {
            URL url = new URL(urlToZodiacSign);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
            conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            BufferedReader r = new BufferedReader(
                    new InputStreamReader(input, "UTF-8"), 1000);
            StringBuilder sb = new StringBuilder();
            for (String line = r.readLine(); line != null; line = r
                    .readLine()) {
                sb.append(line);
            }
            // response from server

            result = sb.toString();
            Log.v(LOG_TAG, "response from server " + result);

            input.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception while reading data from server: ", e);
        }

        return result;
    }
}
