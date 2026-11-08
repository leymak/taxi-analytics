package com.taxianalytics.tracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView statusOverlay;
    private TextView statusAccessibility;
    private EditText editCommission;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("taxi_prefs", Context.MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 70, 50, 50);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("Такси Аналитика");
        title.setTextSize(24);
        title.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        title.setPadding(0, 0, 0, 40);
        layout.addView(title);

        // Поле ввода комиссии
        TextView lblComm = new TextView(this);
        lblComm.setText("Ваша комиссия (Яндекс + Парк), %:");
        lblComm.setTextSize(15);
        layout.addView(lblComm);

        editCommission = new EditText(this);
        editCommission.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        float savedComm = prefs.getFloat("commission_rate", 20.0f);
        editCommission.setText(String.valueOf(savedComm));
        layout.addView(editCommission);

        Button btnSaveComm = new Button(this);
        btnSaveComm.setText("Сохранить комиссию");
        btnSaveComm.setOnClickListener(v -> {
            try {
                float val = Float.parseFloat(editCommission.getText().toString().replace(',', '.'));
                prefs.edit().putFloat("commission_rate", val).apply();
                Toast.makeText(this, "Комиссия " + val + "% сохранена", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(btnSaveComm);

        // Разрешения
        Button btnOverlay = new Button(this);
        btnOverlay.setText("1. Доступ поверх окон");
        btnOverlay.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
        layout.addView(btnOverlay);

        statusOverlay = new TextView(this);
        statusOverlay.setPadding(0, 5, 0, 20);
        layout.addView(statusOverlay);

        Button btnAccess = new Button(this);
        btnAccess.setText("2. Служба (Спец. возможности)");
        btnAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        layout.addView(btnAccess);

        statusAccessibility = new TextView(this);
        statusAccessibility.setPadding(0, 5, 0, 40);
        layout.addView(statusAccessibility);

        // Кнопка запуска
        Button btnStart = new Button(this);
        btnStart.setText("ЗАПУСТИТЬ ВИДЖЕТ");
        btnStart.setBackgroundColor(0xFF2E7D32);
        btnStart.setTextColor(0xFFFFFFFF);
        btnStart.setOnClickListener(v -> {
            // Сохраняем введенный процент перед стартом
            try {
                float val = Float.parseFloat(editCommission.getText().toString().replace(',', '.'));
                prefs.edit().putFloat("commission_rate", val).apply();
            } catch (Exception ignored) {}

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

        // Кнопка остановки
        Button btnStop = new Button(this);
        btnStop.setText("ОСТАНОВИТЬ ВИДЖЕТ");
        btnStop.setBackgroundColor(0xFFC62828);
        btnStop.setTextColor(0xFFFFFFFF);
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(MainActivity.this, FloatingWidgetService.class));
            Toast.makeText(this, "Виджет остановлен", Toast.LENGTH_SHORT).show();
        });
        layout.addView(btnStop);

        setContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean canDrawOverlays = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
        statusOverlay.setText(canDrawOverlays ? "✓ Доступ поверх окон выдан" : "✗ Нет доступа к оверлею");
        statusOverlay.setTextColor(canDrawOverlays ? 0xFF2E7D32 : 0xFFC62828);
        statusAccessibility.setText("Проверьте включение службы Такси Аналитика");
    }
}
