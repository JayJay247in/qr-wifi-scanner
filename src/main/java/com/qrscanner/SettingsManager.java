package com.qrscanner;

import java.util.prefs.Preferences;

public class SettingsManager {

    private final Preferences prefs;
    private static final String THEME = "theme";
    private static final String SCAN_INTERVAL = "scan_interval";
    private static final String SAVE_HISTORY = "save_history";

    public SettingsManager() {
        // Creates a unique preference node for this application
        this.prefs = Preferences.userNodeForPackage(QRScannerApp.class);
    }

    // --- Theme Setting ---
    public String getTheme() {
        return prefs.get(THEME, "System Default");
    }

    public void setTheme(String theme) {
        prefs.put(THEME, theme);
    }

    // --- Scan Interval Setting ---
    public long getScanInterval() {
        // Default to 333ms (~3 scans per second)
        return prefs.getLong(SCAN_INTERVAL, 333);
    }

    public void setScanInterval(long intervalMillis) {
        prefs.putLong(SCAN_INTERVAL, intervalMillis);
    }

    // --- History Setting ---
    public boolean isHistorySavingEnabled() {
        return prefs.getBoolean(SAVE_HISTORY, true); // Default to true
    }

    public void setHistorySavingEnabled(boolean enabled) {
        prefs.putBoolean(SAVE_HISTORY, enabled);
    }
}