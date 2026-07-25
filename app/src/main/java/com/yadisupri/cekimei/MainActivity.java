package com.yadisupri.cekimei;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1001;

    private EditText etImei;
    private Button btnCekImei;
    private TextView tvHasilImei;
    private Button btnBukaLink;
    private CardView cardHasil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi view
        etImei = findViewById(R.id.etImei);
        btnCekImei = findViewById(R.id.btnCekImei);
        tvHasilImei = findViewById(R.id.tvHasilImei);
        btnBukaLink = findViewById(R.id.btnBukaLink);
        cardHasil = findViewById(R.id.cardHasil);

        // Cek permission untuk Android 10 ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermission();
        } else {
            // Untuk Android 9 ke bawah, permission READ_PHONE_STATE diperlukan
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
                displayImeiResult(imei);
            }
        });

        // Tombol Buka Link
        btnBukaLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imei = etImei.getText().toString().trim();
                if (!TextUtils.isEmpty(imei)) {
                    String url = "https://www.imei.info/id/?imei=" + imei;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }
        });
    }

    private void checkPermission() {
        // Untuk Android 10+, akses IMEI memerlukan permission khusus
        // Kita hanya akan menampilkan tombol manual input
        Toast.makeText(this, "Masukkan IMEI secara manual", Toast.LENGTH_LONG).show();
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
                    displayImeiResult(imei);
                } else {
                    Toast.makeText(this, "Gagal mendapatkan IMEI", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void displayImeiResult(String imei) {
        tvHasilImei.setVisibility(View.VISIBLE);
        tvHasilImei.setText("IMEI: " + imei + "\n\n✅ IMEI terdeteksi\n📱 Klik tombol di bawah untuk detail");
        btnBukaLink.setVisibility(View.VISIBLE);
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