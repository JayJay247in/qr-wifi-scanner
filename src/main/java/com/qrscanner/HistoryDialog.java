package com.qrscanner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HistoryDialog extends JDialog {

    private final ScanHistoryManager historyManager;
    private final DefaultTableModel tableModel;

    public HistoryDialog(JFrame parent, ScanHistoryManager historyManager) {
        super(parent, "Scan History", true);
        this.historyManager = historyManager;
        this.tableModel = new DefaultTableModel(new String[]{"Timestamp", "Type", "Content"}, 0);
        initializeUI();
        loadHistory();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(600, 400);
        setLocationRelativeTo(getOwner());

        JTable historyTable = new JTable(tableModel);
        historyTable.setEnabled(false); // Make table read-only
        JScrollPane scrollPane = new JScrollPane(historyTable);

        add(scrollPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadHistory() {
        tableModel.setRowCount(0); // Clear existing rows
        List<ScanHistoryManager.ScanHistoryItem> history = historyManager.getHistory();
        for (ScanHistoryManager.ScanHistoryItem item : history) {
            tableModel.addRow(new Object[]{item.getTimestamp(), item.getType(), item.getContent()});
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton clearButton = new JButton("Clear History");
        clearButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all scan history?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                historyManager.clearHistory();
                loadHistory(); // Refresh the table
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> setVisible(false));
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(closeButton);

        panel.add(clearButton, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);

        return panel;
    }
}