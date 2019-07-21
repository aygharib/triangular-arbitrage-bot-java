package com.develorain;

import org.jgrapht.graph.DefaultWeightedEdge;

public class CustomEdge extends DefaultWeightedEdge {
    public Symbol symbol;
    public String sourceNode;
    public String targetNode;

    public double worstCaseTradeRate;
    public double tradeQuantity;

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

    public double bestCaseTradePrice() {
        return sameDirectionAsSymbol ? symbol.askPrice : symbol.bidPrice;
    }
}
