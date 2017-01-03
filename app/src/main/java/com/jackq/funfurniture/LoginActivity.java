package com.jackq.funfurniture;

import android.support.design.widget.Snackbar;
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

import com.google.gson.reflect.TypeToken;
import com.jackq.funfurniture.user.UserAuth;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.rajawali3d.loader.SceneModel;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements FutureCallback<UserAuth> {

    static final String TAG = "WEB_VIEW";
    static final String HEADER_NAME = "ANDROID_CLIENT_AUTH";
    static final String HEADER_VALUE = "FUN_F";
    Map<String, String> header = new HashMap<>();
    WebView webview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button loginGoogleButton = (Button) findViewById(R.id.btn_login_google);
        Button loginFacebookButton = (Button) findViewById(R.id.btn_login_facebook);
        header.put(HEADER_NAME, HEADER_VALUE);
        webview = (WebView) findViewById(R.id.webview_login);
        webview.getSettings().setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient(){
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "Insert header to " + url);
                if(url.startsWith(getResources().getString(R.string.APIHostName) + "auth/mobileAuth/")){
                    // Perform this with Ion to get JSON result
                    Ion.with(LoginActivity.this).load(url).addHeader(HEADER_NAME, HEADER_NAME).as(new TypeToken<UserAuth>(){}).setCallback(LoginActivity.this);
                    view.loadUrl(getResources().getString(R.string.APIHostName) + "auth/mobileAuthenticating");
                    return true;
                }else{
                    view.loadUrl(url, header);
                }
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

    /**
     * On authentication result loading finish
     */
    @Override
    public void onCompleted(Exception e, UserAuth result) {
        Log.d(TAG, result.toString());
        Snackbar.make(this.webview, "Authentication Success", Snackbar.LENGTH_LONG).setAction("OK", null).show();

    }
}
