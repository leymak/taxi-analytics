package com.taxianalytics.tracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
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
    private static FloatingWidgetService instance;

    public static FloatingWidgetService getInstance() {
        return instance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startForegroundNotification();
        initFloatingWidget();
    }

    private void startForegroundNotification() {
        String channelId = "taxi_tracker_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Такси Аналитика", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle("Такси Аналитика")
                    .setContentText("Виджет активен поверх Яндекс Про")
                    .setSmallIcon(android.R.drawable.ic_menu_compass)
                    .build();
            startForeground(101, notification);
        }
    }

    private void initFloatingWidget() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#E6212121"));
        layout.setPadding(35, 25, 35, 25);

        TextView title = new TextView(this);
        title.setText("АНАЛИТИКА");
        title.setTextColor(Color.parseColor("#FFD600"));
        title.setTextSize(13);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setId(View.generateViewId());
        info.setTag("widget_text");
        info.setText("Ожидание заказа в Яндекс Про...");
        info.setTextColor(Color.WHITE);
        info.setTextSize(15);
        layout.addView(info);

        int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 80;
        params.y = 200;

        layout.setOnTouchListener(new View.OnTouchListener() {
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

        floatingView = layout;
        windowManager.addView(floatingView, params);
    }

    public void updateData(String orderText) {
        if (floatingView != null) {
            TextView info = floatingView.findViewWithTag("widget_text");
            if (info != null) {
                info.setText(orderText);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
        }
    }
}
