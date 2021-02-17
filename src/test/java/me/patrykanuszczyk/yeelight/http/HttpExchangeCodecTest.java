package me.patrykanuszczyk.yeelight.http;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class HttpExchangeCodecTest {
    @Test
    void decodeRequest() throws Exception {
        var http = "GET /index.html HTTP/1.1\n"
            + "h1: v1\n"
            + "h2: v2\n"
            + "\n"
            + "Body\n";

        var request = HttpExchangeCodec.decodeRequest(http);

        assertEquals("GET", request.getMethod());
        assertEquals("/index.html", request.getUriString());
        assertEquals(new URI("/index.html"), request.getUri());
        assertEquals(2, request.getHeaders().size());
        assertEquals("v1", request.getHeader("h1"));
        assertEquals("v2", request.getHeader("h2"));
        assertNull(request.getHeader("h3"));
        assertEquals("Body", request.getBody());
    }

    @Test
    void decodeResponse() throws Exception {
        var http = "HTTP/1.1 200 OK\n"
            + "h1: v1\n"
            + "h2: v2\n"
            + "\n"
            + "Body\n";

        var response = HttpExchangeCodec.decodeResponse(http);

        assertEquals(response.getHttpVersion(), "HTTP/1.1");
        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.getReason(), "OK");
        assertTrue(response.isSuccess());
        assertEquals(2, response.getHeaders().size());
        assertEquals("v1", response.getHeader("h1"));
        assertEquals("v2", response.getHeader("h2"));
        assertNull(response.getHeader("h3"));
        assertEquals("Body", response.getBody());
    }
}