package com.develorain;

public class Tools {
    public static String formatPrice(double price) {
        // Cap to 8 decimal places with no scientific notation
        return String.format("%.8f", price);
    }

    public static String formatAmount(double amount) {
        return String.format("%.10f", amount);
    }
}
