package me.patrykanuszczyk.yeelight.http;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class HttpResponse extends HttpExchange {

    HttpResponse(
        @Nonnull String httpVersion,
        short statusCode,
        @Nonnull String reason,
        @Nonnull Map<String, String> headers,
        @Nullable String body
    ) {
        super(httpVersion, headers, body);
        this.statusCode = statusCode;
        this.reason = reason;
    }

    private short statusCode;
    private String reason;

    public short getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(short statusCode) {
        this.statusCode = statusCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isSuccess() { return !isError(); }
    public boolean isError() { return getStatusCode() >= 400; }
}
