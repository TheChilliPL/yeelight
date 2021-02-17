package me.patrykanuszczyk.yeelight.local;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.patrykanuszczyk.yeelight.CacheMode;
import me.patrykanuszczyk.yeelight.ColorFlow;
import me.patrykanuszczyk.yeelight.MethodNotSupportedException;
import me.patrykanuszczyk.yeelight.YeelightDevice;
import me.patrykanuszczyk.yeelight.http.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LocalYeelightDeviceImpl extends LocalYeelightDevice {
    public LocalYeelightDeviceImpl(long id, URI uri) {
        this.id = id;
        this.uri = uri;
    }

    Logger logger = LoggerFactory.getLogger(LocalYeelightDeviceImpl.class);

    long id;

    @Override
    public long getId() {
        return id;
    }

    URI uri;

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    protected void setUri(URI uri) {
        this.uri = uri;
    }

    String model;

    @Override
    public String getModel() {
        return model;
    }

    int firmwareVersion;

    @Override
    public int getFirmwareVersion() {
        return firmwareVersion;
    }

    @Override
    protected void setFirmwareVersion(int value) {
        this.firmwareVersion = value;
    }

    @Override
    public String getName() {
        return getProperty("name");
    }

    Set<String> supportedMethods;

    @Override
    public Set<String> getSupportedMethods() {
        return supportedMethods;
    }

    Long lastCacheRenew;
    long cacheMaxAge = 60_000_000_000L;
    Map<String, String> propCache = new HashMap<>();
    ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    @SuppressWarnings("SpellCheckingInspection")
    Set<String> cacheable = new HashSet<>(
        List.of(
            "power",
            "bright",
            "ct",
            "rgb",
            "hue",
            "sat",
            "color_mode",
            "flowing",
            "delayoff",
            "flow_params",
            "music_on",
            "name",
            "bg_power",
            "bg_flowing",
            "bg_flow_params",
            "bg_ct",
            "bg_lmode",
            "bg_bright",
            "bg_rgb",
            "bg_hue",
            "bg_sat",
            "nl_br",
            "active_mode"
        )
    );

    @Override
    public boolean isCacheValid() {
        cacheLock.readLock().lock();
        try {
            if(lastCacheRenew == null) return false;
            return System.nanoTime() - lastCacheRenew <= cacheMaxAge;
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public void invalidateCache() {
        cacheLock.writeLock().lock();
        try {
            lastCacheRenew = null;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public void updateCache() {
        cacheLock.writeLock().lock();
        try {
            invalidateCache();

            var cacheKeys = cacheable.toArray(String[]::new);
            var cacheValues = fetchProperties(cacheKeys).get();

            for(var i = 0; i < cacheKeys.length; i++) {
                propCache.put(cacheKeys[i], cacheValues[i]);
            }

            lastCacheRenew = System.nanoTime();
        } catch(MethodNotSupportedException
            | JsonProcessingException
            | InterruptedException
            | ExecutionException e) {
            logger.warn("Update cache exception occurred.", e);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, CacheMode.ANY);
    }


    @Override
    public String getProperty(String key, @Nonnull CacheMode cacheMode) {
        key = key.toLowerCase(Locale.ROOT);

        if(cacheMode.allowsCache(isCacheValid())) {
            cacheLock.readLock().lock();
            try {
                String cacheEntry = propCache.get(key);

                if(cacheEntry != null) return cacheEntry;
            } finally {
                cacheLock.readLock().unlock();
            }
        }

        if(!cacheMode.allowsFetchNew()) return null;

        cacheable.add(key);
        updateCache();

        return getProperty(key, CacheMode.FORCE_CACHE_ONLY);
    }

    @Override
    public void update(@Nonnull HttpExchange httpExchange) {
        var supportHeader = httpExchange.getHeader("support");
        supportedMethods = supportHeader == null
            ? Set.of()
            : Set.of(supportHeader.split("\\s+")); //TODO Thread-safe solution

        cacheLock.writeLock().lock();
        try {
            for(var prop : cacheable) {
                var value = httpExchange.getHeader(prop);
                //if(value != null)
                propCache.put(prop, value);
            }

            lastCacheRenew = System.nanoTime();
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    boolean keepConnectionActive = false;
    Socket connection;
    Thread connectionThread;
    ReentrantLock connectionLock = new ReentrantLock();
    Map<Integer, CompletableFuture<YeelightResult>> pendingResults = new HashMap<>(5);

    @Override
    public boolean getKeepConnectionActive() {
        return keepConnectionActive;
    }

    @Override
    public void setKeepConnectionActive(Boolean keepActive) throws IOException {
        keepConnectionActive = keepActive;
        if(keepActive) ensureConnected();
    }

    @Override
    public boolean isConnectionActive() {
        if(connection == null) return false;
        return connection.isConnected();
    }

    /**
     * <p>
     * Attempts to connect to the device if the connection is not active.
     * Gets automatically called by {@link #forceSendCommand} (along with the
     * actual commands from the {@link YeelightDevice} abstract class) and
     * {@link #setKeepConnectionActive} when <code>keepActive</code> is
     * <code>true</code>.
     * </p>
     * <p>
     * If {@link #getKeepConnectionActive()} returns <code>false</code>,
     * the connection will be closed in a few seconds.
     * </p>
     * <p>
     * If the connection is already active, it does nothing.
     * </p>
     */
    @Override
    public void ensureConnected() throws IOException {
        connectionLock.lock();
        try {
            if(connection != null) return;

            if(connectionThread != null) {
                connectionThread.interrupt();
                connectionThread.join();
            }

            connection = new Socket(
                getUri().getHost(),
                getUri().getPort(),
                null,
                0
            );

            var inputStream = connection.getInputStream();

            connection.setKeepAlive(true);

            connectionThread = new Thread(() -> {
                try {
                    while(!Thread.interrupted()) {
                        String json;
                        //connectionLock.lock();
                        try {
                            logger.info("Receiving packet");
//                            json = new String(
//                                inputStream.readAllBytes(),
//                                StandardCharsets.UTF_8
//                            ).trim();
                            var data = new byte[4096];
                            var i = 0;
                            while(true) {
                                //logger.info("a");
                                var b = inputStream.read();
                                //logger.info("b");
                                if(b < 0 || b == '\r' || b == '\n')
                                    break;
                                data[i++] = (byte) b;
                            }
                            if(i == 0) continue;

                            json = new String(
                                data,
                                0,
                                i,
                                StandardCharsets.UTF_8
                            );
                        } finally {
                            //connectionLock.unlock();
                        }
                        logger.trace("Received response: {}.", json);
                        var response = YeelightResponse.fromJson(json);
                        if(response instanceof YeelightResult) {
                            var result = (YeelightResult) response;

                            var future = pendingResults.remove(result.id);

                            if(future == null) {
                                logger.warn("Received result to unknown command.");
                                continue;
                            }

                            future.complete(result);
                            logger.trace("Completed pending command.");
                        } else if(response instanceof YeelightNotification) {
                            var notification = (YeelightNotification) response;

                            if(!"props".equals(notification.method)) {
                                logger.warn("Received unsupported notification type.");
                            }

                            cacheLock.writeLock().lock();
                            try {
                                invalidateCache();
                                for(var key : notification.params.keySet()) {
                                    var value = notification.params.get(key);
                                    if(value != null)
                                        propCache.put(key, value);
                                }
                                lastCacheRenew = System.nanoTime();
                                logger.info("Cache updated");
                            } catch(RuntimeException e) {
                                // Cache may be corrupted.
                                invalidateCache();
                                throw e;
                            } finally {
                                cacheLock.writeLock().unlock();
                            }
                        }
                    }
                    connectionLock.lock();
                    try {
                        connection.close();
                    } finally {
                        connectionLock.unlock();
                    }
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            });
            connectionThread.start();
        } catch(InterruptedException e) {
            logger.error("Thread was interrupted.", e);
        } finally {
            connectionLock.unlock();
        }
    }

    @Override
    public CompletableFuture<Void> setColor(Color color, int duration) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setBrightness(short brightness, int duration) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setPower(boolean on, int duration) {
        return null;
    }

    @Override
    public CompletableFuture<Void> saveAsDefaults() {
        return null;
    }

    @Override
    public CompletableFuture<Void> startFlow(ColorFlow flow) {
        return null;
    }

    @Override
    public CompletableFuture<Void> stopFlow() {
        return null;
    }

    @Override
    public CompletableFuture<Void> scheduleAutoOff(int minutes) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> getAutoOff() {
        return null;
    }

    @Override
    public CompletableFuture<Void> cancelAutoOff() {
        return null;
    }

    @Override
    public CompletableFuture<Void> startExclusiveMode() {
        return null;
    }

    @Override
    public CompletableFuture<Void> stopExclusiveMode() {
        return null;
    }

    @Override
    public CompletableFuture<Void> setName(String name) {
        return null;
    }

    @Override
    public CompletableFuture<Void> adjustBrightness(short change, int duration) {
        return null;
    }

    @Override
    public CompletableFuture<Void> adjustColorTemperature(short change, int duration) {
        return null;
    }

    int nextCommandId = 0;

    @Override
    public CompletableFuture<YeelightResult> forceSendCommand(YeelightCommand command) throws JsonProcessingException {
        var json = command.toJson();

        var completableFuture = new CompletableFuture<YeelightResult>();

        pendingResults.put(command.id, completableFuture);

        return CompletableFuture.runAsync(() -> {
            connectionLock.lock();
            try {
                ensureConnected();

                var stream = connection.getOutputStream();
                stream.write(
                    StandardCharsets.UTF_8.encode(
                        json + "\r\n"
                    ).array()
                );
                stream.flush();
                logger.info("Sent command {}.", json);
            } catch(IOException e) {
                throw new CompletionException(e);
            } finally {
                connectionLock.unlock();
            }
        }).thenCombine(completableFuture, (ignored, result) -> result);
    }

    @Override
    public int getNextCommandId() {
        return nextCommandId++;
    }
}
