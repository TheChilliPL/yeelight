package me.patrykanuszczyk.yeelight.local;

import me.patrykanuszczyk.yeelight.Utils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {
    @Test
    void yeelightCommandTranslate() throws Exception {
        var c1 = new YeelightCommand(
            1,
            "set_hsv",
            255, 45, "smooth", 500
        );

        var json = c1.toJson();

        var c2 = YeelightCommand.fromJson(json);

        assertEquals(c1.id, c2.id);
        assertEquals(c1.method, c2.method);
        assertArrayEquals(c1.params, c2.params);
    }

    @Test
    void yeelightResultTranslate() throws Exception {
        var r1 = new YeelightResult();
        r1.id = 2;
        r1.result = Utils.getEmptyStringArray();

        var json = r1.toJson();

        var r2 = (YeelightResult) YeelightResponse.fromJson(json);

        assertEquals(r1.id, r2.id);
        assertArrayEquals(r1.result, r2.result);
        assertNull(r2.error);
        assertTrue(r2.isSuccess());
    }

    @Test
    void yeelightErrorTranslate() throws Exception {
        var r1 = new YeelightResult();
        r1.id = 2;
        r1.error = new YeelightError();
        r1.error.code = -1;
        r1.error.message = "invalid command";

        var json = r1.toJson();

        var r2 = (YeelightResult) YeelightResponse.fromJson(json);

        assertEquals(r1.id, r2.id);
        assertArrayEquals(r1.result, r2.result);
        assertNotNull(r2.error);
        assertEquals(r1.error.code, r2.error.code);
        assertEquals(r1.error.message, r2.error.message);
        assertFalse(r2.isSuccess());
    }

    @Test
    void yeelightNotificationTranslate() throws Exception {
        var r1 = new YeelightNotification();
        r1.method = "props";
        r1.params = Map.of("power", "on", "bright", "10");

        var json = r1.toJson();

        var r2 = (YeelightNotification) YeelightResponse.fromJson(json);

        assertEquals(r1.method, r2.method);
        assertEquals(r1.params, r2.params);
    }
}