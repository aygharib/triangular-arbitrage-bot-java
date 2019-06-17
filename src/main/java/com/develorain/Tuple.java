package com.develorain;

import java.util.List;

public class Tuple implements Comparable {
    public final List<String> x;
    public final Double y;
    public Tuple(List<String> x, Double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }

    @Override
    public int compareTo(Object o) {
        Tuple myTuple = (Tuple) o;

        if (y > myTuple.y) {
            return 1;
        }

        if (y < myTuple.y) {
            return -1;
        }

        return 0;
    }
}