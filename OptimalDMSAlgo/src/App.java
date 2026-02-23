
import java.util.ArrayList;
import java.util.List;

import branchNbound.BranchNBound;
import branchNbound.TeamNode;
import experiments.ExperimentLocalSearch;
import io.CSVReader;
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

        List<Swimmer> schwimmerListe = CSVReader.createSwimmer("OptimalDMSAlgo/resources/first7BetterClub.csv");

        SwimmingClub club = new SwimmingClub(schwimmerListe);
        for (Swimmer schwimmer : club.getAllSwimmer()) {
            schwimmer.updatePoints(); // update points for each swimmer based on their times
        }
        club.generateLeaderboards();

        // TeamState teamState = new TeamState(club, true);

        TeamNode testNode = new TeamNode(club, true);

        TeamNode best = BranchNBound.knapSackSolver(testNode, 0);
        // System.out.println(best.getTotalPoints());
        // System.out.println(club.toStringLeaderboards(100, true));
        // System.out.print(testNode.toStringSimpleLeaderBoard());
        // System.out.println(testNode.getUpperBound());

        // ===================
        // Experiment Calls
        // ===================

        // ExperimentLocalSearch.kRestartsFirstChoiceHillClimbing(100, teamState);
        // ExperimentLocalSearch.firstChoiceHillClimbing(teamState);
        // ExperimentLocalSearch.kRestartsHillClimbing(100000, teamState);
        // ExperimentLocalSearch.simulatedAnnealingShavedN(teamState, 50);
        // ExperimentLocalSearch.simulatedAnnealing(teamState);
        // ExperimentLocalSearch.kSideStepsHillClimbing(2, teamState);
        // ExperimentLocalSearch.kSideStepsHillClimbing(20, teamState);
        // ExperimentLocalSearch.kRestartsHillClimbing(50, teamState);
        // ExperimentLocalSearch.kRestartsHillClimbing(5, teamState);
        // ExperimentLocalSearch.standardHillClimbing(teamState);

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
