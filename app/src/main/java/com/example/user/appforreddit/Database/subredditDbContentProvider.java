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

import static com.example.user.appforreddit.Database.subredditsContract.subredditEntry.TABLE_NAME;

/**
 * Created by Akshay on 03-01-2018.
 */

public class subredditDbContentProvider extends ContentProvider {
    public static final int SUBREDDIT = 100;
    public static final int SUBREDDIT_UPDATE = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private subredditDbHelper mDbHelper;
    private static UriMatcher buildUriMatcher() {
        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the task directory and a single item by ID.
         */;
        uriMatcher.addURI(subredditsContract.AUTHORITY, subredditsContract.PATH_TASK, SUBREDDIT);
        uriMatcher.addURI(subredditsContract.AUTHORITY, subredditsContract.PATH_TASK + "/*", SUBREDDIT_UPDATE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDbHelper = new subredditDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;
        switch (match) {
            case SUBREDDIT:
                retCursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case SUBREDDIT_UPDATE:
                String subredditId = uri.getPathSegments().get(1);
                retCursor = db.query(TABLE_NAME,
                        null,
                        subredditsContract.subredditEntry.COLUMN_SUBREDDIT_ID + " = ?",
                        new String[]{subredditId},
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
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues cv) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case SUBREDDIT:
                long id = db.insert(TABLE_NAME, null, cv);
                if(id > 0)
                {
                    returnUri = ContentUris.withAppendedId(subredditsContract.subredditEntry.CONTENT_URI, id);
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
        int subredditDeleted;
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case SUBREDDIT_UPDATE:
                // Get the task ID from the URI path
                String subredditId = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                subredditDeleted = db.delete(TABLE_NAME, subredditsContract.subredditEntry.COLUMN_SUBREDDIT_ID + " = ?", new String[]{subredditId});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }        // Notify the resolver of a change and return the number of items deleted
        if (subredditDeleted > 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return subredditDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
