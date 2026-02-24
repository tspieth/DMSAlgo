package localsearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import experiments.ExperimentLocalSearch;
import objects.SwimmingClub;

public class LocalSearch {

    public static long statesCreated;
    public static int iterations;
    public static double avgIterations;

    /**
     * Standard HillClimbing from Lecture SPS at Universitiy of Mannheim
     * 
     * in each Turn the Best Neighbor is choosen;
     * 
     * @param current
     * @return
     */
    public static TeamState hillClimbing(TeamState current) {

        TeamState currentState = current;

        statesCreated = 0; // reset statesCreated
        iterations = 0; // reset avgIterations

        currentState.setEmptyLineup();

        while (true) {

            List<TeamState> neighbors = currentState.createAllNeighbors();

            statesCreated += neighbors.size(); // add created states to total

            TeamState bestNeighbor = TeamState.getBestState(neighbors);

            if (bestNeighbor.getTotalPointsFast() <= currentState.getTotalPointsFast()) {
                break;
            }

            currentState = bestNeighbor;
            iterations++; // update Iterations
        }
        return currentState;
    }

    public static TeamState firstChoiceHillClimbing(TeamState current) {

        TeamState currentState = current;

        statesCreated = 0; // reset statesCreated
        iterations = 0; // reset avgIterations

        while (true) {

            TeamState firstBetter = currentState.getFirstBetterRandomNeighbor();
            // TeamState firstBetter = currentState.getFirstBetterRandomNeighborkSwaps(3);

            if (firstBetter == null) {
                break; // no better neighbor was found
            }

            currentState = firstBetter;
            iterations++; // update Iterations
        }
        return currentState;
    }

    public static TeamState firstChoiceHillClimbingWithKStarts(TeamState current, int k) {
        List<TeamState> allBest = new ArrayList<TeamState>();

        statesCreated = 0; // reset statesCreated
        iterations = 0; // reset from previoud runs
        avgIterations = 0; // reset avgIterations

        for (int i = 0; i < k; i++) {

            allBest.add(firstChoiceHillClimbing(current));

            avgIterations += iterations;

        }
        TeamState best = allBest.get(0);

        for (TeamState state : allBest) {
            if (state.getTotalPoints() > best.getTotalPoints()) {
                best = state;
            }
        }

        avgIterations = avgIterations / (double) k; // calculate avgIterations
        return best;
    }

    public static TeamState hillClimbingWithKStarts(TeamState current, int k) {
        List<TeamState> allBest = new ArrayList<TeamState>();

        statesCreated = 0; // reset statesCreated
        iterations = 0; // reset from previoud runs
        avgIterations = 0; // reset avgIterations

        for (int i = 0; i < k; i++) {

            current.newRandomLineUp();

            allBest.add(hillClimbing(current));

            avgIterations += iterations;

        }
        TeamState best = allBest.get(0);

        for (TeamState state : allBest) {
            if (state.getTotalPoints() > best.getTotalPoints()) {
                best = state;
            }
        }

        avgIterations = avgIterations / (double) k; // calculate avgIterations
        return best;
    }

    public static TeamState hillClimbingWithKSide(TeamState current, int k) {

        int css = 0;

        TeamState currentState = current;

        statesCreated = 0; // reset statesCreated
        iterations = 0; // reset avgIterations
        avgIterations = 0;

        while (true) {

            List<TeamState> neighbors = currentState.createAllNeighbors();

            statesCreated += neighbors.size(); // add created states to total

            TeamState bestNeighbor = TeamState.getBestState(neighbors);
            int valBest = bestNeighbor.getTotalPointsFast();
            int valCur = currentState.getTotalPointsFast();
            if (valBest < valCur) {
                break;
            } else {
                if (valBest == valCur) {
                    css++;
                    if (css == k) {
                        break;
                    }
                } else {
                    css = 0;
                }
                currentState = bestNeighbor;
            }

            iterations++; // update Iterations
        }
        return currentState;
    }

    public static TeamState beamSearch(TeamState teamState, int k) {

        List<TeamState> currentStates = teamState.createRandomStates(k);

        TeamState currentBest = TeamState.getBestState(currentStates);

        while (true) {
            PriorityQueue<TeamState> maxHeap = new PriorityQueue<>(
                    (a, b) -> Integer.compare(b.getTotalPointsFast(), a.getTotalPointsFast()));
            for (TeamState c : currentStates) {
                /*
                 * for (TeamState b : c.createTopKNeighbors(5)) {
                 * maxHeap.add(b);
                 * }
                 */
                maxHeap.add(TeamState.getBestState(c.createAllNeighbors()));

            }
            List<TeamState> neighborsBest = new ArrayList<>();

            // Bestes wird zuerst hinzugefuegt
            TeamState best = maxHeap.poll();
            neighborsBest.add(best);

            for (int i = 1; i < k && !maxHeap.isEmpty(); i++) {
                neighborsBest.add(maxHeap.poll()); // bestes Element holen
            }

            if (currentBest.getTotalPointsFast() >= best.getTotalPointsFast()) {
                return currentBest;
            } else {
                currentStates = neighborsBest;
                System.out.println(best.getTotalPointsFast());
                currentBest = best;
            }
        }
    }

    public static TeamState SimulatedAnnealing(TeamState current, ExponentialSchedule schedule) {

        statesCreated = 0; // reset statesCreated
        iterations = 0; // reset iterations
        avgIterations = 0; // reset avgIterations

        for (int i = 1;; i++) {
            double t = schedule.getTemperature(i);
            iterations++;

            // System.out.println("Aktuelle Temperatur: " + t);
            if (t == 0) {
                return current;
            }

            TeamState next = current.getRandomNeighbor();
            statesCreated += 3214; // just a aprox.

            int E = next.getTotalPointsFast() - current.getTotalPointsFast();

            if (E > 0) {
                current = next;
            } else {
                double p = ExperimentLocalSearch.rng.nextDouble();

                if (p < someFunction1(E, t)) {
                    current = next;
                }
            }
        }
        // return current; => unreachable
    }

    public static TeamState SimulatedAnnealingShavedN(TeamState current, Schedule schedule, int n) {

        statesCreated = 0; // reset statesCreated
        iterations = 0; // reset iterations
        avgIterations = 0; // reset avgIterations

        for (int i = 1;; i++) {
            double t = schedule.getTemperature(i);
            iterations++;

            // System.out.println("Aktuelle Temperatur: " + t);
            if (t == 0) {
                return current;
            }

            TeamState next = current.getRandomNeighborFromTopN(n);
            statesCreated += 3214; // just a aprox.

            int E = next.getTotalPointsFast() - current.getTotalPointsFast();

            if (E > 0) {
                current = next;
            } else {
                double p = ExperimentLocalSearch.rng.nextDouble();

                if (p < someFunction1(E, t)) {
                    current = next;
                }
            }
        }
        // return current; => unreachable
    }

    public static double someFunction1(int E, double t) {
        return Math.exp(E / t);
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

    /*
     * DEPRECATED
     * public static int hillClimbing(SwimmingClub club, boolean isMale) {
     * 
     * TeamState currentState = new TeamState(club, isMale); // initialize current
     * state with the given club
     * 
     * while (true) {
     * long start = System.nanoTime();
     * List<TeamState> neighbors = currentState.createAllNeighbors();
     * long end = System.nanoTime();
     * TeamState bestNeighbor = TeamState.getBestState(neighbors);
     * if (bestNeighbor.getTotalPointsFast() <= currentState.getTotalPointsFast()) {
     * break;
     * }
     * currentState = bestNeighbor;
     * }
     * 
     * return currentState.getTotalPoints(); // return the value of the final state
     * }
     */

}
