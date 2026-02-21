package experiments;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import io.CSVWriter;
import service.LocalSearch;
import service.TeamState;

public class ExperimentLocalSearch {

    public static Random rng = new Random(42); // fester Seed
    public static int countData = 30;

    // Just for Fun
    private static boolean progressBarInitialized = false;

    /**
     * Startet Standard-Hill-Climbing k Mal und erstellt ein .csv Datei im
     * Verzeichnis:
     * OptimalDMSAlgo/data
     * 
     * @param k
     */
    public static void standardHillClimbing(int k, TeamState teamState) throws IOException {

        // Just for Fun
        // print initial progress bar (0%)
        System.out.println("Experiment \"Standart Hill Climbing\":");
        printProgressBar(0, k);

        CSVWriter standardWriter = new CSVWriter("OptimalDMSAlgo/data/standardHill.csv", ";");

        standardWriter.writeHeader(Arrays.asList("variant", "run", "score", "time_ms", "avg_iterations", "states"));

        for (int i = 0; i <= 3; i++) {
            LocalSearch.hillClimbing(teamState); // JVM Warm-Up
        }

        for (int i = 1; i <= k; i++) {

            teamState.newRandomLineUp(); // so runs dont give same outcome

            long start = System.nanoTime();
            TeamState best = LocalSearch.hillClimbing(teamState);
            long end = System.nanoTime();

            long durationNs = end - start;
            double durationMs = durationNs / 1_000_000.0;

            String run = Integer.toString(i);
            String score = Integer.toString(best.getTotalPoints());
            String time_ms = String.format(Locale.US, "%.3f", durationMs); // Locale.US so it uses . instead of ,
            String iterations = Double.toString(LocalSearch.avgIterations);
            String states = Long.toString(LocalSearch.statesCreated);

            /*
             * variant,run,score,time_ms,avg_iterations,states
             * best;1;842;7100;31;111230
             * best;2;850;6950;29;108440
             * best;3;838;7239;30;145000
             */
            standardWriter.appendRow(Arrays.asList("standard", run, score, time_ms, iterations, states));

            // update progress bar after finishing this run
            printProgressBar(i, k);
        }
    }

    private static void printProgressBar(int done, int total) {
        int width = 40;
        double ratio = (double) done / (double) total;
        int filled = (int) (ratio * width);
        StringBuilder bar = new StringBuilder();
        bar.append('[');
        for (int i = 0; i < filled; i++)
            bar.append('#');
        for (int i = filled; i < width; i++)
            bar.append(' ');
        bar.append(']');
        int percent = (int) (ratio * 100);

        // If we've already printed the bar once, move cursor up and clear that line
        if (progressBarInitialized) {
            System.out.print("\u001B[1A"); // move cursor up 1 line
            System.out.print("\u001B[2K"); // clear whole line
        } else {
            progressBarInitialized = true;
        }

        System.out.print(String.format("%s %3d%% (%d/%d)%n", bar.toString(), percent, done, total));
        System.out.flush();
    }

}
