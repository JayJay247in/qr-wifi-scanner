package com.qrscanner;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class QRExportDialog extends JDialog {

    private final QRProcessor qrProcessor;
    private JTextArea inputTextArea;
    private ImagePanel previewPanel;
    private JButton saveButton;
    private BufferedImage currentQRCode;
    private Timer updateTimer;

    public QRExportDialog(JFrame parent, QRProcessor qrProcessor) {
        super(parent, "Create and Export QR Code", true);
        this.qrProcessor = qrProcessor;
        initializeUI();
        setupListeners();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 400);
        setLocationRelativeTo(getOwner());
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // Input Area
        inputTextArea = new JTextArea("Enter text or URL here...");
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        JScrollPane textScrollPane = new JScrollPane(inputTextArea);

        // Preview Area
        previewPanel = new ImagePanel();
        previewPanel.setPreferredSize(new Dimension(250, 250));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Live Preview"));

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 0));
        mainPanel.add(textScrollPane, BorderLayout.CENTER);
        mainPanel.add(previewPanel, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private void setupListeners() {
        // Timer to delay QR code regeneration for better performance
        updateTimer = new Timer(300, e -> updatePreview());
        updateTimer.setRepeats(false);

        inputTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTimer.restart();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTimer.restart();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTimer.restart();
            }
        });

        // Initial generation
        SwingUtilities.invokeLater(this::updatePreview);
    }

    private void updatePreview() {
        String text = inputTextArea.getText();
        if (text == null || text.trim().isEmpty()) {
            currentQRCode = null;
            saveButton.setEnabled(false);
        } else {
            try {
                currentQRCode = qrProcessor.generateQRCodeImage(text);
                saveButton.setEnabled(true);
            } catch (Exception e) {
                currentQRCode = null;
                saveButton.setEnabled(false);
                System.err.println("Could not generate QR code preview: " + e.getMessage());
            }
        }
        previewPanel.setImage(currentQRCode);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save as Image...");
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveImage());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> setVisible(false));

        panel.add(saveButton);
        panel.add(closeButton);
        return panel;
    }

    private void saveImage() {
        if (currentQRCode == null) {
            JOptionPane.showMessageDialog(this, "There is no QR code to save.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save QR Code Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));
        fileChooser.setSelectedFile(new File("qrcode.png"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ImageIO.write(currentQRCode, "PNG", fileToSave);
                JOptionPane.showMessageDialog(this, "QR Code saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to save image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Custom JPanel for displaying an image
    private static class ImagePanel extends JPanel {
        private BufferedImage image;

        public void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                // Scale image to fit panel while maintaining aspect ratio
                int panelWidth = getWidth() - 10; // some padding
                int panelHeight = getHeight() - 10;
                int imgWidth = image.getWidth();
                int imgHeight = image.getHeight();

                double scale = Math.min((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);
                int newWidth = (int) (imgWidth * scale);
                int newHeight = (int) (imgHeight * scale);

                int x = (getWidth() - newWidth) / 2;
                int y = (getHeight() - newHeight) / 2;
                
                g.drawImage(image, x, y, newWidth, newHeight, this);
            } else {
                g.drawString("No QR Code", getWidth() / 2 - 30, getHeight() / 2);
            }
        }
    }
}