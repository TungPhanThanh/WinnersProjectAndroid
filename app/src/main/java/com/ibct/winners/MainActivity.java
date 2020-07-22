package com.ibct.winners;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ViewDialog mAlertDialogExit = new ViewDialog();

    private ProgressBar progressBar;

    private Context mContext;
    private WebView mWebView;

    private WebView mWebViewPop;
    private AlertDialog builder;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SetupFirebase();

        mWebView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar_webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setAllowContentAccess(true);
        //webSettings.setAllowFileAccess(true);
        //webSettings.setDatabaseEnabled(true);

        mWebView.getSettings().setSavePassword(true);
        mWebView.getSettings().setSaveFormData(true);
        mWebView.setWebViewClient(new UriWebViewClient());
        mWebView.setWebChromeClient(new UriChromeClient());
        mWebView.getSettings().setSavePassword(true);
        mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 9; Pixel) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.93 Mobile Safari/537.36\\t");

        progressBar.setMax(100);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        mWebView.loadUrl("https://v-shopping.vn");
        mContext = this.getApplicationContext();

    }

    private class UriWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            Log.d("onReceivedSslError", "onReceivedSslError");
            //super.onReceivedSslError(view, handler, error);
        }

        @SuppressLint("IntentReset")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            Log.d("shouldOverrideUrlLoading", url);
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            } else if (url.startsWith("https://www.m.me/")) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(),
                            "Oups!Can't open Facebook messenger right now. Please try again later.",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            } else {
                if (builder.isShowing() || mWebViewPop.isShown()) {
                    mWebViewPop.destroy();
                    builder.dismiss();
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(),
                            "Oops! Something went wrong.",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (mWebView != null) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                mAlertDialogExit.showDialog(this);
            }
        } else {
            mAlertDialogExit.showDialog(this);
        }
    }

    class UriChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setProgress(newProgress);
        }

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            mWebViewPop = new WebView(mContext);
            mWebViewPop.setVerticalScrollBarEnabled(false);
            mWebViewPop.setHorizontalScrollBarEnabled(false);
            mWebViewPop.setWebViewClient(new UriWebViewClient());
            mWebViewPop.setWebChromeClient(new UriChromeClient());
            mWebViewPop.getSettings().setJavaScriptEnabled(true);
            mWebViewPop.getSettings().setSavePassword(true);
            mWebViewPop.getSettings().setSaveFormData(true);

            builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).create();

            builder.setTitle("");
            builder.setView(mWebViewPop);

            builder.setButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mWebViewPop.destroy();
                    dialog.dismiss();
                }
            });

            builder.show();
            builder.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mWebViewPop, true);

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebViewPop);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            //Toast.makeText(mContext,"onCloseWindow called",Toast.LENGTH_SHORT).show();
            try {
                mWebViewPop.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                builder.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class ViewDialog {

        void showDialog(Activity activity) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(true);
            FrameLayout mDialogNo = dialog.findViewById(R.id.frmNo);
            mDialogNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            FrameLayout mDialogOk = dialog.findViewById(R.id.frmOk);
            mDialogOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                    System.exit(0);
                }
            });

            dialog.show();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void SetupFirebase() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
    }
}
