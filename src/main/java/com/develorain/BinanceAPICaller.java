package com.develorain;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.market.BookTicker;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class BinanceAPICaller {
    private static BinanceApiRestClient client;

    public static void initialize() {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("My2zlMkv4yorboQMABkUSqcNosJEqVZNi6JzPEvQovzbiVusGrf0ZkLF9rHkQAe7", "nUecuN1O33QAYXLdY76s12BME3fLafphBhj0kUl67Cs3seYxp8xzJ8JqVD7mYwJr");
        client = factory.newRestClient();
    }

    public static void createGraphNodes(SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
        // Make nodes with each currency
        for (Asset asset : client.getAllAssets()) {
            String assetCode = asset.getAssetCode();

            if (assetCode.length() == 3) {
                graph.addVertex(assetCode);
            }
        }
    }

    public static void createGraphEdges(SimpleDirectedWeightedGraph<String, CustomEdge> graph) {
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
                System.out.println("Warning: Attempting to create edge with non-existent node: " + baseAssetCode);
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
}
