package com.example.user.appforreddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;

public class ArticleFeedActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<articleCustomObject>>, mainArticleAapter.dismissItemClickListener{
    private static final int ARTICLES_LOADER_ID = 1008;
    public static final String ARTICLE_URL_KEY = "articleKey";
    private TextView tv;
    ProgressBar spinner;
    mainArticleAapter mAdapter;
    RecyclerView mainListRecycler;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_feed);
        tv = (TextView) findViewById(R.id.textView);
        spinner = (ProgressBar) findViewById(R.id.loading_spinner);
        mainListRecycler = (RecyclerView) findViewById(R.id.mainArticleList);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ArticleFeedActivity.this, SubRedditPreferenceActivity.class);
                startActivity(i);
                finish();
            }
        });
        if(getIntent() != null) {
            getSupportLoaderManager().initLoader(ARTICLES_LOADER_ID, null, ArticleFeedActivity.this).forceLoad();
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader(this){
            ArrayList<articleCustomObject> articlesList;
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                spinner.setVisibility(View.VISIBLE);
            }
            @Override
            public ArrayList<articleCustomObject> loadInBackground() {
                Uri queryUri = subredditsContract.subredditEntry.CONTENT_URI;
                Cursor c = getContext().getContentResolver().query(queryUri, null, subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT + " = ?",
                        new String[]{"show"}, null);
                if(c != null) {
                    if(c.getCount()>0) {
                        articlesList = NetworkUtils.getArticlesFromCursor(c, getApplicationContext());
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
        spinner.setVisibility(View.GONE);
        mainListRecycler.setVisibility(View.VISIBLE);
        if(data != null) {
            if(data.size()>0) {
                Uri queryUri = articleContract.articleEntry.CONTENT_URI;
                Cursor c = getApplicationContext().getContentResolver().query(queryUri, null, null, null, null);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                mAdapter = new mainArticleAapter(c, this, this);
                mainListRecycler.setLayoutManager(layoutManager);
                mainListRecycler.setAdapter(mAdapter);
                SharedPreferences pref = this.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = pref.edit();
                edit.putBoolean("isFirstVisitToFeed", false);
            }
        }else{
            tv.setText("No data retrieved");
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<articleCustomObject>> loader) {

    }

    @Override
    public void onListItemClick(String clickedArticleId, String url) {
        //Toast.makeText(this, "clicked x" + clickedItemId, Toast.LENGTH_SHORT).show();
        if(clickedArticleId != null) {
            new getNewArticleTask().execute(clickedArticleId, url);
        }else{
            Intent i = new Intent(ArticleFeedActivity.this, webViewActivity.class);
            i.putExtra(ARTICLE_URL_KEY, url);
            startActivity(i);
        }
    }

    private class getNewArticleTask extends AsyncTask<String, Void, articleCustomObject>{
        int update;
        @Override
        protected articleCustomObject doInBackground(String... params) {
            String articleId = params[0];
            String url = params[1];
            String json = NetworkUtils.getArticleForSubreddit(url, articleId, getApplicationContext());
            articleCustomObject article = NetworkUtils.extractArticleFromJson(json, url);
         //   Log.d("ASYNCTASK", article.getArticleTitle());
            update = databaseUtils.replaceArticleWithNewArticle(article, getApplicationContext());
         //   Log.d("ASUNCTASK", "database updated" + update);
            return article;
        }

        @Override
        protected void onPostExecute(articleCustomObject i) {
            super.onPostExecute(i);
            if(update>0){
                Uri queryUri = articleContract.articleEntry.CONTENT_URI;
                Cursor c = getApplicationContext().getContentResolver().query(queryUri, null, null, null, null);
                c.moveToFirst();
              //  Log.d("ASYNCTASK", c.getString(c.getColumnIndex(articleContract.articleEntry.COLUM_ARTICLE_TITLE)));
                mAdapter.swapArticleCursor(c);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAdapter!=null){
        mAdapter.notifyDataSetChanged();
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
        if(clickedItem == R.id.logout){
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
}
