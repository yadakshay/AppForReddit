package com.example.user.appforreddit;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.user.appforreddit.Database.subredditsContract;

public class SubRedditPreferenceActivity extends AppCompatActivity implements subredditsCustomAdapter.showhideItemClickListener{
private RecyclerView subredditRV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_reddit_preference);
        subredditRV = (RecyclerView) findViewById(R.id.recyclerView_subreddits);
        Uri queryUri = subredditsContract.subredditEntry.CONTENT_URI;
        Cursor c = this.getContentResolver().query(queryUri, null, null, null, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        subredditRV.setLayoutManager(layoutManager);
        subredditRV.setAdapter(new subredditsCustomAdapter(c, this));
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Toast.makeText(this, "clicked" + clickedItemIndex, Toast.LENGTH_SHORT).show();
    }
}
