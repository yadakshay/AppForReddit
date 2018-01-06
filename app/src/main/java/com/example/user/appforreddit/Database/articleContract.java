package com.example.user.appforreddit.Database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Akshay on 04-01-2018.
 */

public class articleContract {

    public static final String AUTHORITY = "com.example.user.appforreddit.articleDb";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TASK = "subredditURL";
    //constructor
    private articleContract(){}

    public static final class articleEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASK).build();
        public static final String TABLE_NAME = "articles";
        public static final String COLUMN_ARTICLE_URL = "articleUrl";
        public static final String COLUM_ARTICLE_TITLE = "articleTitle";
        public static final String COLUMN_IMAGE_THUMB = "imageThumbnail";
        public static final String COLUMN_ARTICLE_ID = "subredditId";
        public static final String COLUMN_SUBREDDIT_URL = "subredditUrl";
    }
}
