package me.patrykanuszczyk.yeelight;

import java.awt.*;

public class ColorFlow {
    public ColorFlow(int count, PostAction postAction, State... states) {
        this.count = count;
        this.postAction = postAction;
        this.states = states;
    }

    int count;
    PostAction postAction;
    State[] states;

    public String asApiExpression() {
        StringBuilder sb = new StringBuilder(count + "," + postAction);

        for(var state : states) {
            sb.append(",").append(state.asApiExpression());
        }

        return sb.toString();
    }

    public abstract static class State {
        public State(short brightness, int duration) {
            this.brightness = brightness;
            this.duration = duration;
        }

        int duration;
        public int getDuration() { return duration; }

        public abstract int getApiMode();

        public abstract int getValue();

        short brightness;
        public short getBrightness() { return brightness; }

        public String asApiExpression() {
            return getDuration() + "," + getApiMode() + "," + getValue() + "," + getBrightness();
        }

        public static class ColorState extends State {
            public ColorState(Color color, short brightness, int duration) {
                super(brightness, duration);
                this.color = color;
            }

            Color color;
            public Color getColor() {
                return color;
            }

            @Override
            public int getApiMode() {
                return 1;
            }

            @Override
            public int getValue() {
                return getColor().getRGB() & 0xFFFFFF;
            }
        }

        public static class ColorTemperatureState extends State {
            public ColorTemperatureState(short temperature, short brightness, int duration) {
                super(brightness, duration);
                this.temperature = temperature;
            }

            short temperature;
            public short getColorTemperature() {
                return temperature;
            }

            @Override
            public int getApiMode() {
                return 2;
            }

            @Override
            public int getValue() {
                return getColorTemperature();
            }
        }

        public static class WaitState extends State {
            public WaitState(short brightness, int duration) {
                super(brightness, duration);
            }

            @Override
            public int getApiMode() {
                return 7;
            }

            @Override
            public int getValue() {
                return 0;
            }
        }
    }

    public enum PostAction {
        RECOVER(0),
        STAY(1),
        TURN_OFF(2);

        PostAction(int apiValue) {
            this.apiValue = apiValue;
        }

        int apiValue;

        public int getApiValue() {
            return apiValue;
        }
    }
}
