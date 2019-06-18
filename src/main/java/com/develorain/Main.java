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
    static Double percentage = 1 - 0.000750;


    public static void main(String[] args) throws IOException {
        // Set up binance client
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("My2zlMkv4yorboQMABkUSqcNosJEqVZNi6JzPEvQovzbiVusGrf0ZkLF9rHkQAe7", "nUecuN1O33QAYXLdY76s12BME3fLafphBhj0kUl67Cs3seYxp8xzJ8JqVD7mYwJr");
        BinanceApiRestClient client = factory.newRestClient();

        // Initialize graph
        SimpleDirectedWeightedGraph<String, CustomEdge> graph = new SimpleDirectedWeightedGraph<>(CustomEdge.class);
        createGraphNodes(client, graph);
        System.out.println("Created graph nodes");
        createGraphEdgesAPI(client, graph);
        createCyclesAndWriteToFile(graph);

        System.out.println("Done");
        System.exit(0);
    }

    private static void createCyclesAndWriteToFile(SimpleDirectedWeightedGraph<String, CustomEdge> graph) throws IOException {



        FileWriter fileWriter2 = new FileWriter("amounts.txt");


        Double balance = 100.0;

        ArrayList<Tuple> cycleMoneyTuples = new ArrayList<>();

        JohnsonSimpleCycles<String, CustomEdge> johnsonSimpleCycles = new JohnsonSimpleCycles<>(graph);
        for (List<String> cycle : johnsonSimpleCycles.findSimpleCycles()) {
            if (cycle.size() > 2 && cycle.size() < 5) {
                String originalCurrency = cycle.get(0);
                System.out.println(originalCurrency);

                int size = cycle.size();

                ArrayList<Double> conversions = new ArrayList<>();
                ArrayList<Double> amounts = new ArrayList<>();

                // Computes profit
                for (int i = 0; i < size; i++) {
                    conversions.add(graph.getEdge(cycle.get(i), cycle.get((i+1) % size)).getPrice());
                    amounts.add(graph.getEdge(cycle.get(i), cycle.get((i+1) % size)).getAmount());
                }

                for (int i = 0; i < size; i++) {
                    balance = balance * conversions.get(i) * percentage;
                }

                for (int i = 1; i < size; i++) {
                    for (int j = 1; j <= i; j++) {
                        amounts.set(j, amounts.get(j) * conversions.get(i));
                    }
                }

                Double minimumAmountToTrade = Double.MAX_VALUE;

                for (Double num : amounts) {
                    minimumAmountToTrade = Math.min(minimumAmountToTrade, num);
                }

                fileWriter2.write(cycle + ", " + minimumAmountToTrade + "\n");


                // Add to array list if valid price
                if (balance != Double.POSITIVE_INFINITY && balance != Double.NEGATIVE_INFINITY && balance != 0.0 && !Double.isNaN(balance)) {
                    cycleMoneyTuples.add(new Tuple(cycle, balance));
                }

                balance = 100.0;
            }
        }

        fileWriter2.close();

        FileWriter fileWriter = new FileWriter("cycles.txt");

        // Sort tuples by price
        Object[] array = cycleMoneyTuples.toArray();
        Arrays.sort(array);

        for (Object a : array) {
            Tuple woo = (Tuple) a;
            fileWriter.write(woo.toString() + "\n");
        }

        fileWriter.close();
    }

    private static void createGraphNodes(BinanceApiRestClient client, SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        // Make nodes with each currency
        for (Asset asset : client.getAllAssets()) {
            // ONLY TAKES NODES WITH 3 LETTERS
            String assetCode = asset.getAssetCode();

            if (assetCode.length() == 3) {
                graph.addVertex(assetCode);
                //System.out.println("Added node: " + assetCode);
            }
        }
    }

    private static void createGraphEdgesAPI(BinanceApiRestClient client, SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        for (BookTicker bookTicker : client.getBookTickers()) {
            String symbol = bookTicker.getSymbol();

            if (symbol.length() != 6) {
                continue;
            }

            // Get asset codes for base and quote
            // REPLACE THIS WITH REGEX
            System.out.println(symbol);
            String baseAssetCode = symbol.substring(0, 3);
            String quoteAssetCode = symbol.substring(3, 6);

            if (!graph.containsVertex(baseAssetCode)) {
                System.out.println("Node does not exist: " + baseAssetCode);
                continue;
            }

            if (!graph.containsVertex(quoteAssetCode)) {
                System.out.println("Node does not exist: " + quoteAssetCode);
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

    private static void createGraphEdgesOffline(SimpleDirectedWeightedGraph<String, CustomEdge> graph) throws IOException {
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
}

// File does not contain:
// 1. bad currencies (BCC, RTX, etc)
// 2. anything longer than 3 letters


// Uses API to make graph
/*

FileWriter fileWriter = new FileWriter("data.txt");

// Make all connections between currencies
for (BookTicker bookTicker : client.getBookTickers()) {
    String symbol = bookTicker.getSymbol();

    // Get asset codes for base and quote
    // REPLACE THIS WITH REGEX
    //String baseAssetCode = client.getExchangeInfo().getSymbolInfo(symbol).getBaseAsset();
    //String quoteAssetCode = client.getExchangeInfo().getSymbolInfo(symbol).getQuoteAsset();

    bookTicker.getAskPrice();

    System.out.println(bookTicker.getBidPrice());
    if (!graph.containsVertex(baseAssetCode)) {
        System.out.println("Node does not exist: " + baseAssetCode);
        continue;
    }

    if (!graph.containsVertex(quoteAssetCode)) {
        System.out.println("Node does not exist: " + quoteAssetCode);
        continue;
    }

    // Connect these currencies with their corresponding weights
    //System.out.println(symbol);

    if (symbol.length() == 6) {
        fileWriter.write(symbol);

        try {
            CustomEdge baseToQuoteEdge = graph.addEdge(baseAssetCode, quoteAssetCode);
            graph.setEdgeWeight(baseToQuoteEdge, Double.parseDouble(bookTicker.getAskPrice()));

            CustomEdge quoteToBaseEdge = graph.addEdge(quoteAssetCode, baseAssetCode);
            graph.setEdgeWeight(quoteToBaseEdge, Double.parseDouble(bookTicker.getBidPrice()));

            fileWriter.write(", " + bookTicker.getAskPrice() + ", " + bookTicker.getBidPrice() + "\n");
        } catch (NullPointerException e) {
            System.out.println("Problematic: " + symbol);
        }
    }

    //System.out.println(bookTicker.getAskPrice() + ", " + bookTicker.getAskQty());
    //System.out.println(bookTicker.getBidPrice() + ", " + bookTicker.getBidQty());
}

fileWriter.close();

*/



/*
// Write all short cycles to file
JohnsonSimpleCycles<String, CustomEdge> johnsonSimpleCycles = new JohnsonSimpleCycles<>(graph);
FileWriter fileWriter = new FileWriter("cycles.txt");
for (List<String> cycle : johnsonSimpleCycles.findSimpleCycles()) {
    if (cycle.size() > 2 && cycle.size() < 5) {
        fileWriter.write(cycle.toString() + "\n");
    }
}
fileWriter.close();
*/



// DISPLAY GRAPH
/*
//System.out.println(graph.toString());

// Find path between 2 nodes
//System.out.println(DijkstraShortestPath.findPathBetween(graph, "BTC", "BTC"));

//JohnsonSimpleCycles<String, CustomEdge> johnsonSimpleCycles = new JohnsonSimpleCycles<>(graph);

//System.out.println(johnsonSimpleCycles.findSimpleCycles());
*/



/* Sample cash money machine
cashMoney = cashMoney *
        graph.getEdgeWeight(graph.getEdge("BNB", "SKY")) *
        graph.getEdgeWeight(graph.getEdge("SKY", "BTC")) *
        graph.getEdgeWeight(graph.getEdge("BTC", "NAS")) *
        graph.getEdgeWeight(graph.getEdge("NAS", "BNB"));
*/