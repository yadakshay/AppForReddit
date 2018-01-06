package com.example.user.appforreddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import static com.example.user.appforreddit.Database.subredditsContract.subredditEntry.TABLE_NAME;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<SubredditCustomObject>> {

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
    private TextView tv;
    private ProgressBar loadingBar;
    private Button signInToReddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        tv = (TextView) findViewById(R.id.textView);
        loadingBar = (ProgressBar) findViewById(R.id.loading_spinner);
        signInToReddit = (Button) findViewById(R.id.signin);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        if (isConnected) {
            signInToReddit.setVisibility(View.VISIBLE);
            tv.setVisibility(View.GONE);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
            mAdView.loadAd(adRequest);
            SharedPreferences pref = this.getSharedPreferences("AppPref", 0);
            boolean isLoggedIn = pref.getBoolean("isLoggedIn", false);
            if (isLoggedIn) {
                Intent i = new Intent(MainActivity.this, ArticleFeedActivity.class);
                startActivity(i);
                finish();
            }
        } else {
            tv.setVisibility(View.VISIBLE);
            signInToReddit.setVisibility(View.GONE);
        }
    }

    public void startSignIn(View view) {
        String url = String.format(AUTH_URL, CLIENT_ID, STATE, REDIRECT_URI);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null) {
            if (getIntent().getAction() != null) {
                if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
                    Uri uri = getIntent().getData();
                    if (uri.getQueryParameter("error") != null) {
                        String error = uri.getQueryParameter("error");
                        Log.e("TAG:", "An error has occurred : " + error);
                    } else {
                        String state = uri.getQueryParameter("state");
                        if (state.equals(STATE)) {
                            String code = uri.getQueryParameter("code");
                            Bundle bundleForLoader = new Bundle();
                            bundleForLoader.putString(ACCESSCODE_ID, code);
                            getSupportLoaderManager().initLoader(SUBREDITS_LOADER_ID, bundleForLoader, MainActivity.this).forceLoad();
                        }
                    }
                }
            }
        }
    }

    @Override
    public Loader onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader(this) {
            String accessCode = args.getString(ACCESSCODE_ID);
            String json = null;
            ArrayList<SubredditCustomObject> subscribedSubreddits;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                signInToReddit.setVisibility(View.GONE);
                loadingBar.setVisibility(View.VISIBLE);
            }

            @Override
            public ArrayList<SubredditCustomObject> loadInBackground() {
                boolean haveToken = NetworkUtils.getSyncAccessToken(accessCode, getContext());
                if (haveToken) {
                    json = NetworkUtils.makeSubredditCall(getApplicationContext());
                }
                if(json != null) {
                    if (!json.matches("")) {
                        subredditDbHelper dbHelper = new subredditDbHelper(getApplicationContext());
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete(TABLE_NAME, null, null); //clear old data for first login
                        subscribedSubreddits = NetworkUtils.extractSubredditsFromJSON(json);
                        databaseUtils.insertSubredditsToDatabase(subscribedSubreddits, getApplicationContext());
                    }
                }
                return subscribedSubreddits;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<SubredditCustomObject>> loader, ArrayList<SubredditCustomObject> data) {
        loadingBar.setVisibility(View.GONE);
        // schedule jobs on first login
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(RefreshTokenJobService.class) // the JobService that will be called
                .setTag("refresh-tag")        // uniquely identifies the job
                .setRecurring(true)
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT) // don't persist past a device reboot
                .setTrigger(Trigger.executionWindow(3000, 3500))// start between 3000 and 3500 seconds from now
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();
        dispatcher.mustSchedule(myJob);

        Intent i = new Intent(MainActivity.this, ArticleFeedActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }
}
