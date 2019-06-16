package com.develorain;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.domain.market.TickerStatistics;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.JohnsonShortestPaths;
import org.jgrapht.graph.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

public class Main {
    public static void main(String[] args) throws IOException {
        // Set up binance client
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("My2zlMkv4yorboQMABkUSqcNosJEqVZNi6JzPEvQovzbiVusGrf0ZkLF9rHkQAe7", "nUecuN1O33QAYXLdY76s12BME3fLafphBhj0kUl67Cs3seYxp8xzJ8JqVD7mYwJr");
        BinanceApiRestClient client = factory.newRestClient();

        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // Make nodes with each currency
        for (Asset asset : client.getAllAssets()) {
            // ONLY TAKES NODES WITH 3 LETTERS
            String assetCode = asset.getAssetCode();

            if (assetCode.length() == 3) {
                graph.addVertex(assetCode);
                System.out.println("Added node: " + assetCode);
            }
        }
        graph.removeVertex("TUSD");

        // Uses file to make edges between nodes
        BufferedReader bufferedReader = new BufferedReader(new FileReader("data.txt"));
        String line = bufferedReader.readLine();
        String[] elements = line.split(", ");
        String baseAssetCode = elements[0].substring(0, 3);
        String quoteAssetCode = elements[0].substring(3, 6);

        DefaultWeightedEdge baseToQuoteEdge = graph.addEdge(baseAssetCode, quoteAssetCode);
        graph.setEdgeWeight(baseToQuoteEdge, Double.parseDouble(elements[1]));

        DefaultWeightedEdge quoteToBaseEdge = graph.addEdge(quoteAssetCode, baseAssetCode);
        graph.setEdgeWeight(quoteToBaseEdge, Double.parseDouble(elements[2]));


        System.out.println(graph.getEdgeWeight(graph.getEdge("ETH", "BTC")));

        System.out.println("Done");
        System.exit(0);
    }
}

// File does not contain:
// 1. bad currencies (BCC, RTX, etc)
// 2.


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




// DISPLAY GRAPH
/*
//System.out.println(graph.toString());

// Find path between 2 nodes
//System.out.println(DijkstraShortestPath.findPathBetween(graph, "BTC", "BTC"));

//JohnsonSimpleCycles<String, DefaultWeightedEdge> johnsonSimpleCycles = new JohnsonSimpleCycles<>(graph);

//System.out.println(johnsonSimpleCycles.findSimpleCycles());
*/