package com.develorain;

import org.jgrapht.graph.DefaultWeightedEdge;

public class CustomEdge extends DefaultWeightedEdge {
    public Symbol symbol;
    public String sourceNode;
    public String targetNode;

    public double tradeQuantity;

    public boolean sameDirectionAsSymbol;

    public CustomEdge(Symbol symbol, String sourceNode, String targetNode) {
        this.symbol = symbol;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;

        if (sourceNode.equalsIgnoreCase(symbol.baseAsset)) {
            sameDirectionAsSymbol = true;
        } else {
            sameDirectionAsSymbol = false;
        }
    }
}
