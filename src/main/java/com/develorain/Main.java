package com.develorain;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.io.FileWriter;

public class Main {
    public static final Double TRANSACTION_FEE_RATIO = 1 - 0.000750;
    public static final int HOW_MANY_CYCLES = 100;

    private Main() {
        BinanceAPICaller.initialize();

        SimpleDirectedWeightedGraph<String, CustomEdge> graph = createGraph();

        Object[] sortedCyclesByMultiplier = GraphProcessing.getSortedCyclesByMultiplier(graph);

        writeCyclesToFile(sortedCyclesByMultiplier);
    }

    private SimpleDirectedWeightedGraph<String, CustomEdge> createGraph() {
        SimpleDirectedWeightedGraph<String, CustomEdge> graph = new SimpleDirectedWeightedGraph<>(CustomEdge.class);

        BinanceAPICaller.createGraphNodes(graph);
        BinanceAPICaller.createGraphEdges(graph);

        return graph;
    }

    private void writeCyclesToFile(Object[] sortedCyclesByMultiplier) {
        try {
            FileWriter fileWriter = new FileWriter("cycles.txt");

            for (int i = 0; i < HOW_MANY_CYCLES; i++) {
                Cycle cycle = (Cycle) sortedCyclesByMultiplier[i];
                fileWriter.write(cycle.toString() + "\n");
            }

            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main();

        System.out.println("Done");
        System.exit(0);
    }
}