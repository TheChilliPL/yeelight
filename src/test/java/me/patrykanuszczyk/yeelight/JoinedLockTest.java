package me.patrykanuszczyk.yeelight;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class JoinedLockTest {
    @Test
    void joinedLockTest() {
        assertTrue(true);
        Lock a = new ReentrantLock();
        Lock b = new ReentrantLock();
        Lock c = new JoinedLock(a, b);

        if(c.tryLock()) c.unlock();
        else fail();

        if(a.tryLock()) {
            try {
                CompletableFuture.runAsync(() -> {
                    if(c.tryLock()) {
                        c.unlock();
                        fail();
                    }

                    if(b.tryLock()) {
                        try {
                            CompletableFuture.runAsync(() -> {
                                if(c.tryLock()) {
                                    c.unlock();
                                    fail();
                                }
                            }).join();
                        } catch(Exception e) { fail(e); }
                        finally { b.unlock(); }
                    } else fail();
                }).join();
            } catch(Exception e) { fail(e); }
            finally { a.unlock(); }
        } else fail();

        if(b.tryLock()) {
            try {
                CompletableFuture.runAsync(() -> {
                    if(c.tryLock()) {
                        c.unlock();
                        fail();
                    }
                }).join();
            } finally { b.unlock(); }
        } else fail();
    }
}