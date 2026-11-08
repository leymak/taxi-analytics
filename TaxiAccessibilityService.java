package com.taxianalytics.tracker;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaxiAccessibilityService extends AccessibilityService {

    private final Pattern patternKm = Pattern.compile("(\\d+[.,]?\\d*)\\s*км");
    private final Pattern patternMin = Pattern.compile("(\\d+)\\s*мин");

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // Ищем текст заказа на экране
        parseScreenNodes(rootNode);
    }

    private void parseScreenNodes(AccessibilityNodeInfo node) {
        if (node == null) return;

        if (node.getText() != null) {
            String text = node.getText().toString();

            Matcher matcherKm = patternKm.matcher(text);
            Matcher matcherMin = patternMin.matcher(text);

            if (matcherKm.find() && matcherMin.find()) {
                double km = Double.parseDouble(matcherKm.group(1).replace(",", "."));
                int min = Integer.parseInt(matcherMin.group(1));

                calculateAndDisplay(km, min);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            parseScreenNodes(node.getChild(i));
        }
    }

    private void calculateAndDisplay(double km, int minutes) {
        // Базовый тариф СПб: 180 руб посадка + 15 руб/км + 7 руб/мин
        double gross = 180.0 + (km * 15.0) + (minutes * 7.0);

        // Вычитаем комиссии и бензин:
        // 21% Яндекс + 4% налог/парк = 25% вычетов
        double afterCommissions = gross * 0.75;
        // Расход топлива: ~8 руб на 1 км
        double fuelCost = km * 8.0;

        final double netProfit = Math.max(0, afterCommissions - fuelCost);

        if (FloatingWidgetService.tvProfit != null) {
            FloatingWidgetService.tvProfit.post(() -> {
                FloatingWidgetService.tvProfit.setText(String.format("~ %.0f ₽ чистыми", netProfit));
                FloatingWidgetService.tvDetails.setText(String.format("%.1f км • %d мин", km, minutes));
            });
        }
    }

    @Override
    public void onInterrupt() {}
}
