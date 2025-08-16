package com.qrscanner;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * WiFi Manager for handling network connections across different operating systems
 */
public class WiFiManager {
    private final String operatingSystem;
    
    public WiFiManager() {
        this.operatingSystem = System.getProperty("os.name").toLowerCase();
    }
    
    public boolean connectToNetwork(QRProcessor.WiFiCredentials credentials) throws Exception {
        if (operatingSystem.contains("win")) {
            return connectWindowsWiFi(credentials);
        } else if (operatingSystem.contains("mac")) {
            return connectMacWiFi(credentials);
        } else if (operatingSystem.contains("nix") || operatingSystem.contains("nux")) {
            return connectLinuxWiFi(credentials);
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + operatingSystem);
        }
    }
    
    /**
     * CORRECTION: Encapsulate netsh commands within "cmd.exe /c" to ensure
     * that arguments with spaces (like the SSID "Galaxy S9") are parsed correctly
     * by the Windows shell.
     */
    private boolean connectWindowsWiFi(QRProcessor.WiFiCredentials credentials) throws Exception {
        String ssid = credentials.getSsid();
        
        // Proactively delete an existing profile to prevent "add" conflicts.
        try {
            String deleteCmd = "netsh wlan delete profile name=\"" + ssid + "\"";
            executeCommand("cmd.exe", "/c", deleteCmd);
        } catch (Exception e) {
            System.out.println("Info: Could not delete profile for '" + ssid + "'. It probably didn't exist, which is fine.");
        }

        String profileXml = createWindowsWiFiProfile(ssid, credentials.getPassword(), credentials.getSecurity());
        File tempProfile = File.createTempFile("wifi_profile_", ".xml");
        tempProfile.deleteOnExit();
        
        try (FileWriter writer = new FileWriter(tempProfile)) {
            writer.write(profileXml);
        }
        
        try {
            String addCmd = "netsh wlan add profile filename=\"" + tempProfile.getAbsolutePath() + "\"";
            executeCommand("cmd.exe", "/c", addCmd);

            String connectCmd = "netsh wlan connect name=\"" + ssid + "\"";
            executeCommand("cmd.exe", "/c", connectCmd);
            
            Thread.sleep(3000); // Wait for connection to establish
            return isConnectedToNetwork(ssid);
        } finally {
            tempProfile.delete();
        }
    }
    
    private boolean connectMacWiFi(QRProcessor.WiFiCredentials credentials) throws Exception {
        String[] cmd = credentials.hasPassword() ?
            new String[]{"networksetup", "-setairportnetwork", "en0", credentials.getSsid(), credentials.getPassword()} :
            new String[]{"networksetup", "-setairportnetwork", "en0", credentials.getSsid()};
        
        executeCommand(cmd);
        Thread.sleep(5000);
        return isConnectedToNetwork(credentials.getSsid());
    }
    
    private boolean connectLinuxWiFi(QRProcessor.WiFiCredentials credentials) throws Exception {
        if (!isCommandAvailable("nmcli")) {
            throw new Exception("NetworkManager (nmcli) tool not found. Cannot manage WiFi.");
        }
        String[] cmd = credentials.hasPassword() ?
            new String[]{"nmcli", "dev", "wifi", "connect", credentials.getSsid(), "password", credentials.getPassword()} :
            new String[]{"nmcli", "dev", "wifi", "connect", credentials.getSsid()};
            
        executeCommand(cmd);
        Thread.sleep(3000);
        return isConnectedToNetwork(credentials.getSsid());
    }

    private void executeCommand(String... command) throws IOException, InterruptedException, Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true); // Merge stdout and stderr
        Process process = builder.start();

        if (!process.waitFor(15, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new Exception("Command timed out: " + String.join(" ", command));
        }
        if (process.exitValue() != 0) {
            String error = readProcessOutput(process.getInputStream());
            throw new Exception("Command failed: " + error);
        }
    }

    private String createWindowsWiFiProfile(String ssid, String password, String security) {
        String authType, encryptionType, keyMaterial = "";
        
        switch (security.toUpperCase()) {
            case "WPA3":
                authType = "WPA3PSK"; encryptionType = "AES";
                keyMaterial = String.format("<keyMaterial>%s</keyMaterial>", password);
                break;
            case "WPA": case "WPA2":
                authType = "WPA2PSK"; encryptionType = "AES";
                keyMaterial = String.format("<keyMaterial>%s</keyMaterial>", password);
                break;
            case "WEP":
                authType = "open"; encryptionType = "WEP";
                keyMaterial = String.format("<keyMaterial>%s</keyMaterial>", password);
                break;
            case "NOPASS": default:
                authType = "open"; encryptionType = "none";
                break;
        }
        
        return String.format(
            "<?xml version=\"1.0\"?><WLANProfile xmlns=\"http://www.microsoft.com/networking/WLAN/profile/v1\"><name>%s</name><SSIDConfig><SSID><name>%s</name></SSID></SSIDConfig><connectionType>ESS</connectionType><connectionMode>auto</connectionMode><MSM><security><authEncryption><authentication>%s</authentication><encryption>%s</encryption><useOneX>false</useOneX></authEncryption><sharedKey><keyType>passPhrase</keyType><protected>false</protected>%s</sharedKey></security></MSM></WLANProfile>",
            ssid, ssid, authType, encryptionType, keyMaterial
        );
    }
    
    private boolean isConnectedToNetwork(String ssid) throws IOException {
        String[] cmd;
        if (operatingSystem.contains("win")) {
            cmd = new String[]{"netsh", "wlan", "show", "interfaces"};
        } else if (operatingSystem.contains("mac")) {
            cmd = new String[]{"/System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport", "-I"};
        } else if (operatingSystem.contains("nix") || operatingSystem.contains("nux")) {
            cmd = new String[]{"nmcli", "-t", "-f", "active,ssid", "dev", "wifi"};
        } else {
            return false;
        }

        try {
            Process process = new ProcessBuilder(cmd).start();
            String output = readProcessOutput(process.getInputStream());
            return output.contains(ssid);
        } catch (IOException e) {
            System.err.println("Failed to check network status: " + e.getMessage());
            return false;
        }
    }
    
    private boolean isCommandAvailable(String command) {
        String testCmd = operatingSystem.contains("win") ? "where" : "which";
        try {
            return new ProcessBuilder(testCmd, command).start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String readProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }
}