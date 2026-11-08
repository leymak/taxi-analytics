package com.taxianalytics.tracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView statusOverlay;
    private TextView statusAccessibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 100, 60, 60);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("Такси Аналитика");
        title.setTextSize(24);
        title.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        title.setPadding(0, 0, 0, 60);
        layout.addView(title);

        Button btnOverlay = new Button(this);
        btnOverlay.setText("1. Разрешить поверх окон");
        btnOverlay.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
        layout.addView(btnOverlay);

        statusOverlay = new TextView(this);
        statusOverlay.setPadding(0, 10, 0, 40);
        layout.addView(statusOverlay);

        Button btnAccess = new Button(this);
        btnAccess.setText("2. Включить службу (Яндекс Про)");
        btnAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        layout.addView(btnAccess);

        statusAccessibility = new TextView(this);
        statusAccessibility.setPadding(0, 10, 0, 60);
        layout.addView(statusAccessibility);

        Button btnStart = new Button(this);
        btnStart.setText("ЗАПУСТИТЬ ВИДЖЕТ");
        btnStart.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, FloatingWidgetService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "Виджет запущен", Toast.LENGTH_SHORT).show();
            finish();
        });
        layout.addView(btnStart);

        setContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean canDrawOverlays = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
        statusOverlay.setText(canDrawOverlays ? "✓ Доступ поверх окон выдан" : "✗ Требуется разрешение на оверлей");
        statusOverlay.setTextColor(canDrawOverlays ? 0xFF2E7D32 : 0xFFC62828);
        statusAccessibility.setText("Убедитесь, что служба включена в Спец. возможностях");
    }
}
