package com.qrscanner;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CameraPanel extends JPanel {
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private QRProcessor qrProcessor;
    private boolean scanning = false;
    private final ExecutorService qrScanExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean processingQR = false;
    private long scanInterval = 333; // Default interval
    private JLabel statusLabel;
    private JComboBox<String> cameraSelector;
    private JLabel noImageLabel;
    
    public CameraPanel() {
        initializeComponents();
        setupUI();
        detectAvailableCameras();
    }
    
    public void setScanInterval(long millis) {
        this.scanInterval = Math.max(100, millis); // Ensure at least 100ms delay
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        statusLabel = new JLabel("Camera not started", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(0, 0, 0, 150));
        noImageLabel = new JLabel("No camera feed", SwingConstants.CENTER);
        noImageLabel.setForeground(Color.LIGHT_GRAY);
        noImageLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        noImageLabel.setOpaque(true);
        noImageLabel.setBackground(Color.BLACK);
    }
    
    private void setupUI() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controlPanel.setBackground(Color.DARK_GRAY);
        cameraSelector = new JComboBox<>();
        cameraSelector.setPreferredSize(new Dimension(250, 25));
        cameraSelector.addActionListener(e -> switchCamera());
        controlPanel.add(new JLabel("Camera:"));
        controlPanel.add(cameraSelector);
        add(controlPanel, BorderLayout.NORTH);
        add(noImageLabel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void detectAvailableCameras() {
        new SwingWorker<List<Webcam>, Void>() {
            @Override
            protected List<Webcam> doInBackground() { return Webcam.getWebcams(); }
            @Override
            protected void done() {
                try {
                    List<Webcam> webcams = get();
                    cameraSelector.removeAllItems();
                    if (webcams.isEmpty()) {
                        cameraSelector.addItem("No cameras detected");
                        statusLabel.setText("No cameras available");
                    } else {
                        webcams.forEach(cam -> cameraSelector.addItem(cam.getName()));
                        statusLabel.setText(webcams.size() + " camera(s) detected");
                    }
                } catch (Exception e) {
                    cameraSelector.addItem("Error detecting cameras");
                    statusLabel.setText("Camera detection failed: " + e.getMessage());
                }
            }
        }.execute();
    }
    
    private void switchCamera() {
        if (scanning) {
            stopCamera();
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(500);
                    startCamera();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Failed to switch camera: " + e.getMessage());
                }
            });
        }
    }
    
    public void startCamera() throws Exception {
        if (scanning) return;
        int selectedIndex = cameraSelector.getSelectedIndex();
        List<Webcam> webcams = Webcam.getWebcams();
        if (webcams.isEmpty()) throw new Exception("No cameras available to start");
        if (selectedIndex < 0 || selectedIndex >= webcams.size()) selectedIndex = 0;
        webcam = webcams.get(selectedIndex);
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcamPanel = new WebcamPanel(webcam, false);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setMirrored(true);
        remove(noImageLabel);
        add(webcamPanel, BorderLayout.CENTER);
        webcamPanel.start();
        scanning = true;
        statusLabel.setText("Camera started - scanning for QR codes...");
        startQRDetection();
        revalidate();
        repaint();
    }
    
    public void stopCamera() {
        if (!scanning) return;
        scanning = false;
        if (webcamPanel != null) {
            webcamPanel.stop();
            remove(webcamPanel);
            webcamPanel = null;
        }
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            webcam = null;
        }
        add(noImageLabel, BorderLayout.CENTER);
        statusLabel.setText("Camera stopped");
        revalidate();
        repaint();
    }
    
    private void startQRDetection() {
        qrScanExecutor.execute(() -> {
            while (scanning) {
                if (processingQR) continue;
                try {
                    if (!scanning || webcam == null) break;
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        processingQR = true;
                        processImageForQR(image);
                    }
                    Thread.sleep(this.scanInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    processingQR = false;
                }
            }
        });
    }
    
    private void processImageForQR(BufferedImage image) {
        if (qrProcessor == null) return;
        String qrContent = qrProcessor.decodeQRCode(image);
        if (qrContent != null && !qrContent.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("QR Code detected!");
                notifyQRDetected(qrContent);
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void notifyQRDetected(String qrContent) {
        Component parent = SwingUtilities.getWindowAncestor(this);
        if (parent instanceof QRScannerApp) ((QRScannerApp) parent).onQRCodeDetected(qrContent);
    }
    
    public void cleanup() {
        stopCamera();
        qrScanExecutor.shutdownNow();
        try {
            if (!qrScanExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("QR scan thread did not terminate gracefully.");
            }
        } catch (InterruptedException e) {
            qrScanExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public void setQRProcessor(QRProcessor processor) { this.qrProcessor = processor; }
    public boolean isScanning() { return scanning; }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        stopCamera();
    }
}