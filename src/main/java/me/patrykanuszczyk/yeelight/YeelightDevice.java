package me.patrykanuszczyk.yeelight;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.patrykanuszczyk.yeelight.http.HttpExchange;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class YeelightDevice {
    public abstract long getId();

    public abstract URI getUri();

    protected abstract void setUri(URI uri);

    public abstract String getModel();

    public abstract int getFirmwareVersion();

    protected abstract void setFirmwareVersion(int value);

    public abstract String getName();

    public abstract Set<String> getSupportedMethods();

    public boolean supports(String method) {
        return getSupportedMethods().contains(method);
    }

    public void ensureSupports(String method) throws MethodNotSupportedException {
        if(!supports(method))
            throw new MethodNotSupportedException(method);
    }

    public abstract boolean isCacheValid();

    public abstract void invalidateCache();

    public abstract void updateCache();

    public abstract void update(@Nonnull HttpExchange httpExchange);

    public abstract boolean getKeepConnectionActive();

    public abstract void setKeepConnectionActive(Boolean keepActive) throws IOException;

    public abstract boolean isConnectionActive();

    public abstract void ensureConnected() throws IOException;

    //region Control methods
    public abstract CompletableFuture<String[]> fetchProperties(String ...keys)
        throws MethodNotSupportedException, JsonProcessingException;

    public abstract CompletableFuture<Void> setColorTemperature(short temperature, int duration);

    public abstract CompletableFuture<Void> setColor(Color color, int duration);

    public abstract CompletableFuture<Void> setBrightness(short brightness, int duration);

    public abstract CompletableFuture<Void> setPower(boolean on, int duration);

    public abstract CompletableFuture<Void> toggle() throws MethodNotSupportedException, JsonProcessingException;

    public abstract CompletableFuture<Void> saveAsDefaults();

    public abstract CompletableFuture<Void> startFlow(ColorFlow flow);

    public abstract CompletableFuture<Void> stopFlow();

    //public abstract CompletableFuture<Void> setScene(Scene scene);

    public abstract CompletableFuture<Void> scheduleAutoOff(int minutes);

    public abstract CompletableFuture<Integer> getAutoOff();

    public abstract CompletableFuture<Void> cancelAutoOff();

    public abstract CompletableFuture<Void> startExclusiveMode();

    public abstract CompletableFuture<Void> stopExclusiveMode();

    public abstract CompletableFuture<Void> setName(String name);

    public abstract CompletableFuture<Void> adjustBrightness(short change, int duration);

    public abstract CompletableFuture<Void> adjustColorTemperature(short change, int duration);
    //endregion
}
