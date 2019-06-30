package com.develorain;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.io.FileWriter;

public class Main {
    public static final Double TRANSACTION_FEE_RATIO = 1 - 0.000750;
    public static final int HOW_MANY_CYCLES = 100;

    private Main() {
        BinanceAPICaller.initialize();

        SimpleDirectedWeightedGraph<String, CustomEdge> graph = createGraph();

        Cycle[] sortedCyclesByMultiplier = GraphProcessing.getSortedCyclesByMultiplier(graph);

        System.out.println(sortedCyclesByMultiplier[0]);

        /*
        for (int j = 0; j < sortedCyclesByMultiplier[0].size; j++) {
            System.out.println(sortedCyclesByMultiplier[0].cycleString.get(j) + "-> Amount to trade : " + sortedCyclesByMultiplier[0].actualTradeQuantitiesForEachCurrency[j] + " with rate: " + sortedCyclesByMultiplier[0].tradePrices[j] + "\n");
        }
        */

        //writeCyclesToFile(sortedCyclesByMultiplier, 0);

        // Do the first cycle
        //BinanceAPICaller.performCycle(graph, sortedCyclesByMultiplier[0]);

        /*
        System.out.print("Sell base: ");
        System.out.println(graph.getEdge("TRX", "XRP").getPrice());
        System.out.print("Buy base: ");
        System.out.println(graph.getEdge("XRP", "TRX").getPrice());
         */
    }

    private SimpleDirectedWeightedGraph<String, CustomEdge> createGraph() {
        SimpleDirectedWeightedGraph<String, CustomEdge> graph = new SimpleDirectedWeightedGraph<>(CustomEdge.class);

        BinanceAPICaller.createGraphNodes(graph);
        BinanceAPICaller.createGraphEdges(graph);

        return graph;
    }

    private void writeCyclesToFile(Cycle[] sortedCyclesByMultiplier, int cancer) {
        try {
            FileWriter fileWriter = new FileWriter("cycles" + cancer + ".txt");

            for (int i = 0; i < HOW_MANY_CYCLES; i++) {
                Cycle cycle = sortedCyclesByMultiplier[i];

                // Write cycle to file
                fileWriter.write(cycle.toString() + "\n");


                // Write cycle data after cycle
                for (int j = 0; j < cycle.size; j++) {
                    fileWriter.write(cycle.cycleString.get(j) + "-> Amount to trade : " + cycle.actualTradeQuantitiesForEachCurrency[j] + " with rate: " + cycle.tradePrices[j] + "\n");
                }

                fileWriter.write("\n\n\n");
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