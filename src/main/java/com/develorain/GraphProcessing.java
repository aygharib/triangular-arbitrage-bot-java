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

        List<Cycle> cycles = getCycles(graph);

        for (Cycle cycle: cycles) {
            if (cycle.size >= 3 && cycle.size <= 6) {
                initializeCycleAttributes(graph, cycle);
                computeActualTradeQuantities(cycle);

                if (isDesirableCycle(cycle)) {
                    cycleObjects3to4Size.add(cycle);
                }
            }
        }

        return sortCyclesByMultiplier(cycleObjects3to4Size);
    }

    private static List<Cycle> getCycles(SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        JohnsonSimpleCycles<String, CustomEdge> cycleAlgorithm = new JohnsonSimpleCycles<>(graph);
        List<List<String>> cycleStrings = cycleAlgorithm.findSimpleCycles();

        List<Cycle> cycleObjects = new ArrayList<>();

        for (List<String> cycleString : cycleStrings) {
            cycleObjects.add(new Cycle(cycleString));
        }

        return cycleObjects;
    }

    private static boolean isDesirableCycle(Cycle cycle) {
        return cycle.multiplier != Double.POSITIVE_INFINITY && cycle.multiplier != Double.NEGATIVE_INFINITY && cycle.multiplier >= 0.99 && !Double.isNaN(cycle.multiplier);
    }

    private static void initializeCycleAttributes(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle) {
        computeConversionPricesAndQuantities(graph, cycle);
        convertQuantitiesToStartingCurrency(cycle);
        computeCycleMultiplier(cycle, graph);
    }

    private static void computeActualTradeQuantities(Cycle cycle) {
        double maximumAmountToTradeInStartingCurrency = Double.MAX_VALUE;

        for (Double amount : cycle.tradeQuantitiesInStartCurrency) {
            maximumAmountToTradeInStartingCurrency = Math.min(maximumAmountToTradeInStartingCurrency, amount);
        }

        cycle.actualTradeQuantitiesForEachCurrency[0] = maximumAmountToTradeInStartingCurrency;

        for (int i = 1; i < cycle.size; i++) {
            cycle.actualTradeQuantitiesForEachCurrency[i] = cycle.actualTradeQuantitiesForEachCurrency[i-1] * cycle.tradeRates[i-1] * Main.TRANSACTION_FEE_RATIO;
        }
    }

    private static Cycle[] sortCyclesByMultiplier(List<Cycle> cycleObjects3to4Size) {
        Object[] objectsArray = cycleObjects3to4Size.toArray();
        Arrays.sort(objectsArray, Collections.reverseOrder());

        // Convert to Cycle[]
        return Arrays.copyOf(objectsArray, objectsArray.length, Cycle[].class);
    }

    private static void computeCycleMultiplier(Cycle cycle, SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        for (int i = 0; i < cycle.size; i++) {
            cycle.multiplier = cycle.multiplier * cycle.tradeRates[i] * Main.TRANSACTION_FEE_RATIO;
        }
    }

    private static void computeConversionPricesAndQuantities(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle) {
        for (int i = 0; i < cycle.size; i++) {
            // These two nodes are just traversing through the cycle, pair by pair, until you get to the beginning
            String sourceNode = cycle.cycleString.get(i);
            String targetNode = cycle.cycleString.get((i + 1) % cycle.size);

            CustomEdge sourceToTargetEdge = graph.getEdge(sourceNode, targetNode);

            cycle.tradePrices[i] = sourceToTargetEdge.sameDirectionAsSymbol ? sourceToTargetEdge.symbol.bidPrice : sourceToTargetEdge.symbol.askPrice;
            cycle.tradeQuantities[i] = sourceToTargetEdge.sameDirectionAsSymbol ? sourceToTargetEdge.symbol.bidQuantity : sourceToTargetEdge.symbol.askQuantity;

            if (BinanceAPICaller.isMeSellingBaseCurrencyOrder(sourceToTargetEdge)) {
                // IM SELLING THE BASE CURRENCY!!
                cycle.tradeRates[i] = cycle.tradePrices[i];
            } else {
                // IM BUYING THE BASE CURRENCY!!
                cycle.tradeRates[i] = 1.0 / cycle.tradePrices[i];
            }
        }
    }

    private static void convertQuantitiesToStartingCurrency(Cycle cycle) {
        // Move each currency to the right until we reach the start currency

        // Copy array, not values are not correct yet!!
        cycle.tradeQuantitiesInStartCurrency = cycle.tradeQuantities;

        for (int i = 1; i < cycle.size; i++) {
            for (int j = 1; j <= i; j++) {
                cycle.tradeQuantitiesInStartCurrency[j] = cycle.tradeQuantitiesInStartCurrency[j] * cycle.tradeRates[i];
            }
        }

        // Now the values in the array are correct
    }
}
