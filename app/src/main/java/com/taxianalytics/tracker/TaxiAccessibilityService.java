package com.taxianalytics.tracker;

import android.accessibilityservice.AccessibilityService;
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
            sb.append(node.getText().toString()).append("\n");
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectText(node.getChild(i), sb);
        }
    }

    private void parseAndDisplay(String text) {
        // Поиск цены заказа (например: "450 ₽")
        Pattern pricePattern = Pattern.compile("(\\d+)[\\s\u00a0]?₽");
        Matcher priceMatcher = pricePattern.matcher(text);

        // Поиск расстояния (например: "5.4 км" или "5,4 км")
        Pattern distPattern = Pattern.compile("(\\d+[,.]?\\d*)[\\s\u00a0]?км");
        Matcher distMatcher = distPattern.matcher(text);

        if (priceMatcher.find()) {
            String price = priceMatcher.group(1);
            String dist = distMatcher.find() ? distMatcher.group(1) : "?";

            String result = "Заказ: " + price + " ₽ | " + dist + " км\nКомиссия ~22%: " 
                    + Math.round(Integer.parseInt(price) * 0.78) + " ₽ на руки";

            FloatingWidgetService service = FloatingWidgetService.getInstance();
            if (service != null) {
                service.updateData(result);
            }
        }
    }

    @Override
    public void onInterrupt() {}
}
