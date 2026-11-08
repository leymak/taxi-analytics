package com.taxianalytics.tracker;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaxiAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        StringBuilder sb = new StringBuilder();
        collectText(rootNode, sb);
        String allScreenText = sb.toString();

        parseAndDisplay(allScreenText);
    }

    private void collectText(AccessibilityNodeInfo node, StringBuilder sb) {
        if (node == null) return;
        if (node.getText() != null) {
            sb.append(node.getText().toString()).append(" ");
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectText(node.getChild(i), sb);
        }
    }

    private void parseAndDisplay(String text) {
        // Поиск суммы (например: "450 ₽" или "1200 ₽")
        Pattern pricePattern = Pattern.compile("(\\d{2,5})[\\s\u00a0]?₽");
        Matcher priceMatcher = pricePattern.matcher(text);

        // Поиск расстояния (например: "7.2 км" или "7,2 км")
        Pattern distPattern = Pattern.compile("(\\d+[,.]?\\d*)[\\s\u00a0]?км");
        Matcher distMatcher = distPattern.matcher(text);

        // Поиск времени поездки (например: "25 мин" или "1 ч 15 мин")
        Pattern timePattern = Pattern.compile("(\\d+)[\\s\u00a0]?мин");
        Matcher timeMatcher = timePattern.matcher(text);

        if (priceMatcher.find()) {
            double price = Double.parseDouble(priceMatcher.group(1));

            // Читаем сохраненную комиссию водителя из настроек
            SharedPreferences prefs = getSharedPreferences("taxi_prefs", Context.MODE_PRIVATE);
            float commissionRate = prefs.getFloat("commission_rate", 20.0f);

            double cleanIncome = price * (1.0 - (commissionRate / 100.0));

            // Дистанция
            double km = 0;
            if (distMatcher.find()) {
                try {
                    km = Double.parseDouble(distMatcher.group(1).replace(',', '.'));
                } catch (Exception ignored) {}
            }

            // Время и расчет дохода в час
            int minutes = 0;
            if (timeMatcher.find()) {
                try {
                    minutes = Integer.parseInt(timeMatcher.group(1));
                } catch (Exception ignored) {}
            }

            // Расчет часовой ставки (₽ / час)
            int rublesPerHour = 0;
            if (minutes > 0) {
                rublesPerHour = (int) Math.round(cleanIncome / (minutes / 60.0));
            }

            // Расчет ставки за километр (₽ / км)
            int rublesPerKm = 0;
            if (km > 0) {
                rublesPerKm = (int) Math.round(cleanIncome / km);
            }

            StringBuilder out = new StringBuilder();
            out.append("Заказ: ").append((int) price).append(" ₽  (чистыми: ")
               .append((int) Math.round(cleanIncome)).append(" ₽)\n");

            if (rublesPerHour > 0) {
                out.append("⏱ Доход в час: ").append(rublesPerHour).append(" ₽/ч\n");
            }
            if (rublesPerKm > 0) {
                out.append("📍 За километр: ").append(rublesPerKm).append(" ₽/км\n");
            }
            out.append("Параметры: ").append(km > 0 ? km + " км" : "~ км")
               .append(" | ").append(minutes > 0 ? minutes + " мин" : "~ мин")
               .append(" (-").append((int) commissionRate).append("%)");

            FloatingWidgetService service = FloatingWidgetService.getInstance();
            if (service != null) {
                service.updateData(out.toString());
            }
        }
    }

    @Override
    public void onInterrupt() {}
}
