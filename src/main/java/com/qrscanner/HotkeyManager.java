package com.qrscanner;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HotkeyManager implements NativeKeyListener {

    private final Runnable action;

    public HotkeyManager(Runnable action) {
        this.action = action;
    }

    public void initialize() {
        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.WARNING);
            logger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            System.out.println("Global hotkey listener (Ctrl + Alt + S) initialized.");
        } catch (NativeHookException e) {
            System.err.println("There was a problem registering the native hook for hotkeys.");
            System.err.println(e.getMessage());
        }
    }

    public void cleanup() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.unregisterNativeHook();
            System.out.println("Global hotkey listener cleaned up successfully.");
        } catch (NativeHookException e) {
            System.err.println("There was a problem unregistering the native hook.");
            System.err.println(e.getMessage());
        }
    }

    /**
     * CORRECTION: Use the modern getModifiers() method instead of tracking
     * individual key presses. This is the correct way for JNativeHook v2.2+.
     */
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_S) {
            boolean isCtrlDown = (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0;
            boolean isAltDown = (e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0;

            if (isCtrlDown && isAltDown) {
                if (action != null) {
                    action.run();
                }
            }
        }
    }

    // This method is now unused but required by the interface.
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // No action needed on key release with the new modifier logic.
    }
}