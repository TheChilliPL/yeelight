package me.patrykanuszczyk.yeelight.local;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.patrykanuszczyk.yeelight.JsonObject;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

public abstract class YeelightResponse implements JsonObject {
    @Nonnull
    @Contract(value = "_ -> new", pure = true)
    public static YeelightResponse fromJson(@Nonnull String json) throws JsonProcessingException {
        return new ObjectMapper().readTree(json).has("id")
            ? YeelightResult.fromJson(json)
            : YeelightNotification.fromJson(json);
    }
}
