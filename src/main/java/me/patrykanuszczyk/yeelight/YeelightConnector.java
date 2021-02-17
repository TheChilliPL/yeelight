package me.patrykanuszczyk.yeelight;

import me.patrykanuszczyk.yeelight.http.HttpExchange;
import me.patrykanuszczyk.yeelight.http.HttpExchangeCodec;
import me.patrykanuszczyk.yeelight.http.HttpExchangeParseException;
import me.patrykanuszczyk.yeelight.local.LocalYeelightDeviceImpl;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class YeelightConnector {
    Logger logger = LoggerFactory.getLogger(YeelightConnector.class);
    Logger passiveScanLogger =
        LoggerFactory.getLogger(YeelightConnector.class.getName() +
            "#passiveScanThread");
    Logger activeScanLogger =
        LoggerFactory.getLogger(YeelightConnector.class.getName() +
            "#activeScanThread");

    public YeelightConnector() {
        logger.info("Constructed a new Yeelight connector.");
    }

    private boolean stopPassive = true;
    @Nullable
    private Thread passiveScanThread = null;
    private boolean stopActive = true;
    @Nullable
    private Thread activeScanThread = null;
    private long activeScanInterval = 15_000_000_000L;
    private final int bufferSize = 4096;

    private final ReadWriteLock devicesLock = new ReentrantReadWriteLock();
    private final Map<Long, YeelightDevice> devices = new HashMap<>(4) {
        @Override
        public YeelightDevice put(Long key, YeelightDevice value) {
            try {
                return super.put(key, value);
            } finally {
                deviceAwaiterLock.lock();
                try {
                    deviceAwaiter.complete(value);
                } finally {
                    deviceAwaiterLock.unlock();
                }
            }
        }
    };

    public void startPassiveScan() {
        if(isPassiveScanOn())
            throw new IllegalThreadStateException("Passive scan is already on.");
        stopPassive = false;
        passiveScanThread = new Thread(() -> {
            MulticastSocket socket = null;
            try {
                socket = new MulticastSocket(1982);
                passiveScanLogger.info(
                    "Started YeelightConnector passive thread..."
                );
                socket.setReuseAddress(true);
                socket.setSoTimeout(5000);
                socket.joinGroup(
                    new InetSocketAddress(
                        "239.255.255.250",
                        1982
                    ),
                    null
                );
                byte[] buffer = new byte[bufferSize];
                var incoming = new DatagramPacket(
                    buffer, bufferSize
                );
                while(!stopPassive) {
                    try {
                        passiveScanLogger.trace("Receiving packet...");
                        socket.receive(incoming);
                        var response = new String(
                            incoming.getData(),
                            incoming.getOffset(),
                            incoming.getLength(),
                            StandardCharsets.UTF_8
                        );
                        passiveScanLogger.trace("Socket packet received.");
                        var httpReq = HttpExchangeCodec.decodeRequest(response);
                        if(!httpReq.getMethod().equalsIgnoreCase("NOTIFY")) {
                            passiveScanLogger.trace(
                                "HTTP method is {}, not NOTIFY. Ignoring packet.",
                                httpReq.getMethod());
                            continue;
                        }

                        // It's a valid Yeelight device.
                        handleDevice(httpReq, passiveScanLogger);
                    } catch(SocketTimeoutException ignored) {
                        passiveScanLogger.trace("Socket timeout ignored.");
                    } catch(HttpExchangeParseException e) {
                        passiveScanLogger.trace("Parsing HTTP request failed.");
                    } catch(URISyntaxException e) {
                        passiveScanLogger.trace("Location wasn't a valid URI.");
                    }
                }
            } catch(IOException e) {
                passiveScanLogger.error("An exception occurred.", e);
                //TODO Better exception handling.
            } finally {
                if(socket != null)
                    try { socket.close(); } catch(Exception ignored) {}

                stopPassive = true;
                passiveScanThread = null;
            }
        });
        passiveScanThread.start();
    }

    public void stopPassiveScan() {
        stopPassive = true;
    }

    public boolean isPassiveScanOn() {
        return !stopPassive;
    }

    public void startActiveScan() {
        if(isActiveScanOn())
            throw new IllegalThreadStateException("Active scan is already on.");
        stopActive = false;
        activeScanThread = new Thread(() -> {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(0);
                activeScanLogger.info(
                    "Started YeelightConnector active thread..."
                );
                socket.setSoTimeout(5000);
                var searchData = ("M-SEARCH * HTTP/1.1\r\n"
                    + "MAN: \"ssdp:discover\"\r\n"
                    + "ST: wifi_bulb\r\n")
                    .getBytes(StandardCharsets.UTF_8);
                var searchRequest = new DatagramPacket(
                    searchData,
                    searchData.length,
                    InetAddress.getByName("239.255.255.250"),
                    1982
                );
                byte[] buffer = new byte[bufferSize];
                var incoming = new DatagramPacket(
                    buffer, bufferSize
                );
                var lastTimeSent = System.nanoTime();
                while(!stopActive) {
                    if(System.nanoTime() - lastTimeSent < activeScanInterval) {
                        try {
                            socket.send(searchRequest);
                            activeScanLogger.debug("Sent a search request...");
                        } catch(IOException e) {
                            activeScanLogger.warn("Error sending a search request.", e);
                        }
                    }
                    try {
                        activeScanLogger.trace("Receiving packet...");
                        socket.receive(incoming);
                        var response = new String(
                            incoming.getData(),
                            incoming.getOffset(),
                            incoming.getLength(),
                            StandardCharsets.UTF_8
                        );
                        var httpRes = HttpExchangeCodec.decodeResponse(response);
                        if(httpRes.isError()) {
                            activeScanLogger.trace(
                                "HTTP response has returned an error status {}. Ignoring packet.",
                                httpRes.getStatusCode()
                            );
                            continue;
                        }

                        // It's a valid Yeelight device
                        handleDevice(httpRes, activeScanLogger);
                    } catch(SocketTimeoutException ignored) {
                        activeScanLogger.trace("Socket timeout ignored.");
                    } catch(HttpExchangeParseException ignored) {
                        activeScanLogger.trace("Parsing HTTP request failed.");
                    } catch(URISyntaxException ignored) {
                        activeScanLogger.trace("Location wasn't a valid URI.");
                    }
                }
            } catch(IOException e) {
                activeScanLogger.error("An exception occurred.", e);
                //TODO Better exception handling.
            } finally {
                if(socket != null)
                    try { socket.close(); } catch(Exception ignored) {}

                stopActive = true;
                activeScanThread = null;
            }
        });
        activeScanThread.start();
    }

    public void stopActiveScan() {
        stopActive = true;
    }

    public boolean isActiveScanOn() {
        return !stopActive;
    }

    public void setActiveScanInterval(@Nonnull @Nonnegative Duration duration) {
        this.activeScanInterval = duration.toNanos();
    }

    public void stopAllScans() {
        stopPassive = stopActive = true;
    }

    /**
     * Handles a device update in a form of HTTP exchange.
     * @param exchange HTTP exchange from the device.
     * @param logger Logger used to log debug information (optional).
     * @return Added/updated device, or <code>null</code> if the request is invalid.
     * @throws URISyntaxException If location is not a valid {@link URI}
     */
    @SuppressWarnings("UnusedReturnValue")
    @Nullable
    private YeelightDevice handleDevice(@Nonnull HttpExchange exchange, @Nullable Logger logger)
        throws URISyntaxException, IOException
    {
        var location = exchange.getHeader("location");
        if(location == null) {
            if(logger != null) {
                logger.trace("Location header isn't there. Ignoring packet.");
            }
            return null;
        }
        var uri = new URI(location);
        if(!uri.getScheme().equalsIgnoreCase("yeelight")) {
            if(logger != null) {
                logger.trace("Location URI scheme isn't yeelight. Ignoring packet.");
            }
            return null;
        }
        var idString = exchange.getHeader("id");
        if(idString == null) {
            if(logger != null) {
                logger.trace("ID header isn't there.");
            }
            return null;
        }
        if(idString.startsWith("0x")) idString = idString.substring(2);
        var idBytes = Utils.hexToBytes(idString);
        var id = Utils.bytesToLongBE(idBytes);

        YeelightDevice device;
        boolean newDevice;
        devicesLock.writeLock().lock();
        try {
            device = devices.get(id);

            newDevice = device == null;

            if(newDevice) {
                // New device
                device = new LocalYeelightDeviceImpl(
                    id, uri
                );
                device.update(exchange);
                devices.put(id, device);
            } else {
                device.update(exchange);
            }
        } finally {
            devicesLock.writeLock().unlock();
        }

        if(logger != null) {
            var logName = device.getName();
            var logId = device.getId();
            var logUri = device.getUri();

            if(newDevice) {
                logger.info(
                    "New device {} \"{}\" @ {}",
                    logId, logName, logUri
                );
            } else {
                logger.debug(
                    "Updated device {} \"{}\" @ {}",
                    logId, logName, logUri
                );
            }
        }

        return device;
    }

    @Contract(pure = true)
    public Collection<YeelightDevice> getDevices() {
        synchronized(devices) {
            return devices.values();
        }
    }

    @Contract(pure = true)
    public int countDevices() {
        synchronized(devices) {
            return devices.size();
        }
    }

    Lock deviceAwaiterLock = new ReentrantLock();
    private CompletableFuture<YeelightDevice> deviceAwaiter = null;
    public CompletableFuture<YeelightDevice> waitForNewDevice() {
        deviceAwaiterLock.lock();
        try {
            if(deviceAwaiter != null && !deviceAwaiter.isDone()) return deviceAwaiter;
            return deviceAwaiter = new CompletableFuture<>();
        } finally {
            deviceAwaiterLock.unlock();
        }
    }
}