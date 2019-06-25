package com.develorain;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.NewOrderResponseType;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.exception.BinanceApiException;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class BinanceAPICaller {
    private static BinanceApiRestClient client;

    public static void initialize() {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("My2zlMkv4yorboQMABkUSqcNosJEqVZNi6JzPEvQovzbiVusGrf0ZkLF9rHkQAe7", "nUecuN1O33QAYXLdY76s12BME3fLafphBhj0kUl67Cs3seYxp8xzJ8JqVD7mYwJr");
        client = factory.newRestClient();

        //NewOrder test = NewOrder.marketBuy("ETHBTC", "1000");
        //System.out.println(test.toString());
        //client.newOrderTest(test);
    }

    public static void convertCurrency(String from, String to, String amountOriginal) {
        try {
            NewOrderResponse newOrderResponse = client.newOrder(NewOrder.marketBuy("ETHBTC", amountOriginal).newOrderRespType(NewOrderResponseType.FULL));
            System.out.println(newOrderResponse);
        } catch (BinanceApiException e) {
            System.out.println("Transaction failed: Attempting to trade below minimum trade threshold");
        }
    }

    public static void performCycle(Cycle cycle, double amountInOriginalCurrency) {

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

                CustomEdge quoteToBaseEdge = new CustomEdge(1.0/Double.parseDouble(bookTicker.getBidPrice()), Double.parseDouble(bookTicker.getBidQty()));
                graph.addEdge(quoteAssetCode, baseAssetCode, quoteToBaseEdge);
            } catch (NullPointerException e) {
                System.out.println("Problematic: " + symbol);
            }
        }
    }
}
