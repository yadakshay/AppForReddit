package com.example.user.appforreddit;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.google.android.gms.ads.AdRequest;
import com.example.user.appforreddit.Database.articleContract;
import com.example.user.appforreddit.Database.subredditsContract;
import com.google.android.gms.ads.AdView;

public class SubRedditPreferenceActivity extends AppCompatActivity implements subredditsCustomAdapter.showhideItemClickListener{
    private RecyclerView subredditRV;
    private subredditsCustomAdapter mAdapter;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_reddit_preference);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
        subredditRV = (RecyclerView) findViewById(R.id.recyclerView_subreddits);
        Uri queryUri = subredditsContract.subredditEntry.CONTENT_URI;
        Cursor c = this.getContentResolver().query(queryUri, null, null, null, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new subredditsCustomAdapter(c, this);
        subredditRV.setLayoutManager(layoutManager);
        subredditRV.setAdapter(mAdapter);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SubRedditPreferenceActivity.this, ArticleFeedActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onListItemClick(String clickedItemId, String s, String subredditURL) {
      //  Toast.makeText(this, "clicked" + clickedItemId + s, Toast.LENGTH_SHORT).show();
        ContentValues cv = new ContentValues();
        Uri articeURI = articleContract.articleEntry.CONTENT_URI;
        articeURI = articeURI.buildUpon().appendPath(subredditURL).build();
        if (s.matches("show")) {
            cv.put(subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT, "hide");
       //     int i = this.getContentResolver().delete(articeURI, null, null);
        }else{
            cv.put(subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT, "show");
     //      new getNewArticleTask().execute(subredditURL);
        }
        Uri updateUri = subredditsContract.subredditEntry.CONTENT_URI;
        updateUri = updateUri.buildUpon().appendPath(clickedItemId).build();
        int updated = this.getContentResolver().update(updateUri, cv, null, null);

        if(updated>0){
            Uri queryUri = subredditsContract.subredditEntry.CONTENT_URI;
            Cursor c = this.getContentResolver().query(queryUri, null, null, null, null);
            mAdapter.swapCursor(c);
        }
    }

    private class getNewArticleTask extends AsyncTask<String, Void, articleCustomObject>{
        int update;
        @Override
        protected articleCustomObject doInBackground(String... params) {
            String subreddiUrl = params[0];
            String json = NetworkUtils.getArticleForSubreddit(subreddiUrl, null, getApplicationContext());
            articleCustomObject article = NetworkUtils.extractArticleFromJson(json, subreddiUrl);
            databaseUtils.checkDuplicateAndInsertIndArticle(article, getApplicationContext());
            return article;
        }
    }
}
