package com.qrscanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ScanHistoryManager {

    private static final int MAX_HISTORY_SIZE = 100;
    private final LinkedList<ScanHistoryItem> history;
    private final SettingsManager settingsManager;

    public ScanHistoryManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        this.history = new LinkedList<>();
    }

    public void addHistoryItem(String content, String type) {
        if (!settingsManager.isHistorySavingEnabled()) {
            return; // Do nothing if history is disabled
        }
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.removeLast();
        }
        history.addFirst(new ScanHistoryItem(content, type));
    }

    public List<ScanHistoryItem> getHistory() {
        return history;
    }

    public void clearHistory() {
        history.clear();
    }

    public static class ScanHistoryItem {
        private final String content;
        private final String type;
        private final String timestamp;

        public ScanHistoryItem(String content, String type) {
            this.content = content;
            this.type = type;
            this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        }

        public String getContent() {
            return content;
        }

        public String getType() {
            return type;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}