package com.develorain;

import java.util.List;

public class Cycle implements Comparable {
    public final List<String> cycleString; // List of string currencies
    public double worstCaseMultiplier;
    public double averageCaseMultiplier;
    public int size;

    public CustomEdge[] edges; // Stores all the edges that are contained in the cycle (to have access to ask and bid prices)

    public Cycle(List<String> cycleString) {
        this.cycleString = cycleString;
        this.worstCaseMultiplier = 1.0;
        this.averageCaseMultiplier = 1.0;

        this.size = cycleString.size();

        this.edges = new CustomEdge[size];
    }

    @Override
    public String toString() {
        return cycleString + " WCM: " + Tools.formatPrice(worstCaseMultiplier) + ", ACM: " + Tools.formatPrice(averageCaseMultiplier) + " ~~>";
        // Worst Case Multiplier, Average case Multiplier
    }

    @Override
    public int compareTo(Object o) {
        Cycle otherCycle = (Cycle) o;

        if (averageCaseMultiplier > otherCycle.averageCaseMultiplier) {
            return 1;
        }

        if (averageCaseMultiplier < otherCycle.averageCaseMultiplier) {
            return -1;
        }

        return 0;
    }
}