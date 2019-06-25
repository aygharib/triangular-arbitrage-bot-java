package com.develorain;

import org.jgrapht.graph.DefaultWeightedEdge;

public class CustomEdge extends DefaultWeightedEdge {
    private Double price;
    private Double amount;
    public String symbol;
    public String sourceNode;
    public String targetNode;
    public String baseAssetCode;
    public String quoteAssetCode;

    public CustomEdge(Double price, Double amount, String symbol, String sourceNode, String targetNode) {
        this.price = price;
        this.amount = amount;
        this.symbol = symbol;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;

        this.baseAssetCode = symbol.substring(0, 3);
        this.quoteAssetCode = symbol.substring(3, 6);
    }

    public Double getPrice() {
        return price;
    }

    public Double getAmount() {
        return amount;
    }
}
