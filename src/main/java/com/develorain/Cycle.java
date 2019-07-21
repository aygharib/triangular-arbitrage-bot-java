package com.develorain;

import java.util.List;

public class Cycle implements Comparable {
    public final List<String> cycleString; // List of string currencies
    public double multiplier;              // Worst-case multiplier for the cycle


    public CustomEdge[] edges; // Stores all the edges that are contained in the cycle (to have access to ask and bid prices)

    public double[] tradeRates;

    public int size;

    public Cycle(List<String> cycleString) {
        this.cycleString = cycleString;
        this.multiplier = 1.0;
        this.size = cycleString.size();

        this.edges = new CustomEdge[size];
        this.tradeRates = new double[cycleString.size()];
    }

    @Override
    public String toString() {
        return "<~~ " + cycleString + " with multiplier " + Tools.formatPrice(multiplier) + " ~~>";
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