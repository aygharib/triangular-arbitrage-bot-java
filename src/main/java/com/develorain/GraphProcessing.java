package com.develorain;

import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GraphProcessing {
    public static Cycle[] getSortedCyclesByMultiplier(SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        List<Cycle> cycleObjects3to4Size = new ArrayList<>();

        List<Cycle> cycleObjects = getCycleObjects(graph);

        for (Cycle cycle: cycleObjects) {
            if (cycle.size >= 3 && cycle.size <= 4) {
                initializeCycleAttributes(graph, cycle);
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

    private static void initializeCycleAttributes(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle) {
        computeConversionPricesAndQuantities(graph, cycle);
        convertQuantitiesToStartingCurrency(cycle);
        computeCycleMultiplier(cycle);
    }

    private static void computeActualTradeQuantities(Cycle cycle) {
        double maximumAmountToTradeInStartingCurrency = Double.MAX_VALUE;

        for (Double amount : cycle.tradeQuantitiesInStartCurrency) {
            maximumAmountToTradeInStartingCurrency = Math.min(maximumAmountToTradeInStartingCurrency, amount);
        }

        // Force it to be 0.24 BNB
        maximumAmountToTradeInStartingCurrency = Math.min(0.8, maximumAmountToTradeInStartingCurrency);

        cycle.actualTradeQuantitiesForEachCurrency[0] = maximumAmountToTradeInStartingCurrency;

        for (int i = 1; i < cycle.size; i++) {
            cycle.actualTradeQuantitiesForEachCurrency[i] = cycle.actualTradeQuantitiesForEachCurrency[i-1] * cycle.tradePrices[i-1] * Main.TRANSACTION_FEE_RATIO;
        }
    }

    private static Cycle[] sortCyclesByMultiplier(List<Cycle> cycleObjects3to4Size) {
        Object[] objectsArray = cycleObjects3to4Size.toArray();
        Arrays.sort(objectsArray, Collections.reverseOrder());

        // Convert to Cycle[]
        return Arrays.copyOf(objectsArray, objectsArray.length, Cycle[].class);
    }

    private static void computeCycleMultiplier(Cycle cycle) {
        for (int i = 0; i < cycle.size; i++) {
            cycle.multiplier = cycle.multiplier * cycle.tradePrices[i] * Main.TRANSACTION_FEE_RATIO;
        }
    }

    private static void computeConversionPricesAndQuantities(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle) {
        System.out.println("Start cycle at: " + cycle.cycleString.get(0));
        for (int i = 0; i < cycle.size; i++) {
            String sourceNode = cycle.cycleString.get(i);
            String targetNode = cycle.cycleString.get((i + 1) % cycle.size);

            CustomEdge edge = graph.getEdge(sourceNode, targetNode);

            if (BinanceAPICaller.isABuyOrder(edge)) {
                cycle.tradePrices[i] = 1.0 / graph.getEdge(targetNode, sourceNode).getPrice();
                cycle.tradeQuantities[i] = graph.getEdge(targetNode, sourceNode).getAmount();

                System.out.println("Buy order: " + edge.symbol + " Price: " + cycle.tradePrices[i]);
            } else {
                cycle.tradePrices[i] = graph.getEdge(sourceNode, targetNode).getPrice();
                cycle.tradeQuantities[i] = graph.getEdge(sourceNode, targetNode).getAmount();

                System.out.println("Sell order: " + edge.symbol + " Price: " + cycle.tradePrices[i]);
            }
        }

        System.out.println("SEPARATOR@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }

    private static void convertQuantitiesToStartingCurrency(Cycle cycle) {
        // Move each currency to the right until we reach the start currency

        // Copy array
        cycle.tradeQuantitiesInStartCurrency = cycle.tradeQuantities;

        for (int i = 1; i < cycle.size; i++) {
            for (int j = 1; j <= i; j++) {
                cycle.tradeQuantitiesInStartCurrency[j] = cycle.tradeQuantitiesInStartCurrency[j] * cycle.tradePrices[i];
            }
        }
    }
}
