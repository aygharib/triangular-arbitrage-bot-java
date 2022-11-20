package com.develorain;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static final Double TRANSACTION_FEE_RATIO = 1 - 0.000750;
    public static final int HOW_MANY_CYCLES_TO_OUTPUT = 100;

    private Main() {
        long startTime, endTime;

        startTime = System.currentTimeMillis();
        BinanceAPICaller.initialize();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds to create Binance API Caller");

        SimpleDirectedWeightedGraph<String, CustomEdge> graph = createGraph();
        Cycle[] sortedCyclesByMultiplier = GraphProcessing.getSortedCyclesByMultiplier(graph);
        writeCyclesToFile(sortedCyclesByMultiplier, graph, "cycles.txt");

        iterate();
    }

    public void iterate() {
        long startTime, endTime;

        while (true){
            startTime = System.currentTimeMillis();
            SimpleDirectedWeightedGraph<String, CustomEdge> graph = createGraph();
            endTime = System.currentTimeMillis();
            System.out.println((endTime - startTime) + " milliseconds to create Graph");

            startTime = System.currentTimeMillis();
            Cycle[] sortedCyclesByMultiplier = GraphProcessing.getSortedCyclesByMultiplier(graph);
            endTime = System.currentTimeMillis();
            System.out.println((endTime - startTime) + " milliseconds to create Get Sorted Cycles");

            Date date = new Date();
            long time = date.getTime();
            Timestamp ts = new Timestamp(time);
            logCycles(sortedCyclesByMultiplier, graph, "outputCycles.txt", time, ts);
        }
    }

    public void logCycles(Cycle[] sortedCyclesByMultiplier, SimpleDirectedWeightedGraph<String, CustomEdge> graph, String fileName, long time, Timestamp ts) {
        try {
            FileWriter fileWriter = new FileWriter(fileName, true);

            fileWriter.write(ts + ": ");

            Cycle cycle = sortedCyclesByMultiplier[0];

            if (cycle.worstCaseMultiplier >= 1) {
                fileWriter.write(cycle.toString() + "\n");
            } else {
                fileWriter.write("No Profitable Cycle");
            }

            fileWriter.write("\n");
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void promptEnterKey() {
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    private SimpleDirectedWeightedGraph<String, CustomEdge> createGraph() {
        SimpleDirectedWeightedGraph<String, CustomEdge> graph = new SimpleDirectedWeightedGraph<>(CustomEdge.class);

        BinanceAPICaller.createGraphNodes(graph);
        BinanceAPICaller.createGraphEdges(graph);

        return graph;
    }

    private void writeCyclesToFile(Cycle[] sortedCyclesByMultiplier, SimpleDirectedWeightedGraph<String, CustomEdge> graph, String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write("\n\n\n");

            for (int i = 0; i < HOW_MANY_CYCLES_TO_OUTPUT; i++) {
                Cycle cycle = sortedCyclesByMultiplier[i];

                // Write cycle to file
                fileWriter.write(cycle.toString() + "\n");

                fileWriter.write("Start cycle with " + Tools.formatAmount(cycle.edges[0].tradeQuantity) + " " + cycle.cycleString.get(0) + "\n\n");

                // Write cycle data after cycle
                for (int j = 0; j < cycle.size; j++) {
                    String sourceNode = cycle.cycleString.get(j);
                    String targetNode = cycle.cycleString.get((j + 1) % cycle.size);

                    CustomEdge sourceToTargetEdge = graph.getEdge(sourceNode, targetNode);

                    if (sourceToTargetEdge.amISellingBaseCurrency()) {
                        fileWriter.write("Using bottom-half price\n");
                        fileWriter.write(sourceNode + "--->" + targetNode + " | " + "Worst case price >= " + Tools.formatPrice(cycle.edges[j].worstCaseTradePrice()) + " Average case price = " + Tools.formatPrice(cycle.edges[j].averageCaseTradePrice()) + " \n\n");
                    } else {
                        fileWriter.write("Using top-half price\n");
                        fileWriter.write(sourceNode + "--->" + targetNode + " | " + "Worst case price <= " + Tools.formatPrice(cycle.edges[j].worstCaseTradePrice()) + " Average case price = " + Tools.formatPrice(cycle.edges[j].averageCaseTradePrice()) + " \n\n");
                    }
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