package com.yadisupri.cekimei;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1001;

    private EditText etImei;
    private Button btnCekImei;
    private Button btnDetailWeb;
    private Button btnCloseWeb;
    private TextView tvHasilImei;
    private WebView webView;
    private String currentImei = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi view
        etImei = findViewById(R.id.etImei);
        btnCekImei = findViewById(R.id.btnCekImei);
        btnDetailWeb = findViewById(R.id.btnDetailWeb);
        btnCloseWeb = findViewById(R.id.btnCloseWeb);
        tvHasilImei = findViewById(R.id.tvHasilImei);
        webView = findViewById(R.id.webView);

        // Setup WebView
        setupWebView();

        // Cek permission untuk mendapatkan IMEI otomatis
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ tidak bisa dapat IMEI otomatis
            Toast.makeText(this, "Masukkan IMEI secara manual", Toast.LENGTH_LONG).show();
        } else {
            // Android 9 ke bawah
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        PERMISSION_REQUEST_READ_PHONE_STATE);
            } else {
                getImeiNumber();
            }
        }

        // Tombol Cek IMEI
        btnCekImei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imei = etImei.getText().toString().trim();
                if (TextUtils.isEmpty(imei)) {
                    Toast.makeText(MainActivity.this, "Masukkan IMEI terlebih dahulu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (imei.length() != 15) {
                    Toast.makeText(MainActivity.this, "IMEI harus 15 digit!", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentImei = imei;
                displayImeiResult(imei);
            }
        });

        // Tombol Detail (buka WebView)
        btnDetailWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(currentImei)) {
                    showWebView(currentImei);
                } else {
                    Toast.makeText(MainActivity.this, "Cek IMEI terlebih dahulu!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Tombol Close WebView
        btnCloseWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideWebView();
            }
        });
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Sembunyikan loading jika diperlukan
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Tetap di WebView untuk semua link
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
    }

    private void getImeiNumber() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String imei = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = telephonyManager.getImei();
                } else {
                    imei = telephonyManager.getDeviceId();
                }
                if (imei != null && !imei.isEmpty()) {
                    etImei.setText(imei);
                    currentImei = imei;
                    displayImeiResult(imei);
                } else {
                    Toast.makeText(this, "Gagal mendapatkan IMEI", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void displayImeiResult(String imei) {
        tvHasilImei.setVisibility(View.VISIBLE);
        tvHasilImei.setText(
                "✅ IMEI Terdeteksi!\n\n" +
                "📱 " + imei + "\n\n" +
                "📌 Klik tombol DETAIL untuk melihat\n" +
                "informasi lengkap tentang perangkat ini"
        );
        btnDetailWeb.setVisibility(View.VISIBLE);
        btnDetailWeb.setText("🌐 DETAIL IMEI");
        
        // Sembunyikan WebView jika terbuka
        hideWebView();
    }

    private void showWebView(String imei) {
        String url = "https://www.imei.info/id/?imei=" + imei;
        
        // Tampilkan WebView
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
        
        // Sembunyikan teks hasil
        tvHasilImei.setVisibility(View.GONE);
        
        // Tampilkan tombol close
        btnCloseWeb.setVisibility(View.VISIBLE);
        
        // Update tombol detail
        btnDetailWeb.setText("🔄 RELOAD");
        btnDetailWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        Toast.makeText(this, "Memuat informasi IMEI...", Toast.LENGTH_SHORT).show();
    }

    private void hideWebView() {
        webView.setVisibility(View.GONE);
        webView.stopLoading();
        btnCloseWeb.setVisibility(View.GONE);
        
        // Kembalikan tombol detail
        btnDetailWeb.setText("🌐 DETAIL");
        btnDetailWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(currentImei)) {
                    showWebView(currentImei);
                }
            }
        });
        
        // Tampilkan kembali teks hasil
        if (!TextUtils.isEmpty(currentImei)) {
            tvHasilImei.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        // Jika WebView terbuka, tutup dulu
        if (webView.getVisibility() == View.VISIBLE) {
            hideWebView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImeiNumber();
            } else {
                Toast.makeText(this, "Izin ditolak. Masukkan IMEI secara manual", Toast.LENGTH_LONG).show();
            }
        }
    }
}