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
            if (cycle.size == 3) {
                initializeCycleAttributes(graph, cycle);

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
        return cycle.worstCaseMultiplier != Double.POSITIVE_INFINITY && cycle.worstCaseMultiplier != Double.NEGATIVE_INFINITY && cycle.worstCaseMultiplier >= 0.99 && !Double.isNaN(cycle.worstCaseMultiplier)
                && cycle.averageCaseMultiplier != Double.POSITIVE_INFINITY && cycle.averageCaseMultiplier != Double.NEGATIVE_INFINITY && !Double.isNaN(cycle.averageCaseMultiplier);
    }

    private static void initializeCycleAttributes(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle) {
        double[] tradeQuantitiesFromAPI = new double[cycle.size];
        double[] tradeQuantitiesInStartCurrency = tradeQuantitiesFromAPI;

        computeConversionPricesAndQuantities(graph, cycle, tradeQuantitiesFromAPI);
        convertQuantitiesToStartingCurrency(cycle, tradeQuantitiesInStartCurrency);
        computeCycleMultiplier(cycle);
        computeActualTradeQuantities(cycle, tradeQuantitiesInStartCurrency);
    }

    private static Cycle[] sortCyclesByMultiplier(List<Cycle> cycleObjects3to4Size) {
        Object[] objectsArray = cycleObjects3to4Size.toArray();
        Arrays.sort(objectsArray, Collections.reverseOrder());

        // Convert to Cycle[]
        return Arrays.copyOf(objectsArray, objectsArray.length, Cycle[].class);
    }

    private static void computeCycleMultiplier(Cycle cycle) {
        for (int i = 0; i < cycle.size; i++) {
            cycle.worstCaseMultiplier = cycle.worstCaseMultiplier * cycle.edges[i].worstCaseTradeRate * Main.TRANSACTION_FEE_RATIO;
            cycle.averageCaseMultiplier = cycle.averageCaseMultiplier * cycle.edges[i].averageCaseTradeRate * Main.TRANSACTION_FEE_RATIO;
        }
    }

    private static void computeConversionPricesAndQuantities(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle, double[] tradeQuantitiesFromAPI) {
        for (int i = 0; i < cycle.size; i++) {
            // These two nodes are just traversing through the cycle, pair by pair, until you get to the beginning
            String sourceNode = cycle.cycleString.get(i);
            String targetNode = cycle.cycleString.get((i + 1) % cycle.size);

            CustomEdge traversingCycleEdge = graph.getEdge(sourceNode, targetNode);

            cycle.edges[i] = traversingCycleEdge;

            tradeQuantitiesFromAPI[i] = traversingCycleEdge.sameDirectionAsSymbol ? traversingCycleEdge.symbol.bidQuantity : traversingCycleEdge.symbol.askQuantity;

            if (BinanceAPICaller.amISellingBaseCurrency(traversingCycleEdge)) {
                // I'm selling base currency
                cycle.edges[i].worstCaseTradeRate = cycle.edges[i].worstCaseTradePrice();
                cycle.edges[i].averageCaseTradeRate = cycle.edges[i].averageCaseTradePrice();
            } else {
                // I'm buying base currency
                cycle.edges[i].worstCaseTradeRate = 1.0 / cycle.edges[i].worstCaseTradePrice();
                cycle.edges[i].averageCaseTradeRate = 1.0 / cycle.edges[i].averageCaseTradePrice();
            }
        }
    }

    private static void convertQuantitiesToStartingCurrency(Cycle cycle, double[] tradeQuantitiesInStartCurrency) {
        // Move each currency to the right until we reach the start currency
        for (int i = 1; i < cycle.size; i++) {
            for (int j = 1; j <= i; j++) {
                tradeQuantitiesInStartCurrency[j] = tradeQuantitiesInStartCurrency[j] * cycle.edges[i].worstCaseTradeRate;
            }
        }

        // Now the values in the array are correct
    }

    private static void computeActualTradeQuantities(Cycle cycle, double[] tradeQuantitiesInStartCurrency) {
        double maximumAmountToTradeInStartingCurrency = Double.MAX_VALUE;

        for (Double amount : tradeQuantitiesInStartCurrency) {
            maximumAmountToTradeInStartingCurrency = Math.min(maximumAmountToTradeInStartingCurrency, amount);
        }

        cycle.edges[0].tradeQuantity = maximumAmountToTradeInStartingCurrency;

        for (int i = 1; i < cycle.size; i++) {
            cycle.edges[i].tradeQuantity = cycle.edges[i-1].tradeQuantity * cycle.edges[i-1].worstCaseTradeRate * Main.TRANSACTION_FEE_RATIO;
        }
    }
}
