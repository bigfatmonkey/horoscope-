package com.horoscope.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Костя on 10.03.2015.
 */
public class HoroscopeContract {
    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.horoscope";
    /**
     * Base URI. (content://com.horoscope)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Path component for "zodiacsigns"-type resources..
     */
    private static final String ZODIAC_SIGNS = "zodiacsigns";

    private HoroscopeContract() {
    }

    /**
     * Columns supported by "entries" records.
     */
    public static class ZodiacSign implements BaseColumns {
        /**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basicsyncadapter.zodiacsigns";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basicsyncadapter.zodiacsign";

        /**
         * Fully qualified URI for "entry" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(ZODIAC_SIGNS).build();

        /**
         * Table name where records are stored for "entry" resources.
         */
        public static final String TABLE_NAME = "zodiacsigns";
        /**
         * Atom ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_ENTRY_ID = "zodiacsign_id";
        /**
         * Article title
         */
        public static final String COLUMN_NAME_TITLE = "title";
        /**
         * Article hyperlink. Corresponds to the rel="alternate" link in the
         * Atom spec.
         */
        public static final String COLUMN_NAME_DESCRIPTION = "description";

    }
}
