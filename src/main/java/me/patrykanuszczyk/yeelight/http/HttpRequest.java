package me.patrykanuszczyk.yeelight.http;

import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class HttpRequest extends HttpExchange {
    HttpRequest(
        @Nonnull String method,
        @Nonnull String uri,
        @Nonnull String httpVersion,
        @Nonnull Map<String, String> headers,
        @Nullable String body
    ) {
        super(httpVersion, headers, body);
        this.method = method;
        this.uri = uri;
    }

    @Nonnull private String method;
    @Nonnull private String uri;

    @Nonnull
    public String getMethod() {
        return method;
    }

    public void setMethod(@Nonnull String method) {
        this.method = method;
    }

    @Nonnull
    public String getUriString() {
        return uri;
    }

    public void setUriString(@Nonnull String uri) {
        this.uri = uri;
    }

    @Nonnull @Contract(pure = true)
    public URI getUri() throws URISyntaxException {
        return new URI(getUriString());
    }
}
