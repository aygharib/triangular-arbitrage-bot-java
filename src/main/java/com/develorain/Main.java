package com.develorain;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.Asset;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        // Set up binance client
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("My2zlMkv4yorboQMABkUSqcNosJEqVZNi6JzPEvQovzbiVusGrf0ZkLF9rHkQAe7", "nUecuN1O33QAYXLdY76s12BME3fLafphBhj0kUl67Cs3seYxp8xzJ8JqVD7mYwJr");
        BinanceApiRestClient client = factory.newRestClient();

        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        createGraphNodes(client, graph);
        System.out.println("Created graph nodes");

        createEdgesForGraph(graph);


        Double cashMoney = 1000.0;

        JohnsonSimpleCycles<String, DefaultWeightedEdge> johnsonSimpleCycles = new JohnsonSimpleCycles<>(graph);
        for (List<String> cycle : johnsonSimpleCycles.findSimpleCycles()) {
            if (cycle.size() > 2 && cycle.size() < 5) {
                int size = cycle.size();
                System.out.println(cycle);

                // Computes profit
                for (int i = 0; i < size; i++) {
                    cashMoney *= graph.getEdgeWeight(graph.getEdge(cycle.get(i), cycle.get((i+1) % size)));
                    //System.out.println(i + ":" + graph.getEdgeWeight(graph.getEdge(cycle.get(i), cycle.get((i+1) % size))));
                }

                if (cashMoney != Double.POSITIVE_INFINITY && cashMoney != Double.NEGATIVE_INFINITY && cashMoney != 0.0 && !cashMoney.isNaN()) {
                    System.out.println("Total: " + cashMoney);
                }

                cashMoney = 1000.0;
            }
        }

        System.out.println("Done");
        System.exit(0);
    }

    private static void createGraphNodes(BinanceApiRestClient client, SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph) {
        // Make nodes with each currency
        for (Asset asset : client.getAllAssets()) {
            // ONLY TAKES NODES WITH 3 LETTERS
            String assetCode = asset.getAssetCode();

            if (assetCode.length() == 3) {
                graph.addVertex(assetCode);
                //System.out.println("Added node: " + assetCode);
            }
        }
        graph.removeVertex("TUSD");
    }

    private static void createEdgesForGraph(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph) throws IOException {
        // Uses file to make all edges between nodes with weights
        BufferedReader bufferedReader = new BufferedReader(new FileReader("data.txt"));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] elements = line.split(", ");
            String baseAssetCode = elements[0].substring(0, 3);
            String quoteAssetCode = elements[0].substring(3, 6);

            DefaultWeightedEdge baseToQuoteEdge = graph.addEdge(baseAssetCode, quoteAssetCode);
            graph.setEdgeWeight(baseToQuoteEdge, Double.parseDouble(elements[1]));

            DefaultWeightedEdge quoteToBaseEdge = graph.addEdge(quoteAssetCode, baseAssetCode);
            graph.setEdgeWeight(quoteToBaseEdge, 1.0 / Double.parseDouble(elements[2]));
        }
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
            DefaultWeightedEdge baseToQuoteEdge = graph.addEdge(baseAssetCode, quoteAssetCode);
            graph.setEdgeWeight(baseToQuoteEdge, Double.parseDouble(bookTicker.getAskPrice()));

            DefaultWeightedEdge quoteToBaseEdge = graph.addEdge(quoteAssetCode, baseAssetCode);
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
JohnsonSimpleCycles<String, DefaultWeightedEdge> johnsonSimpleCycles = new JohnsonSimpleCycles<>(graph);
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

//JohnsonSimpleCycles<String, DefaultWeightedEdge> johnsonSimpleCycles = new JohnsonSimpleCycles<>(graph);

//System.out.println(johnsonSimpleCycles.findSimpleCycles());
*/



/* Sample cash money machine
cashMoney = cashMoney *
        graph.getEdgeWeight(graph.getEdge("BNB", "SKY")) *
        graph.getEdgeWeight(graph.getEdge("SKY", "BTC")) *
        graph.getEdgeWeight(graph.getEdge("BTC", "NAS")) *
        graph.getEdgeWeight(graph.getEdge("NAS", "BNB"));
*/