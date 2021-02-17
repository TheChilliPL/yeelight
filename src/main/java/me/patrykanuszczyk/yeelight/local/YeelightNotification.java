package me.patrykanuszczyk.yeelight.local;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import java.util.Map;

public class YeelightNotification extends YeelightResponse {
    public String method;
    public Map<String, String> params;

    @Nonnull
    @Contract(value = "_ -> new", pure = true)
    public static YeelightNotification fromJson(@Nonnull String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, YeelightNotification.class);
    }
}
