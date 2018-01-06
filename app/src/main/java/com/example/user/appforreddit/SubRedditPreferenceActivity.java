package com.example.user.appforreddit;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.user.appforreddit.Database.articleContract;
import com.example.user.appforreddit.Database.subredditsContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SubRedditPreferenceActivity extends AppCompatActivity implements SubredditsCustomAdapter.showhideItemClickListener {
    private RecyclerView subredditRV;
    private SubredditsCustomAdapter mAdapter;
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";
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
        mAdapter = new SubredditsCustomAdapter(c, this);
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
        ContentValues cv = new ContentValues();
        Uri articeURI = articleContract.articleEntry.CONTENT_URI;
        articeURI = articeURI.buildUpon().appendPath(subredditURL).build();
        if (s.matches("show")) {
            cv.put(subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT, "hide");
        } else {
            cv.put(subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT, "show");
        }
        Uri updateUri = subredditsContract.subredditEntry.CONTENT_URI;
        updateUri = updateUri.buildUpon().appendPath(clickedItemId).build();
        int updated = this.getContentResolver().update(updateUri, cv, null, null);

        if (updated > 0) {
            Uri queryUri = subredditsContract.subredditEntry.CONTENT_URI;
            Cursor c = this.getContentResolver().query(queryUri, null, null, null, null);
            mAdapter.swapCursor(c);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        subredditRV.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, subredditRV.getLayoutManager().onSaveInstanceState());
    }
}
