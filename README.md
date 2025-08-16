# QR WiFi Scanner

A desktop application that uses your system camera, screen, or local files to scan QR codes and automatically connect to WiFi networks, open URLs, save contacts, and more.

![Java](https://img.shields.io/badge/Java-11+-orange.svg)![License](https://img.shields.io/badge/License-MIT-blue.svg)![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey.svg)

## Features

- 📱 **Live Camera Scanning**: Scan QR codes in real-time using your webcam.
- 📂 **Scan from File**: Scan QR codes from local image (`png`, `jpg`) and `pdf` files.
- 🖥️ **Scan from Screen**: Use a global hotkey (`Ctrl + Alt + S`) to instantly scan for a QR code anywhere on your screen.
- ✨ **QR Code Creator**: Generate your own QR codes from text and export them as PNG images.
- 🔐 **WiFi Auto-Connect**: Automatically connect to WiFi networks from QR codes.
- 🌐 **Expanded QR Support**: Natively handles URLs, vCards (Contacts), Geo-locations, Email, and SMS formats.
- 📋 **Scan History**: Automatically saves a history of all successful scans for later review.
- ⚙️ **Batch Processing**: Select and scan multiple files at once with a consolidated results summary.
- 🎯 **System Tray Integration**: Minimize to the system tray for quick access.
- 🚀 **Auto-Update Checker**: Notifies you on startup if a new version of the application is available.
- 🖥️ **Cross-Platform**: Works on Windows, macOS, and Linux.

## Screenshots

*Screenshots to be added*

## Installation

### Prerequisites

- Java 11 or higher
- A webcam or camera device (for live scanning)
- Administrator/root privileges (for WiFi connections and the global hotkey listener)

### Download

1. **Download the latest release** from the [Releases](https://github.com/JayJay247in/qr-wifi-scanner/releases) page.
2. **Or build from source** (see Building section below).

### Quick Start

1. Run the JAR file from your terminal. For full functionality, run as an administrator:
   ```bash
   java -jar qr-wifi-scanner-1.0.0.jar
   ```

2. Click "Start Scanning" for the live camera feed, or use the "File" menu to scan from a local file.

3. Point your camera at a QR code or use the hotkey to scan your screen. The app will automatically prompt you with the appropriate action.

## Supported QR Code Formats

### WiFi Networks
```
WIFI:T:WPA;S:NetworkName;P:Password;H:false;;
```
- **T**: Security type (WPA, WPA2, WPA3, WEP, or blank for open)
- **S**: Network SSID (name)
- **P**: Password
- **H**: Hidden network (true/false)

### URLs
- `https://example.com`, `http://example.com`, `ftp://example.com`
- Plain domains (e.g., `github.com`) are automatically opened with `https://`

### vCard (Contact)
- Standard `BEGIN:VCARD ... END:VCARD` format.
- Prompts to save the contact as a `.vcf` file.

### Geolocation
- `geo:40.7128,-74.0060`
- Prompts to open the coordinates in your default mapping application.

### Email
- `mailto:someone@example.com?subject=Hello&body=Message`
- Prompts to open the pre-filled message in your default email client.

### SMS
- `smsto:1234567890:Hello there`
- Prompts to open in your system's default messaging application.

### Plain Text
- Any other text content will be displayed in a dialog box.

## Building from Source

### Prerequisites
- Java 11+ JDK
- Maven 3.6+

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/JayJay247in/qr-wifi-scanner.git
   cd qr-wifi-scanner
   ```

2. **Compile and package**:
   ```bash
   mvn clean package
   ```

3. **Run the application**:
   ```bash
   java -jar target/qr-wifi-scanner-1.0.0.jar
   ```

## Usage

### Scanning Methods
- **Live Camera**: Click the "Start Scanning" button.
- **From File**: Go to `File > Scan from File...` and select one or more image/PDF files.
- **From Screen**: Press the global hotkey `Ctrl + Alt + S` at any time.

### Main Features
- **Create QR Code**: Go to `File > Create QR Code...` to open the generator. Type your text and save the live preview as a PNG.
- **View History**: Go to `Tools > View History...` to see a table of your past scans.
- **Settings**: Go to `Tools > Settings` to configure application options.

### System Tray
- **Minimize**: Closing the main window sends the app to the system tray.
- **Restore**: Double-click the tray icon to show the main window.
- **Exit**: Right-click the tray icon and select "Exit".

### Keyboard Shortcuts
- `Ctrl + Alt + S`: (Global Hotkey) Triggers a scan of the entire screen.

## System Requirements
- **OS**: Windows 10+, macOS 10.14+, or a modern Linux distribution.
- **Java**: Java 11 or higher.
- **Permissions**: Administrator/root access is required for automatic WiFi connection and is recommended for the global hotkey to function reliably.

## Troubleshooting

### Common Issues

**Camera not detected**:
- Ensure the camera is not being used by another application.
- Verify camera drivers are installed and up to date.

**WiFi connection fails**:
- **Run the application as an administrator.** This is the most common cause of failure on Windows.
- Ensure the network is in range and the password in the QR code is correct.

**Global Hotkey not working**:
- **Run the application as an administrator/root.** Operating systems often restrict global input listening to privileged applications for security reasons.

## Dependencies

- **ZXing (core, javase)**: For QR code processing, generation, and decoding.
- **Webcam Capture**: For simple and robust camera access.
- **Apache PDFBox**: For rendering PDF documents into scannable images.
- **JNativeHook**: For listening to global keyboard events for the hotkey feature.
- **Java Swing**: The core GUI framework.

## License

This project is licensed under the MIT License.

## Acknowledgments

- The **ZXing** project for its essential QR code library.
- The **Webcam Capture** project for making camera integration straightforward.
- The **JNativeHook** project for enabling global hotkey functionality.
- The **Apache PDFBox** project for its powerful PDF processing capabilities.

## Changelog

### v1.0.0
- **Initial Release & Feature Completion**
- Core QR code scanning from webcam.
- Automatic connection for WiFi and opening for URLs.
- **Feature Expansion**: Added support for vCard, Geolocation, Email, and SMS QR code types.
- **File Scanning**: Implemented scanning from local image and PDF files.
- **Batch Processing**: Added support for scanning multiple files at once.
- **Export Feature**: Implemented a "Create QR Code" dialog with live preview and PNG export.
- **History**: Added a scan history manager and viewer dialog.
- **Hotkeys**: Implemented a global hotkey (`Ctrl+Alt+S`) for screen scanning.
- **UI**: Added a settings dialog foundation.
- **Auto-Update**: Implemented a startup check for new application versions.

## Roadmap

All initial goals for this version have been implemented. Future development could focus on:
- [✔] Enhanced UI & Settings Dialog
- [✔] Additional QR Code Type Support
- [✔] Global Hotkeys
- [✔] Scan History
- [✔] Batch Processing
- [✔] Export Features
- [✔] Automatic Application Updates