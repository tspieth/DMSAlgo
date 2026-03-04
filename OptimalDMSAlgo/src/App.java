
import java.util.ArrayList;
import java.util.List;

import branchNbound.BranchNBound;
import branchNbound.TeamNode;
import experiments.ExperimentLocalSearch;
import io.CSVReader;
import linearProgramming.LinearProgramming;
import linearProgramming.Constraints;
import linearProgramming.Solution;
import linearProgramming.SwimModel;
import localsearch.LocalSearch;
import localsearch.TeamState;
import objects.Competition;
import objects.Swimmer;
import objects.SwimmingClub;

public class App {
    public static void main(String[] args) throws Exception {

        Competition.setBaseTimesMale(CSVReader.getBaseTimes(true, "OptimalDMSAlgo/resources/base_times.csv"));
        Competition.setBaseTimesFemale(CSVReader.getBaseTimes(false, "OptimalDMSAlgo/resources/base_times.csv"));

        int[][] order = CSVReader.getEventOrder("OptimalDMSAlgo/resources/order.csv");
        Competition.setOrder(order);

        int[][] orderMale = CSVReader.getEventOrder("OptimalDMSAlgo/resources/order.csv", true);
        Competition.setOrder(orderMale, true);

        int[][] orderFemale = CSVReader.getEventOrder("OptimalDMSAlgo/resources/order.csv", false);
        Competition.setOrder(orderFemale, false);

        Competition.setSimpleOrder();
        Competition.setSimpleOrderMale();
        Competition.setSimpleOrderFemale();

        List<Swimmer> schwimmerListe = CSVReader.createSwimmer("OptimalDMSAlgo/resources/Duesseldorf_Old.csv");

        SwimmingClub club = new SwimmingClub(schwimmerListe);
        for (Swimmer schwimmer : club.getAllSwimmer()) {
            schwimmer.updatePoints(); // update points for each swimmer based on their times
        }
        club.generateLeaderboards();

        // System.out.println(club.toStringLeaderboards(7, false));
        TeamState teamState = new TeamState(club, false);
        // System.out.println(teamState.toStringShavedLeaderboards());

        // System.out.println(LinearProgramming.toStringsimplexXBnB(club, false, 3));
        // TeamNode.setAvailableSwimmer(club, true);
        // TeamNode.setGlobalLeaderboard();
        // TeamNode.toStringGlobalLead();

        // TeamNode root = new TeamNode(club, true);

        // TeamState best = LocalSearch.hillClimbing(teamState);

        /// System.out.println(best.toStringLineUp());
        // long start = System.nanoTime();
        // BranchNBound.knapSackSolver(root, 0);
        // long end = System.nanoTime();

        // double seconds = (end - start) / 1_000_000_000.0;

        // System.out.println("Dauer: " + seconds + " s");

        // double[][] L = club.createMatrixForSimplex(false);

        // SwimModel model = new SwimModel(L.length, 1, L);
        // Constraints base = SwimModel.buildBaseConstraints(model);

        // Solution sol = LinearProgramming.branchAndBound(model, base);
        // System.out.println("Best objective value = " + sol.bestValue);

        // =========================
        // Experiment Calls
        // =========================

        // ExperimentLocalSearch.beamSearch(teamState, 500);
        // ExperimentLocalSearch.kRestartsFirstChoiceHillClimbing(100, teamState);
        // ExperimentLocalSearch.firstChoiceHillClimbing(teamState);
        // ExperimentLocalSearch.kRestartsHillClimbinFast(1000, teamState);
        // ExperimentLocalSearch.simulatedAnnealingShavedN(teamState, 50);
        // ExperimentLocalSearch.simulatedAnnealing(teamState);
        // ExperimentLocalSearch.kSideStepsHillClimbing(2, teamState);
        // ExperimentLocalSearch.kSideStepsHillClimbing(20, teamState);
        // ExperimentLocalSearch.kRestartsHillClimbing(50, teamState);
        // ExperimentLocalSearch.kRestartsHillClimbing(5, teamState);
        // ExperimentLocalSearch.standardHillClimbingFast(teamState);
        // ExperimentLocalSearch.kRestartsHillClimbinFast(300, teamState);
        // ExperimentLocalSearch.standardHillClimbing(teamState);
        ExperimentLocalSearch.firstChoiceHillClimbingWithKSwaps(teamState, 4);

        /*
         * System.out.println("Punkte vor HillClimb " + teamState.getTotalPoints());
         * System.out.println(teamState.toStringTeamSwimmers());
         * System.out.println(teamState.toStringLineUp());
         * 
         * long start = System.nanoTime();
         * 
         * TeamState best = LocalSearch.hillClimbingWithKStarts(teamState, 2);
         * // TeamState best = LocalSearch.hillClimbing(teamState);
         * long end = System.nanoTime();
         * 
         * System.out.println(best.toStringTeamSwimmers());
         * System.out.println(best.toStringLineUp());
         * 
         * double seconds = (end - start) / 1_000_000_000.0;
         * 
         * System.out.println("Dauer: " + seconds + " s");
         * 
         */
    }

}
