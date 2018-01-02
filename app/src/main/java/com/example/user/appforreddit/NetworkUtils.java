package com.example.user.appforreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by user on 03-01-2018.
 */

public class NetworkUtils {
    private static final String ACCESS_TOKEN_URL =
            "https://www.reddit.com/api/v1/access_token";
    private static final String REDIRECT_URI =
            "http://www.example.com/my_redirect";
    private static final String CLIENT_ID = "2XIKITvqkmX-yg";
    private static String ACCESS_SUBREDDITS_URL = "https://oauth.reddit.com/subreddits/mine/subscriber";
    private static String TAG = "RedditMainActivity";
    public static void getAccessToken(String code, final Context context) {
        final SharedPreferences pref = context.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
        OkHttpClient client = new OkHttpClient();
        String authString = CLIENT_ID + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + REDIRECT_URI))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");
                    String refreshToken = data.optString("refresh_token");

                    Log.d(TAG, "Access Token = " + accessToken);
                    //       Toast.makeText(MainActivity.this, accessToken, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Refresh Token = " + refreshToken);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("token",accessToken);
                    edit.putString("refreshToken",refreshToken);
                    edit.commit();
                    // Log.d(TAG, "Flag is set true");
                    makeSubredditCall(context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void makeSubredditCall(Context context){
        OkHttpClient client = new OkHttpClient();
        SharedPreferences pref = context.getSharedPreferences("AppPref",Context.MODE_PRIVATE);
        String token =pref.getString("token", "");
        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "bearer " + token)
                .url(ACCESS_SUBREDDITS_URL)
                .build();

        client.newCall(request).enqueue(new Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                //     tv.setText(json);
                Log.d(TAG, json);
            }
        });
    }
}
