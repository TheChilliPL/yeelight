package me.patrykanuszczyk.yeelight;

/**
 * Thrown to indicate that the program tried to do something that the Yeelight
 * Device doesn't support.
 * Always check if the device supports some control method by using
 * {@link YeelightDevice#supports}.
 */
public class MethodNotSupportedException extends Exception {
    public MethodNotSupportedException() {
        super();
    }

    public MethodNotSupportedException(String method) {
        super("Method \"" + method + "\" isn't supported by this device.");
    }
}
