package com.example.user.appforreddit.Database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Akshay on 03-01-2018.
 */

public class subredditsContract {
    public static final String AUTHORITY = "com.example.user.appforreddit";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TASK = "subReddits";

    //constructor
    private subredditsContract() {
    }

    public static final class subredditEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASK).build();
        public static final String TABLE_NAME = "subreddits";
        public static final String COLUMN_SUBREDDIT_ID = "subredditId";
        public static final String COLUMN_SUBREDDIT_DISPLAYNAME_PREFIXED = "prefixedDisplayName";
        public static final String COLUMN_SUBREDDIT_URL = "subredditUrl";
        public static final String COLUMN_DISPLAY_SUBREDDIT = "displaySubreddit";
        public static final String COLUMN_SUBREDDIT_DESCRIPTION = "subDescription";
    }
}
