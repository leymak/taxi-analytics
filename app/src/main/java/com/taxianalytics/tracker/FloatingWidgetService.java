package com.taxianalytics.tracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.widget.RelativeLayout;
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
                    .setContentText("Виджет активен")
                    .setSmallIcon(android.R.drawable.ic_menu_compass)
                    .build();
            startForeground(101, notification);
        }
    }

    private void initFloatingWidget() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Главный контейнер со скругленными углами и полупрозрачным фоном
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#EE1E1E1E"));
        bg.setCornerRadius(24f);
        bg.setStroke(2, Color.parseColor("#444444"));
        mainLayout.setBackground(bg);
        mainLayout.setPadding(30, 20, 30, 25);

        // Шапка: заголовок и кнопка закрытия [✕]
        RelativeLayout header = new RelativeLayout(this);

        TextView title = new TextView(this);
        title.setText("АНАЛИТИКА ЗАКАЗА");
        title.setTextColor(Color.parseColor("#FFD600"));
        title.setTextSize(12);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(title);

        TextView btnClose = new TextView(this);
        btnClose.setText(" ✕ ");
        btnClose.setTextColor(Color.parseColor("#AAAAAA"));
        btnClose.setTextSize(16);
        btnClose.setPadding(20, 0, 0, 0);
        RelativeLayout.LayoutParams closeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        closeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        btnClose.setLayoutParams(closeParams);
        btnClose.setOnClickListener(v -> stopSelf());
        header.addView(btnClose);

        mainLayout.addView(header);

        // Текстовый блок расчета
        TextView info = new TextView(this);
        info.setTag("widget_text");
        info.setText("Ожидание карточки заказа...");
        info.setTextColor(Color.WHITE);
        info.setTextSize(14);
        info.setPadding(0, 10, 0, 0);
        mainLayout.addView(info);

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
        params.x = 60;
        params.y = 220;

        mainLayout.setOnTouchListener(new View.OnTouchListener() {
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

        floatingView = mainLayout;
        windowManager.addView(floatingView, params);
    }

    public void updateData(String text) {
        if (floatingView != null) {
            TextView info = floatingView.findViewWithTag("widget_text");
            if (info != null) {
                info.setText(text);
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
