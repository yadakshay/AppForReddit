package com.example.user.appforreddit;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.user.appforreddit.Database.articleContract;
import com.example.user.appforreddit.Database.articleDbHelper;
import com.example.user.appforreddit.Database.subredditsContract;

import java.util.ArrayList;

public class ArticleFeedActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<articleCustomObject>>{
    private static final int ARTICLES_LOADER_ID = 1008;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_feed);
        tv = (TextView) findViewById(R.id.textView);
        getSupportLoaderManager().initLoader(ARTICLES_LOADER_ID, null, ArticleFeedActivity.this).forceLoad();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader(this){
            ArrayList<articleCustomObject> articlesList;
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
            }
            @Override
            public ArrayList<articleCustomObject> loadInBackground() {
                Uri queryUri = subredditsContract.subredditEntry.CONTENT_URI;
                Cursor c = getContext().getContentResolver().query(queryUri, null, subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT + " = ?",
                        new String[]{"show"}, null);
                if(c != null) {
                    if(c.getCount()>0) {
                        articlesList = NetworkUtils.getArticlesFromCursor(c);
                    }
                }
                if(articlesList != null){
                    if(articlesList.size()>0){
                        articleDbHelper dbHelper = new articleDbHelper(getApplicationContext());
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete(articleContract.articleEntry.TABLE_NAME, null, null); //delete existing articles for first login
                        databaseUtils.addToArticlesDatabase(articlesList, getApplicationContext());
                    }
                }
                return articlesList;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<articleCustomObject>> loader, ArrayList<articleCustomObject> data) {
        if(data != null) {
            if(data.size()>0) {
                Uri queryUri = articleContract.articleEntry.CONTENT_URI;
                Cursor c = getApplicationContext().getContentResolver().query(queryUri, null, null, null, null);
                c.moveToFirst();
                tv.setText(c.getString(c.getColumnIndex(articleContract.articleEntry.COLUM_ARTICLE_TITLE)));
            }
        }else{
            tv.setText("No data retrieved");
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<articleCustomObject>> loader) {

    }
}
