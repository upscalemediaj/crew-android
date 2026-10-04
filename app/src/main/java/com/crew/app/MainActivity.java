package com.crew.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String PREFS = "crew_prefs";
    private static final String KEY_SITE = "crew_site";
    private static final int MEDIA_PERMISSION_REQUEST = 41;

    private WebView webView;
    private TextView titleView;
    private SharedPreferences prefs;
    private PermissionRequest pendingPermissionRequest;

    private final Set<String> protectedStreamingHosts = new HashSet<>(Arrays.asList(
            "netflix.com", "www.netflix.com",
            "primevideo.com", "www.primevideo.com",
            "disneyplus.com", "www.disneyplus.com",
            "max.com", "www.max.com",
            "hulu.com", "www.hulu.com",
            "tv.apple.com",
            "peacocktv.com", "www.peacocktv.com",
            "paramountplus.com", "www.paramountplus.com"
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        buildUi();
        configureWebView();
        String site = prefs.getString(KEY_SITE, "");
        if (site == null || site.isBlank()) showSiteSetup(true);
        else webView.loadUrl(site);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(7, 9, 16));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(8), dp(7), dp(8), dp(7));
        bar.setBackgroundColor(Color.rgb(12, 15, 24));

        TextView mark = new TextView(this);
        mark.setText("C");
        mark.setTextColor(Color.WHITE);
        mark.setTextSize(18);
        mark.setGravity(Gravity.CENTER);
        mark.setBackgroundColor(Color.rgb(124, 58, 237));
        bar.addView(mark, new LinearLayout.LayoutParams(dp(36), dp(36)));

        titleView = new TextView(this);
        titleView.setText("  Crew");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(16);
        titleView.setSingleLine(true);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        bar.addView(titleView, titleLp);

        Button back = toolbarButton("‹");
        back.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        bar.addView(back);

        Button reload = toolbarButton("↻");
        reload.setOnClickListener(v -> webView.reload());
        bar.addView(reload);

        Button home = toolbarButton("⌂");
        home.setOnClickListener(v -> {
            String site = prefs.getString(KEY_SITE, "");
            if (site != null && !site.isBlank()) webView.loadUrl(site);
        });
        bar.addView(home);

        Button settings = toolbarButton("⋮");
        settings.setOnClickListener(v -> showSiteSetup(false));
        bar.addView(settings);

        root.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        webView = new WebView(this);
        root.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
    }

    private Button toolbarButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(18);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setPadding(0,0,0,0);
        b.setMinWidth(0); b.setMinimumWidth(0);
        b.setMinHeight(0); b.setMinimumHeight(0);
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(42), dp(42)));
        return b;
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setSupportMultipleWindows(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (isProtectedProvider(uri)) {
                    openExternally(uri);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                titleView.setText("  " + (view.getTitle() == null || view.getTitle().isBlank() ? "Crew" : view.getTitle()));
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    pendingPermissionRequest = request;
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        grantWebPermissions(request);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, MEDIA_PERMISSION_REQUEST);
                    }
                });
            }
        });
    }

    private boolean isProtectedProvider(Uri uri) {
        String host = uri.getHost();
        if (host == null) return false;
        host = host.toLowerCase();
        for (String item : protectedStreamingHosts) {
            if (host.equals(item) || host.endsWith("." + item)) return true;
        }
        return false;
    }

    private void openExternally(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            Toast.makeText(this, "Opened protected streaming service in its supported browser/app", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(this, "No compatible browser/app found", Toast.LENGTH_LONG).show();
        }
    }

    private void grantWebPermissions(PermissionRequest request) {
        if (request == null) return;
        request.grant(request.getResources());
        pendingPermissionRequest = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MEDIA_PERMISSION_REQUEST && pendingPermissionRequest != null) {
            boolean ok = true;
            for (int result : grantResults) if (result != PackageManager.PERMISSION_GRANTED) ok = false;
            if (ok) grantWebPermissions(pendingPermissionRequest);
            else { pendingPermissionRequest.deny(); pendingPermissionRequest = null; }
        }
    }

    private void showSiteSetup(boolean required) {
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("https://crew.example.com");
        input.setText(prefs.getString(KEY_SITE, ""));
        input.setSelectAllOnFocus(true);
        int pad = dp(20);
        LinearLayout box = new LinearLayout(this);
        box.setPadding(pad, dp(5), pad, 0);
        box.addView(input, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(required ? "Connect Crew" : "Crew website")
                .setMessage("Enter the HTTPS address of your Crew website. You only need to do this once.")
                .setView(box)
                .setPositiveButton("Save", null);
        if (!required) b.setNegativeButton("Cancel", null);
        AlertDialog d = b.create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = normalizeSite(input.getText().toString());
            if (url == null) {
                input.setError("Enter a valid HTTPS URL");
                return;
            }
            prefs.edit().putString(KEY_SITE, url).apply();
            d.dismiss();
            webView.loadUrl(url);
        }));
        d.setCancelable(!required);
        d.show();
    }

    private String normalizeSite(String raw) {
        try {
            String v = raw == null ? "" : raw.trim();
            if (!v.startsWith("https://")) return null;
            URI uri = URI.create(v);
            if (uri.getHost() == null || uri.getHost().isBlank()) return null;
            return v.endsWith("/") ? v : v + "/";
        } catch (Exception e) { return null; }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
