package com.develorain;

import org.jgrapht.graph.DefaultWeightedEdge;

public class CustomEdge extends DefaultWeightedEdge {
    private Double price;
    private Double amount;

    public CustomEdge(Double price, Double amount) {
        this.price = price;
        this.amount = amount;
    }

    public Double getPrice() {
        return price;
    }

    public Double getAmount() {
        return amount;
    }
}
