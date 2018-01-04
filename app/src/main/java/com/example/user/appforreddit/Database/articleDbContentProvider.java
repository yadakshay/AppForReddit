package com.example.user.appforreddit.Database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Created by user on 04-01-2018.
 */

public class articleDbContentProvider extends ContentProvider {
    public static final int ARTICLE = 105;
    public static final int ARTICLE_UPDATE = 106;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private articleDbHelper mDbHelper;
    private static UriMatcher buildUriMatcher() {
        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the task directory and a single item by ID.
         */;
        uriMatcher.addURI(articleContract.AUTHORITY, articleContract.PATH_TASK, ARTICLE);
        uriMatcher.addURI(articleContract.AUTHORITY, articleContract.PATH_TASK + "/*", ARTICLE_UPDATE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDbHelper = new articleDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;
        switch (match) {
            case ARTICLE:
                retCursor = db.query(articleContract.articleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ARTICLE_UPDATE:
                String subredditUrl = uri.getPathSegments().get(1);
                retCursor = db.query(articleContract.articleEntry.TABLE_NAME,
                        null,
                        articleContract.articleEntry.COLUMN_SUBREDDIT_URL + " = ?",
                        new String[]{subredditUrl},
                        null,
                        null,
                        null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case ARTICLE:
                long id = db.insert(articleContract.articleEntry.TABLE_NAME, null, values);
                if(id > 0)
                {
                    returnUri = ContentUris.withAppendedId(articleContract.articleEntry.CONTENT_URI, id);
                }else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        //Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int articleDeleted;
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case ARTICLE_UPDATE:
                // Get the task ID from the URI path
                String subredditUrl = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                articleDeleted = db.delete(articleContract.articleEntry.TABLE_NAME,
                        articleContract.articleEntry.COLUMN_SUBREDDIT_URL + " = ?",
                        new String[]{subredditUrl});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }        // Notify the resolver of a change and return the number of items deleted
        if (articleDeleted > 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return articleDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
