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

import java.net.URI;

public class Main {
    public static void main(String[] args) {
        // Set up binance client
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("My2zlMkv4yorboQMABkUSqcNosJEqVZNi6JzPEvQovzbiVusGrf0ZkLF9rHkQAe7", "nUecuN1O33QAYXLdY76s12BME3fLafphBhj0kUl67Cs3seYxp8xzJ8JqVD7mYwJr");
        BinanceApiRestClient client = factory.newRestClient();

        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        
        // Make nodes with each currency
        for (Asset asset : client.getAllAssets()) {
            String assetCode = asset.getAssetCode();
            graph.addVertex(assetCode);

            System.out.println("Add node: " + assetCode);
        }

        // Make all connections between currencies
        for (BookTicker bookTicker : client.getBookTickers()) {
            String symbol = bookTicker.getSymbol();

            // Get asset codes for base and quote
            String baseAssetCode = client.getExchangeInfo().getSymbolInfo(symbol).getBaseAsset();
            String quoteAssetCode = client.getExchangeInfo().getSymbolInfo(symbol).getQuoteAsset();

            // Connect these currencies with their corresponding weights
            System.out.println(symbol);
            DefaultWeightedEdge baseToQuoteEdge = graph.addEdge(baseAssetCode, quoteAssetCode);
            graph.setEdgeWeight(baseToQuoteEdge, Double.parseDouble(bookTicker.getAskPrice()));

            DefaultWeightedEdge quoteToBaseEdge = graph.addEdge(quoteAssetCode, baseAssetCode);
            graph.setEdgeWeight(quoteToBaseEdge, Double.parseDouble(bookTicker.getBidPrice()));

            //System.out.println(bookTicker.getAskPrice() + ", " + bookTicker.getAskQty());
            //System.out.println(bookTicker.getBidPrice() + ", " + bookTicker.getBidQty());
        }

        System.out.println(graph.toString());

        System.out.println(DijkstraShortestPath.findPathBetween(graph, "BTC", "BTC"));

        JohnsonSimpleCycles<String, DefaultWeightedEdge> johnsonSimpleCycles = new JohnsonSimpleCycles<>(graph);

        System.out.println(johnsonSimpleCycles.findSimpleCycles());
    }
}