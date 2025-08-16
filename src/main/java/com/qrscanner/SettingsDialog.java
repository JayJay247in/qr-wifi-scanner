package com.qrscanner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsDialog extends JDialog {

    private final SettingsManager settingsManager;
    private JComboBox<String> themeComboBox;
    private JSlider scanIntervalSlider;
    private JCheckBox saveHistoryCheckBox;

    public SettingsDialog(JFrame parent, SettingsManager settingsManager) {
        super(parent, "Settings", true);
        this.settingsManager = settingsManager;
        initializeUI();
        loadSettings();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(450, 300);
        setLocationRelativeTo(getOwner());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", createGeneralPanel());
        tabbedPane.addTab("Camera", createCameraPanel());
        tabbedPane.addTab("History", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadSettings() {
        // General
        themeComboBox.setSelectedItem(settingsManager.getTheme());
        // Camera
        long interval = settingsManager.getScanInterval();
        scanIntervalSlider.setValue((int) (1000 / interval));
        // History
        saveHistoryCheckBox.setSelected(settingsManager.isHistorySavingEnabled());
    }

    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Application Theme:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        themeComboBox = new JComboBox<>(new String[]{"System Default", "Light (Metal)", "Dark (Nimbus)"});
        panel.add(themeComboBox, gbc);

        gbc.gridy++;
        panel.add(new JLabel("<html><i>(Requires restart to take full effect)</i></html>"), gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel createCameraPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Scan Frequency (Scans per second):"), gbc);

        gbc.gridy++;
        scanIntervalSlider = new JSlider(1, 10, 3);
        scanIntervalSlider.setMajorTickSpacing(1);
        scanIntervalSlider.setPaintTicks(true);
        scanIntervalSlider.setPaintLabels(true);
        panel.add(scanIntervalSlider, gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        saveHistoryCheckBox = new JCheckBox("Save Scan History");
        panel.add(saveHistoryCheckBox);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            applySettings();
            setVisible(false);
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> setVisible(false));

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applySettings());

        panel.add(okButton);
        panel.add(cancelButton);
        panel.add(applyButton);

        return panel;
    }

    private void applySettings() {
        String originalTheme = settingsManager.getTheme();
        String selectedTheme = (String) themeComboBox.getSelectedItem();

        // Save General settings
        settingsManager.setTheme(selectedTheme);

        // Save Camera settings
        int scansPerSecond = scanIntervalSlider.getValue();
        long intervalMillis = 1000 / Math.max(1, scansPerSecond);
        settingsManager.setScanInterval(intervalMillis);

        // Save History settings
        settingsManager.setHistorySavingEnabled(saveHistoryCheckBox.isSelected());

        // Apply settings that can be changed live
        QRScannerApp mainApp = (QRScannerApp) getOwner();
        mainApp.getCameraPanel().setScanInterval(intervalMillis);

        JOptionPane.showMessageDialog(this, "Settings applied.", "Settings", JOptionPane.INFORMATION_MESSAGE);

        if (!originalTheme.equals(selectedTheme)) {
            JOptionPane.showMessageDialog(this, "A restart is required for the theme change to take full effect.", "Restart Required", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}