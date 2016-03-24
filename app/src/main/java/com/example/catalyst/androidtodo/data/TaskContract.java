package com.example.catalyst.androidtodo.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dsloane on 3/2/2016.
 */
public class TaskContract {

    public static final String CONTENT_AUTHORITY = "com.example.catalyst.androidtodo";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TASK = "task";

    public static final String DATABASE_NAME = "Tasks.db";

    public static final class TaskEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASK).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASK;

        public static Uri buildTaskUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "task";

        public static final String COLUMN_TASK_TITLE = "title";

        public static final String COLUMN_TASK_DETAILS = "details";

        public static final String COLUMN_DUE_DATE = "due_date";

        public static final String COLUMN_LOCATION_NAME = "location_name";

        public static final String COLUMN_LATITUDE = "latitude";

        public static final String COLUMN_LONGITUDE = "longitude";

        public static final String COLUMN_PARTICIPANT = "participants";

        public static final String COLUMN_LAST_MODIFIED_DATE = "last_modified_date";

        public static final String COLUMN_SYNC_DATE = "sync_date";

        public static final String COLUMN_SERVER_ID = "server_id";

        public static final String COLUMN_TIMEZONE = "timezone";

        public static final String COLUMN_COMPLETED = "completed";
    }
}
