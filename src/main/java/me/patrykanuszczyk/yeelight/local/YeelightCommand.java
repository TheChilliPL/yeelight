package me.patrykanuszczyk.yeelight.local;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.patrykanuszczyk.yeelight.JsonObject;
import me.patrykanuszczyk.yeelight.Utils;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

public class YeelightCommand implements JsonObject {
    public YeelightCommand() {}
    public YeelightCommand(int id, @Nonnull String method) {
        this(id, method, Utils.getEmptyObjectArray());
    }
    public YeelightCommand(int id, @Nonnull String method, @Nonnull Object... params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }
//    public YeelightCommand(int id, String method, Object param1, Object param2) {
//        this(id, method, new Object[]{param1, param2});
//    }
//    public YeelightCommand(int id, String method, Object param1, Object param2, Object param3) {
//        this(id, method, new Object[]{param1, param2, param3});
//    }
//    public YeelightCommand(int id, String method, Object param1, Object param2, Object param3, Object param4) {
//        this(id, method, new Object[]{param1, param2, param3, param4});
//    }

    public int id;
    public String method;
    public Object[] params;

    @Nonnull
    @Contract(value = "_ -> new", pure = true)
    public static YeelightCommand fromJson(@Nonnull String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, YeelightCommand.class);
    }
}
