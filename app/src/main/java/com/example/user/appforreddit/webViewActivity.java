package com.example.user.appforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static com.example.user.appforreddit.ArticleFeedActivity.ARTICLE_URL_KEY;

public class webViewActivity extends AppCompatActivity {
    private WebView webview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webview = (WebView) findViewById(R.id.webview);
        Intent lauchingIntent = getIntent();
        String url = lauchingIntent.getStringExtra(ARTICLE_URL_KEY);

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webview.setWebViewClient(new WebViewClient());
        webview.setWebChromeClient(new WebChromeClient());
        if (savedInstanceState == null)
        {
            webview.loadUrl(url);
        }else{
            webview.restoreState(savedInstanceState);
        }
    }
    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
//If there is history, then the canGoBack method will return ‘true’//
            return true;
        }
//If the button that’s been pressed wasn’t the ‘Back’ button, or there’s currently no
//WebView history, then the system should resort to its default behavior and return
//the user to the previous Activity//
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        webview.saveState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        webview.restoreState(savedInstanceState);
    }
}
