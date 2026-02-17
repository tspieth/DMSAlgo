package service;

import java.util.List;

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

    public static int hillClimbing(TeamState current) {

        TeamState currentState = current;

        while (true) {
            List<TeamState> neighbors = currentState.createAllNeighbors();
            TeamState bestNeighbor = TeamState.getBestState(neighbors);
            if (bestNeighbor.getTotalPoints() <= currentState.getTotalPoints()) {
                break;
            }
            System.out.println(currentState.getTotalPoints());
            currentState = bestNeighbor;
        }

        return currentState.getTotalPoints(); // return the value of the final state
    }

}
