package me.patrykanuszczyk.yeelight.local;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.patrykanuszczyk.yeelight.CacheMode;
import me.patrykanuszczyk.yeelight.MethodNotSupportedException;
import me.patrykanuszczyk.yeelight.YeelightDevice;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public abstract class LocalYeelightDevice extends YeelightDevice {
    public String getProperty(String key) {
        return getProperty(key, CacheMode.ANY);
    }

    public abstract String getProperty(String key, @Nonnull CacheMode cacheMode);

    public CompletableFuture<YeelightResult> sendCommand(String method, Object... arguments)
        throws JsonProcessingException, MethodNotSupportedException
    {
        ensureSupports(method);
        return forceSendCommand(
            method,
            arguments
        );
    }

    public CompletableFuture<YeelightResult> forceSendCommand(String method, Object... arguments)
        throws JsonProcessingException
    {
        return forceSendCommand(
            new YeelightCommand(
                getNextCommandId(),
                method,
                arguments
            )
        );
    }

    public abstract CompletableFuture<YeelightResult> forceSendCommand(YeelightCommand command) throws
        JsonProcessingException;

    public abstract int getNextCommandId();

    @Override
    public CompletableFuture<String[]> fetchProperties(String... keys)
        throws MethodNotSupportedException, JsonProcessingException
    {
        return sendCommand("get_prop", (Object[]) keys)
            .thenApplyAsync(result -> result.result);
    }

    @Override
    public CompletableFuture<Void> setColorTemperature(short temperature, int duration) {
        return null;
    }

    @Override
    public CompletableFuture<Void> toggle() throws MethodNotSupportedException, JsonProcessingException {
        return sendCommand("toggle").thenAccept(ignored -> {});
    }
}
