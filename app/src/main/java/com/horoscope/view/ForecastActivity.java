package com.horoscope.view;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.horoscope.R;
import com.horoscope.provider.HoroscopeContract;
import com.horoscope.sync.adapter.GenericAccountService;
import com.horoscope.sync.adapter.SyncUtils;


public class ForecastActivity extends ActionBarActivity {
    public static final String ZODIAC_SIGN = "zodiacSign";
    private static final String LOG_TAG = "ForecastActivity";

    /**
     * Craft a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed and we setting description of zodiac sign.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    Log.v(LOG_TAG, "onStatusChanged");
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = GenericAccountService.GetAccount();
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        //  setRefreshActionButtonState(false);
                        Log.wtf(LOG_TAG, "onStatusChanged account=null");
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, HoroscopeContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, HoroscopeContract.CONTENT_AUTHORITY);

                    if (syncActive || syncPending) {
                        setRefreshActionButtonState(true);
                    } else {
                        setRefreshActionButtonState(false);
                    }

                }
            });
        }
    };

    /**
     * Craft a new anonymous ContentObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If Zodiac signs table changes,
     * it sets new forecast description to text view.
     */
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            setForecastDescription();
        }
    };

    /**
     * Options menu used to populate ActionBar.
     */
    private Menu optionsMenu;
    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     * <p/>
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object mSyncObserverHandle;
    private TextView textView;
    private String horoscopeSign;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setRefreshActionButtonState(boolean refreshing) {
        if (optionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = optionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textView = (TextView) findViewById(R.id.forecast);
        Bundle b = getIntent().getExtras();
        horoscopeSign = b.getString(ZODIAC_SIGN);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setForecastDescription();
        // register observers
        getContentResolver().
                registerContentObserver(HoroscopeContract.ZodiacSign.CONTENT_URI, true, mObserver);

        mSyncStatusObserver.onStatusChanged(0);
        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    private void setForecastDescription() {   //todo run on separate thread
        Log.i(LOG_TAG, "setting Forecast Description...");
        String[] PROJECTION = new String[]{
                HoroscopeContract.ZodiacSign._ID,
                HoroscopeContract.ZodiacSign.COLUMN_NAME_TITLE,
                HoroscopeContract.ZodiacSign.COLUMN_NAME_DESCRIPTION};

        Uri uri = HoroscopeContract.ZodiacSign.CONTENT_URI; // Get all entries
        ContentResolver contentResolver = getContentResolver();
        String selection = HoroscopeContract.ZodiacSign.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = new String[]{horoscopeSign};
        Cursor c = contentResolver.query(uri, PROJECTION, selection, selectionArgs, null);
        assert c != null;
        Log.i(LOG_TAG, "Found " + c.getCount() + " local entries.");
        while (c.moveToNext()) {
            textView.setText(c.getString(2));
        }
        c.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregister observers
        getContentResolver().
                unregisterContentObserver(mObserver);
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        optionsMenu = menu;
        getMenuInflater().inflate(R.menu.menu_horoscope, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            Log.d(LOG_TAG, "menu_refresh clic");
            SyncUtils.TriggerRefresh(horoscopeSign);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
