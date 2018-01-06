package com.example.user.appforreddit.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Akshay on 03-01-2018.
 */

public class SubredditDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "subreddits.db";
    private static final int DATABASE_VERSION = 1;

    public SubredditDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_SUBREDDITS_TABLE = "CREATE TABLE " +
                SubredditsContract.subredditEntry.TABLE_NAME + " (" +
                SubredditsContract.subredditEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SubredditsContract.subredditEntry.COLUMN_SUBREDDIT_ID + " TEXT NOT NULL, " +
                SubredditsContract.subredditEntry.COLUMN_SUBREDDIT_DISPLAYNAME_PREFIXED + " TEXT NOT NULL, " +
                SubredditsContract.subredditEntry.COLUMN_SUBREDDIT_URL + " TEXT NOT NULL, " +
                SubredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT + " TEXT NOT NULL, " +
                SubredditsContract.subredditEntry.COLUMN_SUBREDDIT_DESCRIPTION + " TEXT" +
                ");";

        db.execSQL(SQL_CREATE_SUBREDDITS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SubredditsContract.subredditEntry.TABLE_NAME);
        onCreate(db);
    }
}
