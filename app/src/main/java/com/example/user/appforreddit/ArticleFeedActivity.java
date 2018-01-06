package com.example.user.appforreddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.user.appforreddit.Database.articleContract;
import com.example.user.appforreddit.Database.articleDbHelper;
import com.example.user.appforreddit.Database.subredditsContract;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import static android.R.attr.name;

public class ArticleFeedActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<ArticleCustomObject>>, MainArticleAapter.dismissItemClickListener {
    private static final int ARTICLES_LOADER_ID = 1008;
    public static final String ARTICLE_URL_KEY = "articleKey";
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";
    private static final String TAG = "ArticleFeedActivity";
    private TextView tv;
    ProgressBar spinner;
    MainArticleAapter mAdapter;
    RecyclerView mainListRecycler;
    FloatingActionButton fab;
    Tracker mTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_feed);
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        tv = (TextView) findViewById(R.id.textView);
        spinner = (ProgressBar) findViewById(R.id.loading_spinner);
        mainListRecycler = (RecyclerView) findViewById(R.id.mainArticleList);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("FabHit")
                        .build());
                Intent i = new Intent(ArticleFeedActivity.this, SubRedditPreferenceActivity.class);
                startActivity(i);
                finish();
            }
        });
        if (getIntent() != null) {
            getSupportLoaderManager().initLoader(ARTICLES_LOADER_ID, null, ArticleFeedActivity.this).forceLoad();
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader(this) {
            ArrayList<ArticleCustomObject> articlesList;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                spinner.setVisibility(View.VISIBLE);
            }

            @Override
            public ArrayList<ArticleCustomObject> loadInBackground() {
                Uri queryUri = subredditsContract.subredditEntry.CONTENT_URI;
                Cursor c = getContext().getContentResolver().query(queryUri, null, subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT + " = ?",
                        new String[]{"show"}, null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        articlesList = NetworkUtils.getArticlesFromCursor(c, getApplicationContext());
                    }
                }
                if (articlesList != null) {
                    if (articlesList.size() > 0) {
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
    public void onLoadFinished(Loader<ArrayList<ArticleCustomObject>> loader, ArrayList<ArticleCustomObject> data) {
        spinner.setVisibility(View.GONE);
        mainListRecycler.setVisibility(View.VISIBLE);
        if (data != null) {
            if (data.size() > 0) {
                Uri queryUri = articleContract.articleEntry.CONTENT_URI;
                Cursor c = getApplicationContext().getContentResolver().query(queryUri, null, null, null, null);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                mAdapter = new MainArticleAapter(c, this, this);
                mainListRecycler.setLayoutManager(layoutManager);
                mainListRecycler.setAdapter(mAdapter);
            }
        } else {
            tv.setText(R.string.noDataRetrived);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ArticleCustomObject>> loader) {
    }

    @Override
    public void onListItemClick(String clickedArticleId, String url) {
        if (clickedArticleId != null) {
            new getNewArticleTask().execute(clickedArticleId, url);
        } else {
            Intent i = new Intent(ArticleFeedActivity.this, WebViewActivity.class);
            i.putExtra(ARTICLE_URL_KEY, url);
            startActivity(i);
        }
    }

    private class getNewArticleTask extends AsyncTask<String, Void, ArticleCustomObject> {
        int update;

        @Override
        protected ArticleCustomObject doInBackground(String... params) {
            String articleId = params[0];
            String url = params[1];
            String json = NetworkUtils.getArticleForSubreddit(url, articleId, getApplicationContext());
            ArticleCustomObject article = NetworkUtils.extractArticleFromJson(json, url);
            update = databaseUtils.replaceArticleWithNewArticle(article, getApplicationContext());
            return article;
        }

        @Override
        protected void onPostExecute(ArticleCustomObject i) {
            super.onPostExecute(i);
            if (update > 0) {
                Uri queryUri = articleContract.articleEntry.CONTENT_URI;
                Cursor c = getApplicationContext().getContentResolver().query(queryUri, null, null, null, null);
                c.moveToFirst();
                mAdapter.swapArticleCursor(c);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int clickedItem = item.getItemId();
        if (clickedItem == R.id.logout) {
            SharedPreferences pref = this.getSharedPreferences("AppPref", 0);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("token", "");
            edit.putString("refreshToken", "");
            edit.putBoolean("isLoggedIn", false);
            edit.commit();
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
            dispatcher.cancel("refresh-tag"); // cancel the firebase job Dispatcher
            Intent i = new Intent(ArticleFeedActivity.this, MainActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        mainListRecycler.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Image~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mainListRecycler != null) {
            if (mainListRecycler.getLayoutManager() != null) {
                if (mainListRecycler.getLayoutManager().onSaveInstanceState() != null) {
                    outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mainListRecycler.getLayoutManager().onSaveInstanceState());
                }
            }
        }
    }
}
