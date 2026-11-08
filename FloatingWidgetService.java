package com.taxianalytics.tracker;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatingWidgetService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    public static TextView tvProfit;
    public static TextView tvDetails;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Создаем компактный интерфейс плашки программно
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 16, 24, 16);

        // Черный матовый фон с закругленными углами
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E6121417"));
        bg.setCornerRadius(24f);
        bg.setStroke(2, Color.parseColor("#334155"));
        layout.setBackground(bg);

        tvProfit = new TextView(this);
        tvProfit.setText("~ 0 ₽ чистыми");
        tvProfit.setTextSize(16);
        tvProfit.setTextColor(Color.parseColor("#10B981")); // Ярко-зеленый
        tvProfit.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvProfit);

        tvDetails = new TextView(this);
        tvDetails.setText("Ожидание заказа...");
        tvDetails.setTextSize(12);
        tvDetails.setTextColor(Color.parseColor("#94A3B8"));
        layout.addView(tvDetails);

        floatingView = layout;

        int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50;
        params.y = 200;

        // Перетаскивание виджета пальцем по экрану
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(floatingView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
    }
}
