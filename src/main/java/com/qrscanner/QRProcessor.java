package com.qrscanner;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QRProcessor {
    private final QRScannerApp parentApp;
    private final QRCodeMultiReader qrReader;
    private final WiFiManager wifiManager;
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", Pattern.CASE_INSENSITIVE);
    
    public enum QRCodeType { WIFI, URL, TEXT, VCARD, GEO, EMAIL, SMS }

    public QRProcessor(QRScannerApp parentApp) {
        this.parentApp = parentApp;
        this.wifiManager = new WiFiManager();
        this.qrReader = new QRCodeMultiReader();
    }

    /**
     * ADDED: Generates a QR Code image from a string of text.
     * @param text The content to encode in the QR code.
     * @return A BufferedImage of the generated QR code.
     * @throws WriterException if the content cannot be encoded.
     */
    public BufferedImage generateQRCodeImage(String text) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250, hints);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
    
    // ... (rest of the file is unchanged) ...
    public String decodeQRCode(BufferedImage image) { /* ... same as before ... */
        if (image == null) return null;
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            Result result = qrReader.decode(bitmap, hints);
            return result.getText();
        } catch (Exception e) { return null; }
    }
    public void processQRCode(String qrContent) { /* ... same as before ... */
        if (qrContent == null || qrContent.trim().isEmpty()) return;
        QRCodeType type = determineQRType(qrContent);
        SwingUtilities.invokeLater(() -> {
            switch (type) {
                case WIFI: processWiFiQR(qrContent); break;
                case URL: processUrlQR(qrContent); break;
                case VCARD: processVCardQR(qrContent); break;
                case GEO: processGeoQR(qrContent); break;
                case EMAIL: processEmailQR(qrContent); break;
                case SMS: processSmsQR(qrContent); break;
                case TEXT: default: processTextQR(qrContent); break;
            }
        });
    }
    public QRCodeType determineQRType(String content) { /* ... same as before ... */
        String upperContent = content.toUpperCase();
        if (upperContent.startsWith("WIFI:")) return QRCodeType.WIFI;
        if (upperContent.startsWith("BEGIN:VCARD")) return QRCodeType.VCARD;
        String lowerContent = content.toLowerCase();
        if (lowerContent.startsWith("geo:")) return QRCodeType.GEO;
        if (lowerContent.startsWith("mailto:")) return QRCodeType.EMAIL;
        if (lowerContent.startsWith("smsto:")) return QRCodeType.SMS;
        if (URL_PATTERN.matcher(content).matches() || content.matches("(?i)www\\..+\\..+")) return QRCodeType.URL;
        return QRCodeType.TEXT;
    }
    private void processWiFiQR(String wifiQR) { /* ... same as before ... */
        WiFiCredentials credentials = parseWiFiQR(wifiQR);
        if (credentials != null) {
            parentApp.updateStatus("WiFi network detected: " + credentials.getSsid());
            showWiFiDialog(credentials);
        } else {
            parentApp.updateStatus("Invalid WiFi QR code format");
            showQRContent(wifiQR);
        }
    }
    private WiFiCredentials parseWiFiQR(String wifiQR) { /* ... same as before ... */
        Map<String, String> params = new HashMap<>();
        String content = wifiQR.substring(5);
        for (String part : content.split("(?<!\\\\);")) {
            if (part.isEmpty()) continue;
            String[] keyValue = part.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].replace("\\", "");
                String value = keyValue[1].replace("\\\\", "\\").replace("\\;", ";").replace("\\:", ":");
                params.put(key.toUpperCase(), value);
            }
        }
        String ssid = params.get("S");
        if (ssid == null || ssid.isEmpty()) return null;
        return new WiFiCredentials(ssid, params.getOrDefault("P", ""), params.getOrDefault("T", "WPA"), Boolean.parseBoolean(params.getOrDefault("H", "false")));
    }
    private void processUrlQR(String url) { /* ... same as before ... */
        String finalUrl = url.toLowerCase().startsWith("http") ? url : "https://" + url;
        parentApp.updateStatus("URL detected: " + finalUrl);
        showUrlDialog(finalUrl);
    }
    private void processTextQR(String text) { /* ... same as before ... */
        parentApp.updateStatus("Text QR code detected");
        showQRContent(text);
    }
    private void processVCardQR(String vcardData) { /* ... same as before ... */
        parentApp.updateStatus("Contact Card (vCard) detected.");
        Map<String, String> parsedVCard = parseVCard(vcardData);
        showVCardDialog(parsedVCard, vcardData);
    }
    private void processGeoQR(String geoData) { /* ... same as before ... */
        parentApp.updateStatus("Geolocation detected.");
        showGeoDialog(geoData);
    }
    private void processEmailQR(String emailData) { /* ... same as before ... */
        parentApp.updateStatus("Email action detected.");
        showEmailDialog(emailData);
    }
    private void processSmsQR(String smsData) { /* ... same as before ... */
        parentApp.updateStatus("SMS action detected.");
        showSmsDialog(smsData);
    }
    private Map<String, String> parseVCard(String vcardData) { /* ... same as before ... */
        Map<String, String> vcard = new HashMap<>();
        Pattern fnPattern = Pattern.compile("^FN:(.*)$", Pattern.MULTILINE);
        Pattern telPattern = Pattern.compile("^TEL.*:(.*)$", Pattern.MULTILINE);
        Pattern emailPattern = Pattern.compile("^EMAIL.*:(.*)$", Pattern.MULTILINE);
        Pattern orgPattern = Pattern.compile("^ORG:(.*)$", Pattern.MULTILINE);
        Matcher m;
        m = fnPattern.matcher(vcardData); if (m.find()) vcard.put("FN", m.group(1).trim());
        m = telPattern.matcher(vcardData); if (m.find()) vcard.put("TEL", m.group(1).trim());
        m = emailPattern.matcher(vcardData); if (m.find()) vcard.put("EMAIL", m.group(1).trim());
        m = orgPattern.matcher(vcardData); if (m.find()) vcard.put("ORG", m.group(1).trim());
        return vcard;
    }
    private void showVCardDialog(Map<String, String> vcard, String rawData) { /* ... same as before ... */
        String message = String.format("<html><h3>Contact Card Detected</h3><p><b>Name:</b> %s</p><p><b>Phone:</b> %s</p><p><b>Email:</b> %s</p><p><b>Organization:</b> %s</p><br><p>Do you want to save this contact?</p></html>", vcard.getOrDefault("FN", "N/A"), vcard.getOrDefault("TEL", "N/A"), vcard.getOrDefault("EMAIL", "N/A"), vcard.getOrDefault("ORG", "N/A"));
        if (JOptionPane.showConfirmDialog(parentApp, message, "Save Contact", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            saveVCard(rawData, vcard.getOrDefault("FN", "contact"));
        }
    }
    private void saveVCard(String rawData, String suggestedName) { /* ... same as before ... */
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Contact As");
        fileChooser.setSelectedFile(new File(suggestedName.replaceAll("[^a-zA-Z0-9.-]", "_") + ".vcf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("vCard File (*.vcf)", "vcf"));
        if (fileChooser.showSaveDialog(parentApp) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(rawData);
                parentApp.updateStatus("Contact saved to " + fileToSave.getName());
                JOptionPane.showMessageDialog(parentApp, "Contact saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                showError("Failed to save contact file: " + e.getMessage());
            }
        }
    }
    private void showGeoDialog(String geoData) { /* ... same as before ... */
        if (JOptionPane.showConfirmDialog(parentApp, "A location QR code was detected. Open in your default map application?", "Open Location", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            try { Desktop.getDesktop().browse(new URI(geoData)); } 
            catch (Exception e) { showError("Could not open map application: " + e.getMessage()); }
        }
    }
    private void showEmailDialog(String emailData) { /* ... same as before ... */
        if (JOptionPane.showConfirmDialog(parentApp, "An email QR code was detected. Open in your default email client?", "Compose Email", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            try { Desktop.getDesktop().mail(new URI(emailData)); } 
            catch (Exception e) { showError("Could not open email client: " + e.getMessage()); }
        }
    }
    private void showSmsDialog(String smsData) { /* ... same as before ... */
        if (JOptionPane.showConfirmDialog(parentApp, "An SMS QR code was detected. Attempt to open in a messaging app?", "Send SMS", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            try { Desktop.getDesktop().browse(new URI(smsData)); } 
            catch (Exception e) { showError("Could not open messaging app. This feature may not be supported on your OS."); }
        }
    }
    private void showWiFiDialog(WiFiCredentials credentials) { /* ... same as before ... */
        String message = String.format("<html><h3>WiFi Network Detected</h3><p><b>Network:</b> %s</p><p><b>Security:</b> %s</p><p><b>Password:</b> %s</p><br><p>Would you like to connect to this network?</p></html>", credentials.getSsid(), credentials.getSecurity(), credentials.hasPassword() ? "••••••••" : "None");
        if (JOptionPane.showConfirmDialog(parentApp, message, "Connect to WiFi", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            connectToWiFi(credentials);
        }
    }
    private void showUrlDialog(String url) { /* ... same as before ... */
        String message = String.format("<html><h3>URL Detected</h3><p><b>URL:</b> %s</p><br><p>Would you like to open this URL in your default browser?</p></html>", url);
        if (JOptionPane.showConfirmDialog(parentApp, message, "Open URL", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            openUrl(url);
        }
    }
    private void showQRContent(String content) { /* ... same as before ... */
        JTextArea textArea = new JTextArea(content, 10, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(parentApp, new JScrollPane(textArea), "QR Code Content", JOptionPane.INFORMATION_MESSAGE);
    }
    private void connectToWiFi(WiFiCredentials credentials) { /* ... same as before ... */
        parentApp.updateStatus("Connecting to " + credentials.getSsid() + "...");
        new SwingWorker<Boolean, Void>() {
            private Exception connectionException = null;
            @Override
            protected Boolean doInBackground() {
                try { return wifiManager.connectToNetwork(credentials); } 
                catch (Exception e) { connectionException = e; return false; }
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        parentApp.updateStatus("Successfully connected to " + credentials.getSsid());
                        JOptionPane.showMessageDialog(parentApp, "Successfully connected to WiFi network: " + credentials.getSsid(), "WiFi Connected", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        throw (connectionException != null) ? connectionException : new Exception("Connection failed for an unknown reason.");
                    }
                } catch (Exception e) {
                    showError("Error connecting to WiFi:\n" + e.getMessage());
                }
            }
        }.execute();
    }
    private void openUrl(String url) { /* ... same as before ... */
        try { Desktop.getDesktop().browse(new URI(url)); parentApp.updateStatus("Opened URL in browser: " + url); } 
        catch (Exception e) { showError("Failed to open URL: " + e.getMessage()); }
    }
    private void showError(String message) { /* ... same as before ... */
        parentApp.updateStatus("Error: " + message);
        JOptionPane.showMessageDialog(parentApp, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static class WiFiCredentials { /* ... same as before ... */
        private final String ssid, password, security;
        private final boolean hidden;
        public WiFiCredentials(String ssid, String password, String security, boolean hidden) { this.ssid = ssid; this.password = password; this.security = security; this.hidden = hidden; }
        public String getSsid() { return ssid; }
        public String getPassword() { return password; }
        public String getSecurity() { return security; }
        public boolean isHidden() { return hidden; }
        public boolean hasPassword() { return password != null && !password.isEmpty(); }
    }
}