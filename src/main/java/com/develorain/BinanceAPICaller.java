package com.develorain;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.TimeInForce;
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
            Symbol symbol = new Symbol(bookTicker.getSymbol(),
                    Double.parseDouble(bookTicker.getAskPrice()),
                    Double.parseDouble(bookTicker.getBidPrice()),
                    Double.parseDouble(bookTicker.getAskQty()),
                    Double.parseDouble(bookTicker.getBidQty()));

            // Do not include symbols that are not 6 letters long
            if (!symbol.temporarilyParsable) {
                continue;
            }

            // Do not include symbols with a price of zero (aka these symbols don't actually exist, such as PAXETH)
            if (Double.parseDouble(bookTicker.getAskPrice()) == 0.0 || Double.parseDouble(bookTicker.getBidPrice()) == 0.0) {
                System.out.println("Warning: The following symbol has an ask or bid price of zero, so it is being removed: " + symbol.symbolString);
                continue;
            }

            if (!graph.containsVertex(symbol.baseAsset) || !graph.containsVertex(symbol.quoteAsset)) {
                System.out.println("Warning: Attempting to create edge with non-existent node: " + symbol.symbolString);
                continue;
            }

            // Connect these currencies with their corresponding weights
            try {
                // Sell edge is in the same direction as symbolString
                String sellSourceNode = symbol.baseAsset;
                String sellTargetNode = symbol.quoteAsset;

                // Buy edge is in the opposite direction as symbolString
                String buySourceNode = symbol.quoteAsset;
                String buyTargetNode = symbol.baseAsset;

                CustomEdge sellEdge = new CustomEdge(symbol, sellSourceNode, sellTargetNode);
                CustomEdge buyEdge = new CustomEdge(symbol, buySourceNode, buyTargetNode);

                graph.addEdge(sellSourceNode, sellTargetNode, sellEdge);
                graph.addEdge(buySourceNode, buyTargetNode, buyEdge);
            } catch (NullPointerException e) {
                System.out.println("Problematic symbol: " + symbol.symbolString);
            }
        }
    }

    public static void performCycle(Cycle cycle, boolean dryRun) {
        for (int i = 0; i < 1; i++) { // i < cycle.size
            convertCurrency(cycle.edges[i], Double.toString(cycle.edges[i].tradeQuantityInBaseCurrency), Double.toString(cycle.edges[i].averageCaseTradePrice()), dryRun);
        }

        System.out.println("Completed cycle");
    }

    public static void convertCurrency(CustomEdge edge, String tradeQuantityInBaseCurrency, String averageCaseTradePrice, boolean dryRun) {
        try {
            System.out.println("Convert: " + edge.sourceNode + "->" + edge.targetNode);

            if (edge.amISellingBaseCurrency()) {
                System.out.println("We are selling base currency: " + edge.symbol.symbolString + ", " + tradeQuantityInBaseCurrency + ", " + averageCaseTradePrice);

                if (!dryRun) {
                    NewOrderResponse newOrderResponse =
                            client.newOrder(NewOrder.limitSell(edge.symbol.symbolString, TimeInForce.GTC, tradeQuantityInBaseCurrency, averageCaseTradePrice)
                                    .newOrderRespType(NewOrderResponseType.FULL));
                    System.out.println(newOrderResponse);
                }
            } else {
                System.out.println("We are buying base currency: " + edge.symbol.symbolString + ", " + tradeQuantityInBaseCurrency + ", " + averageCaseTradePrice);

                if (!dryRun) {
                    NewOrderResponse newOrderResponse =
                            client.newOrder(
                                    NewOrder.limitBuy(
                                            edge.symbol.symbolString,
                                            TimeInForce.GTC,
                                            tradeQuantityInBaseCurrency,
                                            averageCaseTradePrice).newOrderRespType(NewOrderResponseType.FULL));
                    System.out.println(newOrderResponse);
                }
            }
        } catch (BinanceApiException e) {
            System.out.println("Transaction failed: Attempting to trade below minimum trade threshold");
            e.printStackTrace();
        }
    }
}
