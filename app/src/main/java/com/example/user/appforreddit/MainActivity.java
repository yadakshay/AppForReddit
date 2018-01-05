package com.example.user.appforreddit;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.user.appforreddit.Database.subredditDbHelper;

import java.util.ArrayList;

import static com.example.user.appforreddit.Database.subredditsContract.subredditEntry.TABLE_NAME;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<subredditCustomObject>> {

    private static final String AUTH_URL =
            "https://www.reddit.com/api/v1/authorize.compact?client_id=%s" +
                    "&response_type=code&state=%s&redirect_uri=%s&" +
                    "duration=permanent&scope=mysubreddits,read";
    private static final String CLIENT_ID = "2XIKITvqkmX-yg";

    private static final String REDIRECT_URI =
            "http://www.example.com/my_redirect";

    private static final String STATE = "MY_RANDOM_STRING_1";
    private static final int SUBREDITS_LOADER_ID = 1004;
    private static final String ACCESSCODE_ID = "accessCodeId";
    private static String TAG = "RedditMainActivity";
    private boolean tokenObtained;
    private TextView tv;
    private ProgressBar loadingBar;
    private Button signInToReddit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textView);
        loadingBar = (ProgressBar) findViewById(R.id.loading_spinner);
        signInToReddit = (Button) findViewById(R.id.signin);
    }

    public void startSignIn(View view) {
        String url = String.format(AUTH_URL, CLIENT_ID, STATE, REDIRECT_URI);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent()!=null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            if(uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                Log.e("TAG:", "An error has occurred : " + error);
            } else {
                String state = uri.getQueryParameter("state");
                if(state.equals(STATE)) {
                    String code = uri.getQueryParameter("code");
                    Bundle bundleForLoader = new Bundle();
                    bundleForLoader.putString(ACCESSCODE_ID, code);
                    getSupportLoaderManager().initLoader(SUBREDITS_LOADER_ID, bundleForLoader, MainActivity.this).forceLoad();
                }
            }
        }
    }

    @Override
    public Loader onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader(this){
            String accessCode = args.getString(ACCESSCODE_ID);
            String json = null;
            ArrayList<subredditCustomObject> subscribedSubreddits;
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                signInToReddit.setVisibility(View.GONE);
                loadingBar.setVisibility(View.VISIBLE);
            }

            @Override
            public ArrayList<subredditCustomObject> loadInBackground() {
                boolean haveToken = NetworkUtils.getSyncAccessToken(accessCode, getContext());
                if(haveToken){
                    json = NetworkUtils.makeSubredditCall(getApplicationContext());
                }
                subredditDbHelper dbHelper = new subredditDbHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(TABLE_NAME, null, null); //clear old data for first login
                subscribedSubreddits = NetworkUtils.extractSubredditsFromJSON(json);
                databaseUtils.insertSubredditsToDatabase(subscribedSubreddits, getApplicationContext());
                return subscribedSubreddits;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<subredditCustomObject>> loader, ArrayList<subredditCustomObject> data) {
        loadingBar.setVisibility(View.GONE);
        Intent i = new Intent(MainActivity.this, ArticleFeedActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
