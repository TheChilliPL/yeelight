package me.patrykanuszczyk.yeelight;

public enum ColorMode {
    RGB_MODE(1),
    COLOR_TEMP_MODE(2),
    HSV_MODE(3),

    UNKNOWN(0);

    ColorMode(int value) {
        this.value = value;
    }

    private final int value;

    int getValue() {
        return value;
    }
}
