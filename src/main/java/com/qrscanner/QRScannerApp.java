package com.qrscanner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QRScannerApp extends JFrame {
    private static final String APP_NAME = "QR WiFi Scanner";
    private static final String VERSION = "1.0.0";
    // IMPORTANT: Replace this with the raw URL to your version.txt file
    private static final String UPDATE_URL = "https://raw.githubusercontent.com/your-username/your-repo/main/version.txt";

    private CameraPanel cameraPanel;
    private QRProcessor qrProcessor;
    private SystemTrayManager trayManager;
    private ScanHistoryManager historyManager;
    private HotkeyManager hotkeyManager;
    private JLabel statusLabel;
    private JButton scanButton;
    
    public QRScannerApp() {
        this.historyManager = new ScanHistoryManager();
        initializeComponents();
        setupUI();
        setupEventHandlers();
        this.qrProcessor = new QRProcessor(this);
        this.trayManager = new SystemTrayManager(this);
        this.cameraPanel.setQRProcessor(qrProcessor);
        this.hotkeyManager = new HotkeyManager(this::scanScreenForQRCode);
        this.hotkeyManager.initialize();

        // Check for updates on startup
        checkForUpdates();
    }

    /**
     * ADDED: Checks for a new application version in the background.
     */
    private void checkForUpdates() {
        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() {
                try {
                    URL url = new URL(UPDATE_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000); // 5-second timeout
                    conn.setReadTimeout(5000);

                    if (conn.getResponseCode() == 200) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                            String latestVersion = reader.readLine();
                            String downloadUrl = reader.readLine();
                            if (latestVersion != null && downloadUrl != null) {
                                return new String[]{latestVersion.trim(), downloadUrl.trim()};
                            }
                        }
                    }
                } catch (IOException e) {
                    // Fail silently on network errors
                    System.err.println("Update check failed: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    String[] updateInfo = get();
                    if (updateInfo != null && updateInfo.length == 2) {
                        String latestVersion = updateInfo[0];
                        String downloadUrl = updateInfo[1];

                        // Compare versions (e.g., "1.0.1" > "1.0.0")
                        if (latestVersion.compareTo(VERSION) > 0) {
                            String message = String.format(
                                "<html>A new version (<b>%s</b>) is available.<br>You are currently running version %s.<br><br>Would you like to open the download page?</html>",
                                latestVersion, VERSION
                            );
                            int response = JOptionPane.showConfirmDialog(QRScannerApp.this, message, "Update Available", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                            if (response == JOptionPane.YES_OPTION) {
                                Desktop.getDesktop().browse(new URI(downloadUrl));
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing update information: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void initializeComponents() { /* ... same as before ... */
        setTitle(APP_NAME + " v" + VERSION);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon.png"));
            setIconImage(icon);
        } catch (Exception e) { System.err.println("Could not load application icon: " + e.getMessage()); }
    }
    private void setupUI() { /* ... same as before ... */
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout());
        cameraPanel = new CameraPanel();
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera Preview"));
        mainPanel.add(cameraPanel, BorderLayout.CENTER);
        mainPanel.add(createControlPanel(), BorderLayout.SOUTH);
        add(createStatusPanel(), BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
        setJMenuBar(createMenuBar());
    }
    private JPanel createControlPanel() { /* ... same as before ... */
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        scanButton = new JButton("Start Scanning");
        scanButton.setPreferredSize(new Dimension(140, 40));
        JButton settingsButton = new JButton("Settings");
        settingsButton.setPreferredSize(new Dimension(100, 40));
        settingsButton.addActionListener(e -> showSettings());
        JButton aboutButton = new JButton("About");
        aboutButton.setPreferredSize(new Dimension(80, 40));
        aboutButton.addActionListener(e -> showAboutDialog());
        panel.add(scanButton);
        panel.add(settingsButton);
        panel.add(aboutButton);
        return panel;
    }
    private JPanel createStatusPanel() { /* ... same as before ... */
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Ready to scan QR codes...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(statusLabel, BorderLayout.CENTER);
        return panel;
    }
    private JMenuBar createMenuBar() { /* ... same as before ... */
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem createQrItem = new JMenuItem("Create QR Code...");
        createQrItem.addActionListener(e -> showExportDialog());
        JMenuItem scanFromFileItem = new JMenuItem("Scan from File...");
        scanFromFileItem.addActionListener(e -> scanFromFile());
        JMenuItem minimizeItem = new JMenuItem("Minimize to Tray");
        minimizeItem.addActionListener(e -> minimizeToTray());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(createQrItem);
        fileMenu.add(scanFromFileItem);
        fileMenu.addSeparator();
        fileMenu.add(minimizeItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> showSettings());
        JMenuItem historyItem = new JMenuItem("View History...");
        historyItem.addActionListener(e -> showHistory());
        toolsMenu.add(settingsItem);
        toolsMenu.add(historyItem);
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }
    private void scanScreenForQRCode() { /* ... same as before ... */
        trayManager.showTrayMessage("Scanning screen for QR Code...", "Scan Initiated");
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage capture = new Robot().createScreenCapture(screenRect);
                return qrProcessor.decodeQRCode(capture);
            }
            @Override
            protected void done() {
                try {
                    String qrContent = get();
                    if (qrContent != null) {
                        trayManager.showTrayMessage("QR Code found on screen!", "Success");
                        onQRCodeDetected(qrContent);
                    } else {
                        trayManager.showTrayMessage("No QR Code was found on the screen.", "Scan Complete");
                    }
                } catch (Exception e) {
                    trayManager.showTrayMessage("An error occurred during screen capture.", "Error", TrayIcon.MessageType.ERROR);
                }
            }
        };
        worker.execute();
    }
    private void scanFromFile() { /* ... same as before ... */
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select QR Code Image(s) or PDF(s)");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image & PDF Files", "png", "jpg", "jpeg", "bmp", "pdf"));
        fileChooser.setMultiSelectionEnabled(true);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles.length == 1) {
                updateStatus("Scanning file: " + selectedFiles[0].getName());
                processSingleFileWithWorker(selectedFiles[0]);
            } else if (selectedFiles.length > 1) {
                processBatchFilesWithWorker(selectedFiles);
            }
        }
    }
    private void processSingleFileWithWorker(File file) { /* ... same as before ... */
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception { return scanFileForQRCode(file); }
            @Override
            protected void done() {
                try {
                    String qrContent = get();
                    if (qrContent != null) onQRCodeDetected(qrContent);
                    else showError("No QR code could be found in the selected file.");
                } catch (Exception e) {
                    showError("Failed to process file: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    private void processBatchFilesWithWorker(File[] files) { /* ... same as before ... */
        SwingWorker<Map<String, String>, String> worker = new SwingWorker<>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                Map<String, String> results = new LinkedHashMap<>();
                for (int i = 0; i < files.length; i++) {
                    publish("Scanning file " + (i + 1) + " of " + files.length + ": " + files[i].getName());
                    String qrContent = scanFileForQRCode(files[i]);
                    if (qrContent != null) {
                        results.put(files[i].getName(), qrContent);
                    }
                }
                return results;
            }
            @Override
            protected void process(List<String> chunks) {
                updateStatus(chunks.get(chunks.size() - 1));
            }
            @Override
            protected void done() {
                try {
                    Map<String, String> results = get();
                    updateStatus("Batch scan complete.");
                    if (results.isEmpty()) {
                        JOptionPane.showMessageDialog(QRScannerApp.this, "No QR codes were found in any of the selected files.", "Batch Scan Complete", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    for (Map.Entry<String, String> entry : results.entrySet()) {
                        String qrContent = entry.getValue();
                        String qrType = qrProcessor.determineQRType(qrContent).name();
                        historyManager.addHistoryItem(qrContent, qrType);
                    }
                    StringBuilder summary = new StringBuilder("Found " + results.size() + " QR code(s) in " + files.length + " files:\n\n");
                    for (Map.Entry<String, String> entry : results.entrySet()) {
                        summary.append("File: ").append(entry.getKey()).append("\n");
                        summary.append("Content: ").append(entry.getValue()).append("\n\n");
                    }
                    JTextArea textArea = new JTextArea(summary.toString());
                    textArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    scrollPane.setPreferredSize(new Dimension(500, 300));
                    JOptionPane.showMessageDialog(QRScannerApp.this, scrollPane, "Batch Scan Results", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    showError("An error occurred during batch processing: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    private String scanFileForQRCode(File file) throws IOException { /* ... same as before ... */
        String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
        if ("pdf".equals(extension)) {
            try (PDDocument document = PDDocument.load(file)) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                for (int pageNum = 0; pageNum < document.getNumberOfPages(); ++pageNum) {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(pageNum, 300);
                    String qrContent = qrProcessor.decodeQRCode(image);
                    if (qrContent != null) return qrContent;
                }
            }
        } else {
            BufferedImage image = ImageIO.read(file);
            if (image != null) return qrProcessor.decodeQRCode(image);
            else throw new IOException("Could not read image file: " + file.getName());
        }
        return null;
    }
    private void setupEventHandlers() { /* ... same as before ... */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { exitApplication(); }
        });
        scanButton.addActionListener(e -> toggleScanning());
    }
    public void toggleScanning() { if (cameraPanel.isScanning()) stopScanning(); else startScanning(); }
    private void startScanning() { /* ... same as before ... */
        try {
            cameraPanel.startCamera();
            scanButton.setText("Stop Scanning");
            trayManager.setTrayIconAnimated(true);
            updateStatus("Scanning for QR codes...");
        } catch (Exception e) { showError("Failed to start camera: " + e.getMessage()); }
    }
    private void stopScanning() { /* ... same as before ... */
        cameraPanel.stopCamera();
        scanButton.setText("Start Scanning");
        trayManager.setTrayIconAnimated(false);
        updateStatus("Scanning stopped.");
    }
    public void updateStatus(String message) { SwingUtilities.invokeLater(() -> statusLabel.setText(message)); }
    public void onQRCodeDetected(String qrContent) { /* ... same as before ... */
        SwingUtilities.invokeLater(() -> {
            updateStatus("QR Code detected! Processing...");
            String qrType = qrProcessor.determineQRType(qrContent).name();
            historyManager.addHistoryItem(qrContent, qrType);
            qrProcessor.processQRCode(qrContent);
        });
    }
    private void minimizeToTray() { /* ... same as before ... */
        if (trayManager.isSystemTraySupported()) {
            setVisible(false);
            trayManager.showTrayMessage("QR Scanner is running in the background.");
        } else {
            setExtendedState(JFrame.ICONIFIED);
        }
    }
    public void restoreFromTray() { /* ... same as before ... */
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            setExtendedState(JFrame.NORMAL);
            toFront();
            requestFocus();
        });
    }
    private void exitApplication() { /* ... same as before ... */
        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            hotkeyManager.cleanup();
            cameraPanel.cleanup();
            trayManager.cleanup();
            dispose();
            System.exit(0);
        }
    }
    private void showSettings() { /* ... same as before ... */
        SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.setVisible(true);
    }
    private void showHistory() { /* ... same as before ... */
        HistoryDialog historyDialog = new HistoryDialog(this, historyManager);
        historyDialog.setVisible(true);
    }
    private void showExportDialog() { /* ... same as before ... */
        QRExportDialog exportDialog = new QRExportDialog(this, qrProcessor);
        exportDialog.setVisible(true);
    }
    private void showAboutDialog() { /* ... same as before ... */
        String aboutText = String.format("<html><h2>%s</h2><p>Version: %s</p><br><p>A desktop utility for scanning QR codes from a webcam.</p><b>Features:</b><ul><li>WiFi network connection</li><li>URL opening in default browser</li><li>System tray integration</li></ul></html>", APP_NAME, VERSION);
        JOptionPane.showMessageDialog(this, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showError(String message) { /* ... same as before ... */
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        updateStatus("Error: " + message);
    }
    public static void main(String[] args) { /* ... same as before ... */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { System.err.println("Could not set system look and feel: " + e.getMessage()); }
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new QRScannerApp().setVisible(true));
    }
}