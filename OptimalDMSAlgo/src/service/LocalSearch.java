package service;

import java.util.ArrayList;
import java.util.List;

import objects.SwimmingClub;

public class LocalSearch {

    public static int hillClimbing(SwimmingClub club, boolean isMale) {

        TeamState currentState = new TeamState(club, isMale); // initialize current state with the given club

        while (true) {
            long start = System.nanoTime();
            List<TeamState> neighbors = currentState.createAllNeighbors();
            long end = System.nanoTime();
            TeamState bestNeighbor = TeamState.getBestState(neighbors);
            if (bestNeighbor.getTotalPointsFast() <= currentState.getTotalPointsFast()) {
                break;
            }
            currentState = bestNeighbor;
        }

        return currentState.getTotalPoints(); // return the value of the final state
    }

    public static TeamState hillClimbing(TeamState current) {

        TeamState currentState = current;

        int durchlauf = 0;
        while (true) {
            durchlauf += 1;

            long start = System.nanoTime();
            List<TeamState> neighbors = currentState.createAllNeighbors();

            System.out.println("Erzeugte Nachbarn: " + neighbors.size());

            long end = System.nanoTime();
            double seconds = (end - start) / 1_000_000_000.0;

            // System.out.println("Dauer Nachbarberechnung: " + seconds + " s");

            start = System.nanoTime();
            TeamState bestNeighbor = TeamState.getBestState(neighbors);
            end = System.nanoTime();

            seconds = (end - start) / 1_000_000_000.0;

            // System.out.println("Dauer Bestensuche: " + seconds + " s");

            if (bestNeighbor.getTotalPointsFast() <= currentState.getTotalPointsFast()) {
                break;
            }
            // System.out.println("Fast: " + currentState.getTotalPointsFast());
            // System.out.println("Slow: " + currentState.getTotalPoints());
            currentState = bestNeighbor;
        }
        System.out.println(currentState.toStringTeamSwimmers());
        System.out.println(currentState.toStringLineUp());
        System.out.println("Insgesamt: " + durchlauf + " Durchlaeufe");
        return currentState;
    }

    public static TeamState hillClimbingWithKStarts(TeamState current, int k) {
        List<TeamState> allBest = new ArrayList<TeamState>();

        for (int i = 0; i < k; i++) {
            current.newRandomLineUp();
            allBest.add(hillClimbing(current));

        }
        TeamState best = allBest.get(0);

        for (TeamState state : allBest) {
            if (state.getTotalPoints() > best.getTotalPoints()) {
                best = state;
            }
        }
        return best;
    }

    /*
     * DEPRECATED
     * public static TeamState hillClimbingWithKStarts(int k) {
     * List<TeamState> allBest = new ArrayList<TeamState>();
     * 
     * List<Swimmer> schwimmerListe =
     * CSVReader.createSwimmer("OptimalDMSAlgo/resources/SVM.csv");
     * 
     * SwimmingClub club = new SwimmingClub(schwimmerListe);
     * 
     * for (Swimmer schwimmer : club.getAllSwimmer()) {
     * schwimmer.updatePoints(); // update points for each swimmer based on their
     * times
     * }
     * 
     * club.generateLeaderboards();
     * 
     * for (int i = 0; i < k; i++) {
     * TeamState teamState;
     * do {
     * teamState = new TeamState(club, true);
     * } while (teamState.lineUp == null);
     * 
     * long start = System.nanoTime();
     * 
     * allBest.add(hillClimbing(teamState));
     * 
     * long end = System.nanoTime();
     * 
     * double seconds = (end - start) / 1_000_000_000.0;
     * 
     * System.out.println("Dauer HillClimb: " + seconds + " s");
     * 
     * }
     * TeamState best = allBest.get(0);
     * 
     * for (TeamState state : allBest) {
     * if (state.getTotalPoints() > best.getTotalPoints()) {
     * best = state;
     * }
     * }
     * return best;
     * }
     */

}
