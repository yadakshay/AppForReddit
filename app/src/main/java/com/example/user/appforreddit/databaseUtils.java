package com.example.user.appforreddit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.user.appforreddit.Database.articleContract;
import com.example.user.appforreddit.Database.subredditsContract;

import java.util.ArrayList;

/**
 * Created by user on 03-01-2018.
 */

public class databaseUtils {
    private static final String TAG = "redditAppDatabaseUtils";
    static Context context, contextForArticle;
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

    public static void addToArticlesDatabase(ArrayList<articleCustomObject> articlesList, Context c){
        contextForArticle =c;
        for(int i = 0; i<articlesList.size(); i++){
            checkDuplicateAndInsertArticle(articlesList.get(i));
        }
    }

    public static void checkDuplicateAndInsertArticle(articleCustomObject article){
        ContentValues cv = new ContentValues();
        Uri uri = null;
        cv.put(articleContract.articleEntry.COLUMN_ARTICLE_URL, article.getResourceURL());
        cv.put(articleContract.articleEntry.COLUM_ARTICLE_TITLE, article.getArticleTitle());
        cv.put(articleContract.articleEntry.COLUMN_IMAGE_THUMB, article.getArticleThumbnail());
        cv.put(articleContract.articleEntry.COLUMN_ARTICLE_ID, article.getArticleId());
        cv.put(articleContract.articleEntry.COLUMN_SUBREDDIT_URL, article.getSubredditURL());
        Uri queryUri = articleContract.articleEntry.CONTENT_URI;
        queryUri = queryUri.buildUpon().appendPath(article.getSubredditURL()).build();
        Cursor c = contextForArticle.getContentResolver().query(queryUri, null, null, null, null);
        if(c !=null && c.getCount()>0){//do nothing
        }else{
            uri = contextForArticle.getContentResolver().insert(articleContract.articleEntry.CONTENT_URI, cv);
        }
    }

    public static int replaceArticleWithNewArticle(articleCustomObject article, Context con){
        ContentValues cv = new ContentValues();
        Uri uri = null;
        int updated;
        cv.put(articleContract.articleEntry.COLUMN_ARTICLE_URL, article.getResourceURL());
        cv.put(articleContract.articleEntry.COLUM_ARTICLE_TITLE, article.getArticleTitle());
        cv.put(articleContract.articleEntry.COLUMN_IMAGE_THUMB, article.getArticleThumbnail());
        cv.put(articleContract.articleEntry.COLUMN_ARTICLE_ID, article.getArticleId());
        Uri updateUri = articleContract.articleEntry.CONTENT_URI;
        updateUri = updateUri.buildUpon().appendPath(article.getSubredditURL()).build();
        updated = con.getContentResolver().update(updateUri, cv, null, null);
        return updated;
    }
}
