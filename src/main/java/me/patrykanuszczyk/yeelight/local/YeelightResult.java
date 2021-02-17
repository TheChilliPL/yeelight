package me.patrykanuszczyk.yeelight.local;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class YeelightResult extends YeelightResponse {
    public int id;
    public String[] result;
    public YeelightError error;

    public boolean isSuccess() {
        return !isError();
    }

    public boolean isError() {
        return error != null;
    }

    @Nonnull
    @Contract(value = "_ -> new", pure = true)
    public static YeelightResult fromJson(@Nonnull String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, YeelightResult.class);
    }
}
