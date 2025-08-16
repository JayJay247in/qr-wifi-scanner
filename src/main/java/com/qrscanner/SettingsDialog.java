package com.qrscanner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsDialog extends JDialog {

    private JComboBox<String> themeComboBox;
    private JSlider scanIntervalSlider;

    public SettingsDialog(JFrame parent) {
        super(parent, "Settings", true); // true for modal
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(450, 300);
        setLocationRelativeTo(getOwner());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", createGeneralPanel());
        tabbedPane.addTab("Camera", createCameraPanel());
        // Placeholder for a future feature from your list
        tabbedPane.addTab("History", new JPanel());

        add(tabbedPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Look and Feel (Theme) Setting
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Application Theme:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        themeComboBox = new JComboBox<>(new String[]{"System Default", "Light (Metal)", "Dark (Nimbus)"});
        panel.add(themeComboBox, gbc);

        // Add a spacer to push everything to the top
        gbc.gridy = 1;
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

        // Scan Interval Setting
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Scan Interval (Scans per second):"), gbc);

        gbc.gridy = 1;
        scanIntervalSlider = new JSlider(1, 10, 3); // Min 1, Max 10, Default 3 scans/sec
        scanIntervalSlider.setMajorTickSpacing(1);
        scanIntervalSlider.setPaintTicks(true);
        scanIntervalSlider.setPaintLabels(true);
        panel.add(scanIntervalSlider, gbc);

        // Add a spacer to push everything to the top
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

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
        // In a future step, this method would actually change the app's behavior.
        // For now, we just print the selected values to confirm it works.
        String selectedTheme = (String) themeComboBox.getSelectedItem();
        int scansPerSecond = scanIntervalSlider.getValue();
        long intervalMillis = 1000 / scansPerSecond;

        System.out.println("--- Settings Applied ---");
        System.out.println("Theme: " + selectedTheme);
        System.out.println("Scans per second: " + scansPerSecond + " (~" + intervalMillis + "ms interval)");
        System.out.println("------------------------");

        // Example of how you would notify the main app in the future:
        // ((QRScannerApp) getOwner()).applyTheme(selectedTheme);
        // ((QRScannerApp) getOwner()).getCameraPanel().setScanInterval(intervalMillis);

        JOptionPane.showMessageDialog(this, "Settings applied (check console output).", "Settings", JOptionPane.INFORMATION_MESSAGE);
    }
}