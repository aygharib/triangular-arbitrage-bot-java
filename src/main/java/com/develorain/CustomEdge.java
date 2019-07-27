package com.develorain;

import org.jgrapht.graph.DefaultWeightedEdge;

public class CustomEdge extends DefaultWeightedEdge {
    public Symbol symbol;
    public String sourceNode;
    public String targetNode;

    public double worstCaseTradeRate;
    public double averageCaseTradeRate;

    public double tradeQuantity;
    public double tradeQuantityInBaseCurrency;

    public boolean sameDirectionAsSymbol;

    public CustomEdge(Symbol symbol, String sourceNode, String targetNode) {
        this.symbol = symbol;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;

        sameDirectionAsSymbol = sourceNode.equalsIgnoreCase(symbol.baseAsset);
    }

    public double worstCaseTradePrice() {
        return sameDirectionAsSymbol ? symbol.bidPrice : symbol.askPrice;
    }

    private double bestCaseTradePrice() {
        return sameDirectionAsSymbol ? symbol.askPrice : symbol.bidPrice;
    }

    public double averageCaseTradePrice() {
        return bestCaseTradePrice() * 1 + worstCaseTradePrice() * 0;
    }

    public boolean amISellingBaseCurrency() {
        return sameDirectionAsSymbol;
    }
}
