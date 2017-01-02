package com.jackq.funfurniture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import org.rajawali3d.loader.SceneModel;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    static final String TAG = "WEB_VIEW";
    Map<String, String> header = new HashMap<>();
    WebView webview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button loginGoogleButton = (Button) findViewById(R.id.btn_login_google);
        Button loginFacebookButton = (Button) findViewById(R.id.btn_login_facebook);
        header.put("ANDROID_CLIENT_AUTH", "FUN_F");
        webview = (WebView) findViewById(R.id.webview_login);
        webview.getSettings().setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient(){
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                Log.d(TAG, url);
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url, header);
                Log.d(TAG, "Insert header to " + url);
                return true;
            }

        });

        loginGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginGoogle();
            }
        });

        loginFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginFacebook();
            }
        });
    }

    private void loginFacebook() {
        webview.setVisibility(View.VISIBLE);
        webview.loadUrl(getResources().getString(R.string.APIHostName) + "auth/facebook");
    }

    private void loginGoogle() {
        webview.setVisibility(View.VISIBLE);
        webview.loadUrl(getResources().getString(R.string.APIHostName) + "auth/google");
    }

    @Override
    public void onBackPressed() {
        if(webview.getVisibility() == View.VISIBLE){
            webview.clearHistory();
            webview.loadUrl("about:blank");
            webview.setVisibility(View.GONE);
        }else{
            finish();
        }
    }
}
