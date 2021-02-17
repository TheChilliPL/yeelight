package me.patrykanuszczyk.yeelight.http;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class HttpExchange {
    HttpExchange(
        @Nonnull String httpVersion,
        @Nonnull Map<String, String> headers,
        @Nullable String body
    ) {
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;
    }

    @Nonnull private String httpVersion;
    @Nonnull private Map<String, String> headers;
    @Nullable private String body;

    @Nonnull
    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(@Nonnull String httpVersion) {
        this.httpVersion = httpVersion;
    }

    @Nonnull
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(@Nonnull Map<String, String> headers) {
        this.headers = headers;
    }

    @Nullable
    public String getBody() {
        return body;
    }

    public void setBody(@Nullable String body) {
        this.body = body;
    }

    @Nullable
    public String getHeader(@Nonnull String header) {
        for(var pair : headers.entrySet()) {
            if(pair.getKey().equalsIgnoreCase(header)) {
                return pair.getValue();
            }
        }

        return null;
    }

    public String getHeaderOrDefault(@Nonnull String header, String def) {
        var value = getHeader(header);
        return value == null ? def : value;
    }
}

