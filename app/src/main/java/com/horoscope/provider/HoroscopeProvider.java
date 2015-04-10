package com.horoscope.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.horoscope.util.SelectionBuilder;

/**
 * Created by Костя on 10.03.2015.
 */
public class HoroscopeProvider extends ContentProvider {

    /**
     * URI ID for route: /entries
     */
    public static final int ROUTE_ZODIAC_SIGNS = 1;
    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ROUTE_ZODIAC_SIGNS_ID = 2;

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = HoroscopeContract.CONTENT_AUTHORITY;
    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "zodiacsigns", ROUTE_ZODIAC_SIGNS);
        sUriMatcher.addURI(AUTHORITY, "zodiacsigns/*", ROUTE_ZODIAC_SIGNS_ID);
    }

    private HoroscopeDatabase mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new HoroscopeDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ZODIAC_SIGNS:
                return HoroscopeContract.ZodiacSign.CONTENT_TYPE;
            case ROUTE_ZODIAC_SIGNS_ID:
                return HoroscopeContract.ZodiacSign.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_ZODIAC_SIGNS_ID:
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(HoroscopeContract.ZodiacSign._ID + "=?", id);
            case ROUTE_ZODIAC_SIGNS:
                // Return all known entries.
                builder.table(HoroscopeContract.ZodiacSign.TABLE_NAME)
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                c.setNotificationUri(getContext().getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_ZODIAC_SIGNS:
                long id = db.insertOrThrow(HoroscopeContract.ZodiacSign.TABLE_NAME, null, contentValues);
                result = Uri.parse(HoroscopeContract.ZodiacSign.CONTENT_URI + "/" + id);
                break;
            case ROUTE_ZODIAC_SIGNS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        getContext().getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ZODIAC_SIGNS:
                count = builder.table(HoroscopeContract.ZodiacSign.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, contentValues);
                break;
            case ROUTE_ZODIAC_SIGNS_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(HoroscopeContract.ZodiacSign.TABLE_NAME)
                        .where(HoroscopeContract.ZodiacSign._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, contentValues);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    static class HoroscopeDatabase extends SQLiteOpenHelper {
        /**
         * Schema version.
         */
        public static final int DATABASE_VERSION = 1;
        /**
         * Filename for SQLite file.
         */
        public static final String DATABASE_NAME = "horoscope.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";
        /**
         * SQL statement to create "zodiac signs" table.
         */
        private static final String SQL_CREATE_ZODIAC_SIGNS =
                "CREATE TABLE " + HoroscopeContract.ZodiacSign.TABLE_NAME + " (" +
                        HoroscopeContract.ZodiacSign._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        HoroscopeContract.ZodiacSign.COLUMN_NAME_TITLE + TYPE_TEXT + " UNIQUE" + COMMA_SEP +
                        HoroscopeContract.ZodiacSign.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + ")";

        /**
         * SQL statement to drop "entry" table.
         */
        private static final String SQL_DELETE_ZODIAC_SIGNS =
                "DROP TABLE IF EXISTS " + HoroscopeContract.ZodiacSign.TABLE_NAME;

        public HoroscopeDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ZODIAC_SIGNS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ZODIAC_SIGNS);
            onCreate(db);
        }
    }
}
