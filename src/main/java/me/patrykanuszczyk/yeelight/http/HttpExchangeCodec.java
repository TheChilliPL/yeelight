package me.patrykanuszczyk.yeelight.http;

import me.patrykanuszczyk.yeelight.Pair;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Static class encoding/decoding HTTP exchanges to/from strings.
 */
public class HttpExchangeCodec {
    private HttpExchangeCodec() {}

    /**
     * Decodes an HTTP request from a string.<br>
     * Supports <code>\r</code>, <code>\n</code> and <code>\r\n</code> line endings.
     * @param request HTTP string to decode.
     * @return Parsed HTTP request.
     * @throws HttpExchangeParseException When parsing encounters an error.
     */
    @Nonnull
    @Contract("_ -> new")
    public static HttpRequest decodeRequest(String request)
        throws HttpExchangeParseException
    {
        try {
            var lines = request.lines().toArray(String[]::new);

            var firstLine = lines[0];
            var meta = firstLine.strip().split("\\s+", 3);
            var method = meta[0];
            var uri = meta[1];
            var httpVersion = meta[2];

            var pair = parseHeadersAndBody(
                Arrays.stream(lines).skip(1).toArray(String[]::new)
            );
            var headers = pair.first;
            var body = pair.second;

            return new HttpRequest(method, uri, httpVersion, headers, body);
        } catch(Exception e) {
            throw new HttpExchangeParseException(
                "HTTP exchange codec encountered an error when parsing a "
                    + "request.",
                e
            );
        }
    }

    /**
     * Decodes an HTTP response from a string.<br>
     * Supports <code>\r</code>, <code>\n</code> and <code>\r\n</code> line endings.
     * @param response HTTP string to decode.
     * @return Parsed HTTP response.
     * @throws HttpExchangeParseException When parsing encounters an error.
     */
    @Nonnull
    @Contract("_ -> new")
    public static HttpResponse decodeResponse(String response)
        throws HttpExchangeParseException
    {
        try {
            var lines = response.lines().toArray(String[]::new);

            var firstLine = lines[0];
            var meta = firstLine.strip().split("\\s+", 3);
            var httpVersion = meta[0];
            var statusCode = Short.parseShort(meta[1]);
            var reason = meta[2];

            var pair = parseHeadersAndBody(
                Arrays.stream(lines).skip(1).toArray(String[]::new)
            );
            var headers = pair.first;
            var body = pair.second;

            return new HttpResponse(httpVersion, statusCode, reason, headers,
                body);
        } catch(Exception e) {
            throw new HttpExchangeParseException(
                "HTTP exchange codec encountered an error when parsing a "
                    + "request.",
                e
            );
        }
    }

    @Nonnull
    static Pair<Map<String, String>, String> parseHeadersAndBody(
        @Nonnull String[] lines
    ) {
        var headers = new HashMap<String, String>();

        int i;
        for(i = 0; i < lines.length; i++) {
            if(lines[i].isBlank()) break;

            var header = lines[i].split(":", 2);

            headers.put(header[0].strip(), header[1].strip());
        }

        var body = String.join("\n",
            Arrays.stream(lines).skip(i+1).toArray(String[]::new));

        return new Pair<>(headers, body);
    }
}
