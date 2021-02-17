package me.patrykanuszczyk.yeelight;

public class NotImplementedException extends RuntimeException {
    public NotImplementedException() {
        super("Method is not implemented yet.");
    }

    public NotImplementedException(String message) {
        super(message);
    }
}
