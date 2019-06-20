package com.develorain;

import java.util.List;

public class Cycle implements Comparable {
    public final List<String> cycleString;
    public double multiplier;
    public double[] conversions;
    public double[] amounts;
    public int size;

    Cycle(List<String> cycleString) {
        this.cycleString = cycleString;
        this.multiplier = 1.0;
        this.size = cycleString.size();

        this.conversions = new double[cycleString.size()];
        this.amounts = new double[cycleString.size()];
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