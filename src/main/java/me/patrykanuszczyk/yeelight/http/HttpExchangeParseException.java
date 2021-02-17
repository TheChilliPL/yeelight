package me.patrykanuszczyk.yeelight.http;

public class HttpExchangeParseException extends Exception {
    public HttpExchangeParseException(String message) {
        super(message);
    }

    public HttpExchangeParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
