package com.example.user.appforreddit;

import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by Akshay on 05-01-2018.
 */

public class RefreshTokenJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        // NetworkUtils.refreshAccessToken(getApplicationContext());
        new refreshToken().execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    private class refreshToken extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            NetworkUtils.refreshSyncAccessToken(getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            jobFinished(null, true);
        }
    }
}
