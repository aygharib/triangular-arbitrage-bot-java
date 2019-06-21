package com.develorain;

import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GraphProcessing {
    public static Object[] getSortedCyclesByMultiplier(SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        List<Cycle> cycleObjects3to4Size = new ArrayList<>();

        List<Cycle> cycleObjects = getCycleObjects(graph);

        for (Cycle cycle: cycleObjects) {
            if (cycle.size >= 3 && cycle.size <= 6) {
                computeCycleAttributes(graph, cycle);
                computeActualTradeQuantities(cycle);

                if (isDesirableCycle(cycle)) {
                    cycleObjects3to4Size.add(cycle);
                }
            }
        }

        return sortCyclesByMultiplier(cycleObjects3to4Size);
    }

    private static List<Cycle> getCycleObjects(SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        JohnsonSimpleCycles<String, CustomEdge> cycleAlgorithm = new JohnsonSimpleCycles<>(graph);
        List<List<String>> cycleStrings = cycleAlgorithm.findSimpleCycles();

        List<Cycle> cycleObjects = new ArrayList<>();

        for (List<String> cycleString : cycleStrings) {
            cycleObjects.add(new Cycle(cycleString));
        }

        return cycleObjects;
    }

    private static boolean isDesirableCycle(Cycle cycle) {
        return cycle.multiplier != Double.POSITIVE_INFINITY && cycle.multiplier != Double.NEGATIVE_INFINITY && cycle.multiplier >= 1.0 && !Double.isNaN(cycle.multiplier);
    }

    private static void computeCycleAttributes(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle) {
        computeConversionPricesAndQuantities(graph, cycle);
        convertQuantitiesToStartingCurrency(cycle);
        computeCycleMultiplier(cycle);
    }

    private static void computeActualTradeQuantities(Cycle cycle) {
        double maximumAmountToTradeInStartingCurrency = Double.MAX_VALUE;

        for (Double amount : cycle.tradeQuantitiesInStartCurrency) {
            maximumAmountToTradeInStartingCurrency = Math.min(maximumAmountToTradeInStartingCurrency, amount);
        }

        cycle.actualTradeQuantities[0] = maximumAmountToTradeInStartingCurrency;

        for (int i = 1; i < cycle.size; i++) {
            cycle.actualTradeQuantities[i] = cycle.actualTradeQuantities[i-1] * cycle.tradePrices[i-1] * Main.TRANSACTION_FEE_RATIO;
        }
    }

    private static Object[] sortCyclesByMultiplier(List<Cycle> cycleObjects3to4Size) {
        Object[] cycleObjectsArray = cycleObjects3to4Size.toArray();
        Arrays.sort(cycleObjectsArray, Collections.reverseOrder());
        return cycleObjectsArray;
    }

    private static void computeCycleMultiplier(Cycle cycle) {
        for (int i = 0; i < cycle.size; i++) {
            cycle.multiplier = cycle.multiplier * cycle.tradePrices[i] * Main.TRANSACTION_FEE_RATIO;
        }
    }

    private static void computeConversionPricesAndQuantities(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle) {
        for (int i = 0; i < cycle.size; i++) {
            String sourceNode = cycle.cycleString.get(i);
            String targetNode = cycle.cycleString.get((i + 1) % cycle.size);

            cycle.tradePrices[i] = graph.getEdge(sourceNode, targetNode).getPrice();
            cycle.tradeQuantitiesInStartCurrency[i] = graph.getEdge(sourceNode, targetNode).getAmount();
        }
    }

    private static void convertQuantitiesToStartingCurrency(Cycle cycle) {
        for (int i = 1; i < cycle.size; i++) {
            for (int j = 1; j <= i; j++) {
                cycle.tradeQuantitiesInStartCurrency[j] = cycle.tradeQuantitiesInStartCurrency[j] * cycle.tradePrices[i];
            }
        }
    }
}
