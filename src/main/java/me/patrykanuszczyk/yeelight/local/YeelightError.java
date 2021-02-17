package me.patrykanuszczyk.yeelight.local;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

public class YeelightError {
    public int code;
    public String message;

    @Nonnull
    @Contract(value = "_ -> new", pure = true)
    public static YeelightError fromJson(@Nonnull String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, YeelightError.class);
    }
}

