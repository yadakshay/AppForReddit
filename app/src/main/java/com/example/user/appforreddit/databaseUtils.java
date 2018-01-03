package com.example.user.appforreddit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.user.appforreddit.Database.subredditsContract;

import java.util.ArrayList;

/**
 * Created by user on 03-01-2018.
 */

public class databaseUtils {
    private static final String TAG = "redditAppDatabaseUtils";
    static Context context;
    public static void insertSubredditsToDatabase(ArrayList<subredditCustomObject> subredditList, Context c){
        context = c;
        for(int i = 0; i<subredditList.size(); i++){
            checkDupicateAndInsertSubreddit(subredditList.get(i));
        }
    }

    public static Uri checkDupicateAndInsertSubreddit(subredditCustomObject subredditObject){
        ContentValues cv = new ContentValues();
        Uri uri = null;
        cv.put(subredditsContract.subredditEntry.COLUMN_SUBREDDIT_ID, subredditObject.getSubredditId());
        cv.put(subredditsContract.subredditEntry.COLUMN_SUBREDDIT_DISPLAYNAME_PREFIXED, subredditObject.getPrefixedDisplayName());
        cv.put(subredditsContract.subredditEntry.COLUMN_SUBREDDIT_URL, subredditObject.getSubredditUrl());
        cv.put(subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT, subredditObject.getDisplaySubreddit());
        cv.put(subredditsContract.subredditEntry.COLUMN_SUBREDDIT_DESCRIPTION, subredditObject.getSubDescription());
        Uri queryUri = subredditsContract.subredditEntry.CONTENT_URI;
        queryUri = queryUri.buildUpon().appendPath(subredditObject.getSubredditId()).build();
        Cursor c = context.getContentResolver().query(queryUri, null, null, null, null);
        if(c !=null && c.getCount()>0){return null;}
        else{
            uri = context.getContentResolver().insert(subredditsContract.subredditEntry.CONTENT_URI, cv);
            if (uri != null) {
            //    Toast.makeText(context, "Sucess", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "success submitting " + uri);
            } else {
            //    Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "failed updating db " + uri);
            }}
        return uri;
    }

}
