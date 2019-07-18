package com.develorain;

public class Tools {
    public static String formatPrice(double num) {
        // Cap to 8 decimal places with no scientific notation
        return String.format("%.8f", num);
    }
}
