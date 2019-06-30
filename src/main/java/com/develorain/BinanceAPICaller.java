package com.develorain;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.exception.BinanceApiException;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class BinanceAPICaller {
    private static BinanceApiRestClient client;

    public static void initialize() {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("My2zlMkv4yorboQMABkUSqcNosJEqVZNi6JzPEvQovzbiVusGrf0ZkLF9rHkQAe7", "nUecuN1O33QAYXLdY76s12BME3fLafphBhj0kUl67Cs3seYxp8xzJ8JqVD7mYwJr");
        client = factory.newRestClient();
    }

    public static boolean isMeSellingBaseCurrencyOrder(CustomEdge edge) {
        if (edge.baseAssetCode.equalsIgnoreCase(edge.sourceNode)) {
            return true;
        } else {
            return false;
        }
    }

    public static void convertCurrency(CustomEdge edge, String amountInOriginalCurrency) {
        boolean buyFlag = isMeSellingBaseCurrencyOrder(edge);

        try {
            System.out.println("Convert: " + edge.sourceNode + "->" + edge.targetNode);
            if (buyFlag) {
                //NewOrderResponse newOrderResponse = client.newOrder(NewOrder.marketBuy(edge.symbol, amountInOriginalCurrency).newOrderRespType(NewOrderResponseType.FULL));
                //System.out.println(newOrderResponse);
            } else {
                //NewOrderResponse newOrderResponse = client.newOrder(NewOrder.marketSell(edge.symbol, amountInOriginalCurrency).newOrderRespType(NewOrderResponseType.FULL));
                //System.out.println(newOrderResponse);
            }
        } catch (BinanceApiException e) {
            System.out.println("Transaction failed: Attempting to trade below minimum trade threshold");
            e.printStackTrace();
        }
    }

    public static void performCycle(SimpleDirectedWeightedGraph<String, CustomEdge> graph, Cycle cycle) {
        for (int i = 0; i < cycle.size; i++) {
            String sourceNode = cycle.cycleString.get(i);
            String targetNode = cycle.cycleString.get((i + 1) % cycle.size);

            convertCurrency(graph.getEdge(sourceNode, targetNode), Double.toString(cycle.actualTradeQuantitiesForEachCurrency[i]));
        }

        System.out.println("Completed cycle");
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

            // REMOVE ANY SYMBOLS WITH LONGER THAN 6 LETTERS, AND ALSO REMOTE SYMBOLS THAT HAVE PRICE ZERO (AKA THEY DONT ACTUALLY EXIST, SUCH AS PAXETH)
            if (symbol.length() != 6) {
                continue;
            }

            if (Double.parseDouble(bookTicker.getAskPrice()) == 0.0) {
                System.out.println("Warning: The following symbol has an ask price of zero, so it is being removed: " + symbol);
                continue;
            }

            if (Double.parseDouble(bookTicker.getBidPrice()) == 0.0) {
                System.out.println("Warning: The following symbol has a bid of zero, so it is being removed: " + symbol);
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
                CustomEdge sellEdge = new CustomEdge(Double.parseDouble(bookTicker.getBidPrice()), Double.parseDouble(bookTicker.getBidQty()), symbol, baseAssetCode, quoteAssetCode);
                CustomEdge buyEdge = new CustomEdge(Double.parseDouble(bookTicker.getAskPrice()), Double.parseDouble(bookTicker.getAskQty()), symbol, quoteAssetCode, baseAssetCode);

                graph.addEdge(baseAssetCode, quoteAssetCode, sellEdge);
                graph.addEdge(quoteAssetCode, baseAssetCode, buyEdge);
            } catch (NullPointerException e) {
                System.out.println("Problematic: " + symbol);
            }
        }
    }
}
