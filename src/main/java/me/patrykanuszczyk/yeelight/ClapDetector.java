package me.patrykanuszczyk.yeelight;

import javax.sound.sampled.*;

public class ClapDetector implements AutoCloseable {
    public ClapDetector() {
        thread.setDaemon(true);
        thread.start();
    }

    private final Thread thread = new Thread(() -> {
        var format = new AudioFormat(
            44100,
            16,
            1,
            false,
            false);

        while(!Thread.interrupted()) {
            try {
                var line = AudioSystem.getTargetDataLine(format);
                line.open();
                var level = line.getLevel();
                System.out.println(level);
            } catch(LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    public void close() {
        while(true) {
            try {
                thread.interrupt();
                thread.join();
                break;
            } catch(InterruptedException ignored) {}
        }
    }
}
