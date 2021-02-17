package me.patrykanuszczyk.yeelight;

import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

public class Utils {
    private Utils() {}

    @SuppressWarnings("SpellCheckingInspection")
//    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    @Nonnull
    @Contract(pure = true)
    public static String bytesToHex(byte ...bytes) {
        if(bytes == null) return "null";
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = Character.forDigit(v >>> 4, 16);
            hexChars[j * 2 + 1] = Character.forDigit(v & 0x0F, 16);
//            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
//            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Nonnull
    @Contract(pure = true)
    public static byte[] hexToBytes(@Nonnull String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for(int i = 0; i < len; i += 2) {
            var value = (Character.digit(hex.charAt(i), 16) << 4)
                | Character.digit(hex.charAt(i + 1), 16);
            data[i / 2] = (byte) value;
        }
        return data;
    }

    @Contract(pure = true)
    public static long bytesToLongBE(@Nonnull byte[] bytes) {
        long value = 0;

        for(byte aByte : bytes) {
            //System.out.println("iteration");
            value = value << 8;
            value |= aByte & 0xffL;
        }

        return value;
    }
    private static final String[] emptyStringArray = new String[0];

    @Contract(pure = true)
    public static String[] getEmptyStringArray() {
        return emptyStringArray;
    }

    @Contract(pure = true)
    public static Object[] getEmptyObjectArray() {
        return emptyStringArray;
    }
}
