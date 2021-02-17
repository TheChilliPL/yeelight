package me.patrykanuszczyk.yeelight;

public class Application {
    public static void main(String[] args) {
        try {
//            var connector = new YeelightConnector();
//
//            connector.startActiveScan();
//            var device = connector.waitForNewDevice().get();
//            connector.stopAllScans();
//
//            device.setKeepConnectionActive(true);
//
//            device.toggle().join();
//            System.out.println("Toggled");

            try(var clapDetector = new ClapDetector()) {
                while(true);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}