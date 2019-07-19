package com.develorain;

import org.jgrapht.graph.DefaultWeightedEdge;

public class CustomEdge extends DefaultWeightedEdge {
    private Double price;
    private Double amount;
    public Symbol symbol;
    public String sourceNode;
    public String targetNode;

    public CustomEdge(Double price, Double amount, Symbol symbol, String sourceNode, String targetNode) {
        this.price = price;
        this.amount = amount;
        this.symbol = symbol;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }

    public Double getPrice() {
        return price;
    }

    public Double getAmount() {
        return amount;
    }
}
