package com.yandepderche.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String SITE_URL = "https://sites.google.com/view/yandepderche";

    private DrawerLayout drawer;
    private NavigationView nav;
    private Toolbar toolbar;
    private WebView webView;
    private View homeContainer, loadingView, offlineView, settingsView, aboutView;
    private boolean loadFailed = false;
    private boolean onHome = true;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer);
        toggle.getDrawerArrowDrawable().setColor(0xFFFFFFFF);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        nav = findViewById(R.id.nav_view);
        nav.setNavigationItemSelectedListener(this);
        nav.setCheckedItem(R.id.nav_home);

        homeContainer = findViewById(R.id.home_container);
        loadingView = findViewById(R.id.loading_view);
        offlineView = findViewById(R.id.offline_view);
        settingsView = findViewById(R.id.settings_view);
        aboutView = findViewById(R.id.about_view);
        webView = findViewById(R.id.webview);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadFailed = false;
                offlineView.setVisibility(View.GONE);
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingView.setVisibility(View.GONE);
                if (loadFailed) {
                    showOffline();
                }
            }

            // Esasy sahypanyň ýalňyşlygy (ähli Android wersiýalarynda işleýär)
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                loadFailed = true;
                showOffline();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                String host = uri.getHost() == null ? "" : uri.getHost();
                boolean web = "http".equals(scheme) || "https".equals(scheme);
                boolean inside = host.endsWith("google.com") || host.endsWith("gstatic.com")
                        || host.endsWith("googleusercontent.com");
                if (web && inside) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, R.string.no_browser, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        findViewById(R.id.retry_button).setOnClickListener(v -> loadSite());

        findViewById(R.id.clear_cache_button).setOnClickListener(v -> {
            webView.clearCache(true);
            Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.open_browser_button).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SITE_URL)));
            } catch (Exception e) {
                Toast.makeText(this, R.string.no_browser, Toast.LENGTH_SHORT).show();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else if (!onHome) {
                    showSection(R.id.nav_home);
                    nav.setCheckedItem(R.id.nav_home);
                } else if (webView.canGoBack() && !loadFailed) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });

        showSection(R.id.nav_home);
        loadSite();
    }

    private void loadSite() {
        offlineView.setVisibility(View.GONE);
        if (!isOnline()) {
            loadFailed = true;
            showOffline();
            return;
        }
        loadFailed = false;
        loadingView.setVisibility(View.VISIBLE);
        webView.loadUrl(SITE_URL);
    }

    private void showOffline() {
        loadingView.setVisibility(View.GONE);
        offlineView.setVisibility(View.VISIBLE);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void showSection(int id) {
        onHome = (id == R.id.nav_home);
        homeContainer.setVisibility(id == R.id.nav_home ? View.VISIBLE : View.GONE);
        settingsView.setVisibility(id == R.id.nav_settings ? View.VISIBLE : View.GONE);
        aboutView.setVisibility(id == R.id.nav_about ? View.VISIBLE : View.GONE);
        if (id == R.id.nav_settings) {
            toolbar.setTitle(R.string.menu_settings);
        } else if (id == R.id.nav_about) {
            toolbar.setTitle(R.string.menu_about);
        } else {
            toolbar.setTitle(R.string.menu_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        showSection(item.getItemId());
        drawer.closeDrawers();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}
