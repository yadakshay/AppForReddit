package com.example.user.appforreddit.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Akshay on 04-01-2018.
 */

public class ArticleDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "articles.db";
    private static final int DATABASE_VERSION = 1;

    public ArticleDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ARTICLES_TABLE = "CREATE TABLE " +
                ArticleContract.articleEntry.TABLE_NAME + " (" +
                ArticleContract.articleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ArticleContract.articleEntry.COLUMN_ARTICLE_URL + " TEXT NOT NULL, " +
                ArticleContract.articleEntry.COLUM_ARTICLE_TITLE + " TEXT NOT NULL, " +
                ArticleContract.articleEntry.COLUMN_IMAGE_THUMB + " TEXT NOT NULL, " +
                ArticleContract.articleEntry.COLUMN_ARTICLE_ID + " TEXT NOT NULL, " +
                ArticleContract.articleEntry.COLUMN_SUBREDDIT_URL + " TEXT NOT NULL" +
                ");";

        db.execSQL(SQL_CREATE_ARTICLES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SubredditsContract.subredditEntry.TABLE_NAME);
        onCreate(db);
    }
}
