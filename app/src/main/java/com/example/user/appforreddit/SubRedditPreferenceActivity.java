package com.example.user.appforreddit;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.user.appforreddit.Database.subredditsContract;

public class SubRedditPreferenceActivity extends AppCompatActivity implements subredditsCustomAdapter.showhideItemClickListener{
    private RecyclerView subredditRV;
    private subredditsCustomAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_reddit_preference);
        subredditRV = (RecyclerView) findViewById(R.id.recyclerView_subreddits);
        Uri queryUri = subredditsContract.subredditEntry.CONTENT_URI;
        Cursor c = this.getContentResolver().query(queryUri, null, null, null, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new subredditsCustomAdapter(c, this);
        subredditRV.setLayoutManager(layoutManager);
        subredditRV.setAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(String clickedItemId, String s) {
      //  Toast.makeText(this, "clicked" + clickedItemId + s, Toast.LENGTH_SHORT).show();
        ContentValues cv = new ContentValues();
        if (s.matches("show")) {
            cv.put(subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT, "hide");
        }else{
            cv.put(subredditsContract.subredditEntry.COLUMN_DISPLAY_SUBREDDIT, "show");
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
}
