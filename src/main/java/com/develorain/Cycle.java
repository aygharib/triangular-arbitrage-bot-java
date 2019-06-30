package com.develorain;

import java.util.List;

public class Cycle implements Comparable {
    public final List<String> cycleString;
    public double multiplier;
    public double[] tradePrices;

    // this is basically tradePrices, but does 1.0 / tradePrices for buying original currencies, so that we can do the math easier, but still have the original tradePrices in memory
    // basically, when using the top half, you gotta 1.0/over top half as you're going from other currency back to main currency
    public double[] tradeRates;

    public double[] tradeQuantities;
    public double[] tradeQuantitiesInStartCurrency;
    public double[] actualTradeQuantitiesForEachCurrency;
    public int size;

    Cycle(List<String> cycleString) {
        this.cycleString = cycleString;
        this.multiplier = 1.0;
        this.size = cycleString.size();

        this.tradePrices = new double[cycleString.size()];
        this.tradeRates = new double[cycleString.size()];
        this.tradeQuantities = new double[cycleString.size()];
        this.tradeQuantitiesInStartCurrency = new double[cycleString.size()];
        this.actualTradeQuantitiesForEachCurrency = new double[cycleString.size()];
    }

    @Override
    public String toString() {
        return cycleString + ", " + multiplier;
    }

    @Override
    public int compareTo(Object o) {
        Cycle otherCycle = (Cycle) o;

        if (multiplier > otherCycle.multiplier) {
            return 1;
        }

        if (multiplier < otherCycle.multiplier) {
            return -1;
        }

        return 0;
    }
}