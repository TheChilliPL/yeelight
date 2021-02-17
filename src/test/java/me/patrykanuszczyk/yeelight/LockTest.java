//package me.patrykanuszczyk.yeelight;
//
//import org.junit.jupiter.api.Test;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class LockTest {
//    @Test
//    void lockTest() {
//        var lock = new ReentrantLock();
//        lock.lock();
//        try {
//            CompletableFuture.runAsync(() -> {
//                try {
//                    Thread.sleep(5000);
//                    throw new RuntimeException();
//                } catch(InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                } finally {
//                    lock.unlock();
//                }
//            }).get();
//        } catch(Exception e) {
//            lock.unlock();
//            e.printStackTrace();
//        }
//    }
//}
