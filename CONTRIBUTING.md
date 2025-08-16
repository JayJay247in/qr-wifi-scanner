# Contributing to QR WiFi Scanner

Thank you for your interest in contributing to QR WiFi Scanner! This document provides guidelines and information for contributors.

## Ways to Contribute

- 🐛 **Bug Reports**: Report issues and bugs
- ✨ **Feature Requests**: Suggest new features
- 💻 **Code Contributions**: Submit code improvements
- 📚 **Documentation**: Improve documentation
- 🧪 **Testing**: Help test on different platforms
- 🌍 **Translations**: Add internationalization support

## Getting Started

### Prerequisites

- Java 11+ JDK
- Maven 3.6+
- Git
- An IDE (IntelliJ IDEA, Eclipse, or VS Code is recommended)

### Development Setup

1.  **Fork the repository** on GitHub.

2.  **Clone your fork**:
    ```bash
    git clone https://github.com/your-username/qr-wifi-scanner.git
    cd qr-wifi-scanner
    ```

3.  **Set up upstream remote**:
    ```bash
    git remote add upstream https://github.com/JayJay247in/qr-wifi-scanner.git
    ```

4.  **Build the project**:
    ```bash
    mvn clean compile
    ```

5.  **Run tests (if available)**:
    ```bash
    mvn test
    ```

6.  **Run the application**:
    ```bash
    mvn exec:java -Dexec.mainClass="com.qrscanner.QRScannerApp"
    ```

## Code Style and Standards

### Java Code Style

-   **Indentation**: 4 spaces (no tabs)
-   **Line Length**: Maximum 120 characters
-   **Naming Conventions**:
    -   Classes: PascalCase (`QRScannerApp`)
    -   Methods: camelCase (`processQRCode`)
    -   Constants: UPPER_SNAKE_CASE (`MAX_HISTORY_SIZE`)
    -   Variables: camelCase (`qrContent`)

### Documentation

-   **Javadoc**: All new public methods and classes should have Javadoc comments.
-   **Inline Comments**: Explain complex or non-obvious logic.
-   **README Updates**: Update the main `README.md` if your changes affect user-facing features.

## Submitting Changes

### Pull Request Process

1.  **Create a feature branch**:
    ```bash
    git checkout -b feature/your-feature-name
    ```

2.  **Make your changes**, following the code style guidelines.

3.  **Test your changes thoroughly** by running the application.

4.  **Commit your changes** with descriptive messages using the Conventional Commits specification.
    ```bash
    git commit -m "feat(history): add persistence to scan history
    
    - Save history to a local file on exit
    - Load history from file on startup
    - Add new setting to disable history saving"
    ```

5.  **Push to your fork**:
    ```bash
    git push origin feature/your-feature-name
    ```

6.  **Create a Pull Request** on GitHub with a clear title, a description of the changes, and a reference to any related issues.

## Testing Guidelines

### Manual Testing
Thorough manual testing is critical. Please test on at least one platform before submitting a PR:

-   **Windows**: Test WiFi connections, file scanning (PDF/Image), and the global hotkey.
-   **macOS**: Verify camera permissions, file scanning, and network setup.
-   **Linux**: Test with different desktop environments (e.g., GNOME, KDE).

### Performance Testing
-   The camera preview should remain smooth while scanning.
-   Memory usage should remain stable during long-running use.
-   The application startup time should be reasonably fast.

## Issue Guidelines

Please use the provided templates on GitHub for submitting bug reports or feature requests. Provide as much detail as possible, including your operating system, Java version, and steps to reproduce the issue.

## Development Areas

### Code Architecture
The project is structured into logical components for UI, core processing, and system integration.

```
com.qrscanner/
├── QRScannerApp.java          # Main application frame, orchestrates all components
├── CameraPanel.java           # UI panel for the live camera preview and capture
├── QRProcessor.java           # Core logic for decoding, generating, and processing all QR types
├── WiFiManager.java           # Platform-specific OS commands for WiFi management
├── SystemTrayManager.java     # Handles the system tray icon, menu, and notifications
├── HotkeyManager.java         # Manages the global hotkey listener (JNativeHook)
├── ScanHistoryManager.java    # In-memory storage and management of scan history
├── SettingsDialog.java        # UI window for application settings
├── HistoryDialog.java         # UI window for displaying the scan history
└── QRExportDialog.java        # UI window for creating and exporting new QR codes
```

**Key Concepts**:
-   **Swing GUI**: The user interface is built with Java Swing.
-   **SwingWorker**: Long-running tasks like file scanning, batch processing, and update checks are performed on background threads to keep the UI responsive.
-   **Manager Classes**: Logic is decoupled into dedicated "manager" classes (`WiFiManager`, `SystemTrayManager`, `ScanHistoryManager`, `HotkeyManager`) to keep `QRScannerApp.java` from becoming overly complex.

### Priority Areas for Contribution

Now that the core feature set is complete, here are some great areas for future contributions:

1.  **History Persistence**:
    -   The `ScanHistoryManager` is currently in-memory. A great improvement would be to save the history to a local file (JSON or CSV) on exit and load it on startup.
    -   Add an option in the Settings dialog to enable/disable history saving.

2.  **UI/UX Improvements**:
    -   Implement the "Look and Feel" theme switcher in the `SettingsDialog`.
    -   Improve the layout of the `QRExportDialog` with dedicated UI for creating specific QR types (e.g., WiFi, vCard).

3.  **Hotkey Configuration**:
    -   The `Ctrl + Alt + S` hotkey is currently hardcoded. Add a new tab to the `SettingsDialog` to allow users to customize this key combination.

4.  **Internationalization (i18n)**:
    -   Externalize UI strings (buttons, labels, dialogs) into resource bundles to allow for translation into other languages.

5.  **Performance Optimization**:
    -   Analyze memory usage when scanning large, multi-page PDFs and optimize if necessary.
    -   Refine the live preview generation in the `QRExportDialog` for very long text inputs.

Thank you for contributing to QR WiFi Scanner! 🚀