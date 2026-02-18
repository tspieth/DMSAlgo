package service;

import java.util.ArrayList;
import java.util.List;

import io.CSVReader;
import objects.Swimmer;
import objects.SwimmingClub;

public class LocalSearch {

    public static int hillClimbing(SwimmingClub club, boolean isMale) {

        TeamState currentState = new TeamState(club, isMale); // initialize current state with the given club

        while (true) {
            List<TeamState> neighbors = currentState.createAllNeighbors();
            TeamState bestNeighbor = TeamState.getBestState(neighbors);
            if (bestNeighbor.getTotalPoints() <= currentState.getTotalPoints()) {
                break;
            }
            currentState = bestNeighbor;
        }

        return currentState.getTotalPoints(); // return the value of the final state
    }

    public static TeamState hillClimbing(TeamState current) {

        TeamState currentState = current;

        while (true) {
            List<TeamState> neighbors = currentState.createAllNeighbors();
            TeamState bestNeighbor = TeamState.getBestState(neighbors);
            if (bestNeighbor.getTotalPoints() <= currentState.getTotalPoints()) {
                break;
            }
            // System.out.println(currentState.getTotalPoints());
            currentState = bestNeighbor;
        }
        System.out.println(currentState.toStringLineUp());
        // System.out.println(currentState.getTotalPoints());
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

    public static TeamState hillClimbingWithKStarts(int k) {
        List<TeamState> allBest = new ArrayList<TeamState>();

        for (int i = 0; i < k; i++) {

            List<Swimmer> schwimmerListe = CSVReader.createSwimmer("OptimalDMSAlgo/resources/betterClub.csv");

            SwimmingClub club = new SwimmingClub(schwimmerListe);

            for (Swimmer schwimmer : club.getAllSwimmer()) {
                schwimmer.updatePoints(); // update points for each swimmer based on their times
            }
            club.generateLeaderboards();
            // System.out.println(club.toStringLeaderboards(5, true));
            // System.out.println(club.toStringLeaderboards(5, false));

            TeamState teamState = new TeamState(club, true);

            allBest.add(hillClimbing(teamState));

        }
        TeamState best = allBest.get(0);

        for (TeamState state : allBest) {
            if (state.getTotalPoints() > best.getTotalPoints()) {
                best = state;
            }
        }
        return best;
    }
}
