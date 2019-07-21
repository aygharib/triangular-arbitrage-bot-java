package com.develorain;

import java.util.List;

public class Cycle implements Comparable {
    public final List<String> cycleString; // List of string currencies
    public double worstCaseMultiplier;              // Worst-case worstCaseMultiplier for the cycle
    public int size;

    public CustomEdge[] edges; // Stores all the edges that are contained in the cycle (to have access to ask and bid prices)

    public Cycle(List<String> cycleString) {
        this.cycleString = cycleString;
        this.worstCaseMultiplier = 1.0;
        this.size = cycleString.size();

        this.edges = new CustomEdge[size];
    }

    @Override
    public String toString() {
        return "<~~ " + cycleString + " with worstCaseMultiplier " + Tools.formatPrice(worstCaseMultiplier) + " ~~>";
    }

    @Override
    public int compareTo(Object o) {
        Cycle otherCycle = (Cycle) o;

        if (worstCaseMultiplier > otherCycle.worstCaseMultiplier) {
            return 1;
        }

        if (worstCaseMultiplier < otherCycle.worstCaseMultiplier) {
            return -1;
        }

        return 0;
    }
}