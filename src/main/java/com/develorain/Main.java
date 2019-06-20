package com.develorain;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.market.BookTicker;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final Double TRANSACTION_FEE_RATIO = 1 - 0.000750;

    private Main()  {
        // Set up Binance client
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("My2zlMkv4yorboQMABkUSqcNosJEqVZNi6JzPEvQovzbiVusGrf0ZkLF9rHkQAe7", "nUecuN1O33QAYXLdY76s12BME3fLafphBhj0kUl67Cs3seYxp8xzJ8JqVD7mYwJr");
        BinanceApiRestClient client = factory.newRestClient();

        SimpleDirectedWeightedGraph<String, CustomEdge> graph = createGraph(client);
        processCyclesTemp(graph);
    }

    private SimpleDirectedWeightedGraph<String, CustomEdge> createGraph(BinanceApiRestClient client) {
        SimpleDirectedWeightedGraph<String, CustomEdge> graph = new SimpleDirectedWeightedGraph<>(CustomEdge.class);
        createGraphNodes(client, graph);
        createGraphEdgesAPI(client, graph);

        return graph;
    }

    private List<Cycle> getCycleObjects(SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        JohnsonSimpleCycles<String, CustomEdge> cycleAlgorithm = new JohnsonSimpleCycles<>(graph);
        List<List<String>> cycleStrings = cycleAlgorithm.findSimpleCycles();

        List<Cycle> cycleObjects = new ArrayList<>();

        for (List<String> cycleString : cycleStrings) {
            cycleObjects.add(new Cycle(cycleString));
        }

        return cycleObjects;
    }

    private void processCyclesTemp(SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
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

        Object[] sortedCyclesByMultiplier = sortCyclesByMultiplier(cycleObjects3to4Size);

        writeCyclesToFile(sortedCyclesByMultiplier);
    }

    private void writeCyclesToFile(Object[] sortedCyclesByMultiplier) {
        try {
            FileWriter fileWriter = new FileWriter("cycles.txt");

            for (Object a : sortedCyclesByMultiplier) {
                Cycle cycle = (Cycle) a;
                fileWriter.write(cycle.toString() + "\n");
            }

            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object[] sortCyclesByMultiplier(List<Cycle> cycleObjects3to4Size) {
        Object[] cycleObjectsArray = cycleObjects3to4Size.toArray();
        Arrays.sort(cycleObjectsArray);
        return cycleObjectsArray;
    }

    private boolean isDesirableCycle(Cycle cycle) {
        return cycle.multiplier != Double.POSITIVE_INFINITY && cycle.multiplier != Double.NEGATIVE_INFINITY && cycle.multiplier >= 1.0 && !Double.isNaN(cycle.multiplier);
    }

    private void computeActualTradeQuantities(Cycle cycle) {
        double maximumAmountToTradeInStartingCurrency = Double.MAX_VALUE;

        for (Double amount : cycle.tradeQuantitiesInStartCurrency) {
            maximumAmountToTradeInStartingCurrency = Math.min(maximumAmountToTradeInStartingCurrency, amount);
        }

        cycle.actualTradeQuantities[0] = maximumAmountToTradeInStartingCurrency;

        for (int i = 1; i < cycle.size; i++) {
            cycle.actualTradeQuantities[i] = cycle.actualTradeQuantities[i-1] * cycle.tradePrices[i-1] * TRANSACTION_FEE_RATIO;
        }
    }

    private void computeCycleMultiplier(Cycle cycle) {
        for (int i = 0; i < cycle.size; i++) {
            cycle.multiplier = cycle.multiplier * cycle.tradePrices[i] * TRANSACTION_FEE_RATIO;
        }
    }

    private void computeCycleAttributes(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle) {
        computeConversionPricesAndQuantities(graph, cycle);
        convertQuantitiesToStartingCurrency(cycle);
        computeCycleMultiplier(cycle);
    }

    private void computeConversionPricesAndQuantities(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle) {
        for (int i = 0; i < cycle.size; i++) {
            String sourceNode = cycle.cycleString.get(i);
            String targetNode = cycle.cycleString.get((i + 1) % cycle.size);

            cycle.tradePrices[i] = graph.getEdge(sourceNode, targetNode).getPrice();
            cycle.tradeQuantitiesInStartCurrency[i] = graph.getEdge(sourceNode, targetNode).getAmount();
        }
    }

    private void convertQuantitiesToStartingCurrency(Cycle cycle) {
        for (int i = 1; i < cycle.size; i++) {
            for (int j = 1; j <= i; j++) {
                cycle.tradeQuantitiesInStartCurrency[j] = cycle.tradeQuantitiesInStartCurrency[j] * cycle.tradePrices[i];
            }
        }
    }

    private void createGraphNodes(BinanceApiRestClient client, SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        // Make nodes with each currency
        for (Asset asset : client.getAllAssets()) {
            String assetCode = asset.getAssetCode();

            if (assetCode.length() == 3) {
                graph.addVertex(assetCode);
            }
        }
    }

    private void createGraphEdgesAPI(BinanceApiRestClient client, SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        for (BookTicker bookTicker : client.getBookTickers()) {
            String symbol = bookTicker.getSymbol();

            if (symbol.length() != 6) {
                continue;
            }

            // Get asset codes for base and quote
            // REPLACE THIS WITH REGEX
            String baseAssetCode = symbol.substring(0, 3);
            String quoteAssetCode = symbol.substring(3, 6);

            if (!graph.containsVertex(baseAssetCode)) {
                System.out.println("Warning: Attempting to create edge with non-existent node: " + quoteAssetCode);
                continue;
            }

            if (!graph.containsVertex(quoteAssetCode)) {
                System.out.println("Warning: Attempting to create edge with non-existent node: " + quoteAssetCode);
                continue;
            }

            // Connect these currencies with their corresponding weights
            try {
                CustomEdge baseToQuoteEdge = new CustomEdge(Double.parseDouble(bookTicker.getAskPrice()), Double.parseDouble(bookTicker.getAskQty()));
                graph.addEdge(baseAssetCode, quoteAssetCode, baseToQuoteEdge);
                //graph.setEdgeWeight(baseToQuoteEdge, Double.parseDouble(bookTicker.getAskPrice()));

                CustomEdge quoteToBaseEdge = new CustomEdge(1.0/Double.parseDouble(bookTicker.getBidPrice()), Double.parseDouble(bookTicker.getBidQty()));
                graph.addEdge(quoteAssetCode, baseAssetCode, quoteToBaseEdge);
                //graph.setEdgeWeight(quoteToBaseEdge, 1.0/Double.parseDouble(bookTicker.getBidPrice()));
            } catch (NullPointerException e) {
                System.out.println("Problematic: " + symbol);
            }
        }
    }

    private void createGraphEdgesOffline(SimpleDirectedWeightedGraph<String, CustomEdge> graph) throws IOException {
        // Uses file to make all edges between nodes with weights
        BufferedReader bufferedReader = new BufferedReader(new FileReader("data.txt"));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] elements = line.split(", ");
            String baseAssetCode = elements[0].substring(0, 3);
            String quoteAssetCode = elements[0].substring(3, 6);

            CustomEdge baseToQuoteEdge = graph.addEdge(baseAssetCode, quoteAssetCode);
            graph.setEdgeWeight(baseToQuoteEdge, Double.parseDouble(elements[1]));

            CustomEdge quoteToBaseEdge = graph.addEdge(quoteAssetCode, baseAssetCode);
            graph.setEdgeWeight(quoteToBaseEdge, 1.0 / Double.parseDouble(elements[2]));
        }

        bufferedReader.close();
    }

    public static void main(String[] args) {
        new Main();

        System.out.println("Done");
        System.exit(0);
    }
}