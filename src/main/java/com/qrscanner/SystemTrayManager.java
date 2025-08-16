package com.qrscanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class SystemTrayManager {
    private final QRScannerApp parentApp;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private boolean traySupported;
    private Timer animationTimer;

    public SystemTrayManager(QRScannerApp parentApp) {
        this.parentApp = parentApp;
        initializeSystemTray();
    }

    private void initializeSystemTray() {
        if (!SystemTray.isSupported()) {
            traySupported = false;
            return;
        }
        traySupported = true;
        systemTray = SystemTray.getSystemTray();
        trayIcon = new TrayIcon(createTrayIcon(), "QR WiFi Scanner", createTrayPopupMenu());
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) restoreApplication();
            }
        });
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            traySupported = false;
        }
    }

    private Image createTrayIcon() {
        try {
            return Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon.png")).getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            return createProgrammaticIcon();
        }
    }

    private Image createProgrammaticIcon() {
        int size = 16;
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(1, 1, 5, 5);
        g2d.fillRect(10, 1, 5, 5);
        g2d.fillRect(1, 10, 5, 5);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(2, 2, 3, 3);
        g2d.fillRect(11, 2, 3, 3);
        g2d.fillRect(2, 11, 3, 3);
        g2d.dispose();
        return icon;
    }

    private PopupMenu createTrayPopupMenu() {
        PopupMenu popup = new PopupMenu();
        MenuItem showHideItem = new MenuItem("Show/Hide Application");
        showHideItem.addActionListener(e -> restoreApplication());
        popup.add(showHideItem);
        popup.addSeparator();
        MenuItem scanItem = new MenuItem("Start/Stop Scanning");
        scanItem.addActionListener(e -> toggleScanning());
        popup.add(scanItem);
        popup.addSeparator();
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.addActionListener(e -> showAboutFromTray());
        popup.add(aboutItem);
        popup.addSeparator();
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());
        popup.add(exitItem);
        return popup;
    }

    private void restoreApplication() { if (parentApp != null) parentApp.restoreFromTray(); }
    private void toggleScanning() { if (parentApp != null) parentApp.toggleScanning(); }
    private void showAboutFromTray() { showTrayMessage("QR WiFi Scanner v1.0.0", "About QR Scanner", TrayIcon.MessageType.INFO); }
    private void exitApplication() { if (parentApp != null) parentApp.dispatchEvent(new WindowEvent(parentApp, WindowEvent.WINDOW_CLOSING)); }

    /**
     * CORRECTION: Re-added this convenient overload to fix the compile errors.
     */
    public void showTrayMessage(String message, String title) {
        showTrayMessage(message, title, TrayIcon.MessageType.INFO);
    }

    public void showTrayMessage(String message, String title, TrayIcon.MessageType messageType) {
        if (traySupported) trayIcon.displayMessage(title, message, messageType);
    }
    public void showTrayMessage(String message) { showTrayMessage(message, "QR WiFi Scanner", TrayIcon.MessageType.INFO); }
    public boolean isSystemTraySupported() { return traySupported; }

    public void cleanup() {
        if (traySupported) {
            setTrayIconAnimated(false);
            systemTray.remove(trayIcon);
        }
    }
    
    public void setTrayIconAnimated(boolean animated) {
        if (!traySupported) return;
        if (animationTimer != null) animationTimer.stop();
        if (animated) {
            animationTimer = new Timer(500, e -> trayIcon.setImage(createAnimatedIcon()));
            animationTimer.start();
        } else {
            trayIcon.setImage(createTrayIcon());
        }
    }
    
    private Image createAnimatedIcon() {
        int size = 16;
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0, 150, 255));
        g2d.fillRect(1, 1, 5, 5);
        g2d.fillRect(10, 1, 5, 5);
        g2d.fillRect(1, 10, 5, 5);
        g2d.setColor(new Color(0, 255, 0, 150));
        g2d.fillRect(0, (int) ((System.currentTimeMillis() / 200) % size), size, 2);
        g2d.dispose();
        return icon;
    }
}