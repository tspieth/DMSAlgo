
import java.util.ArrayList;
import java.util.List;

import branchNbound.BranchNBound;
import branchNbound.TeamNode;
import experiments.ExperimentLocalSearch;
import io.CSVReader;
import io.CSVWriter;
import linearProgramming.LinearProgramming;
import linearProgramming.Constraints;
import linearProgramming.Solution;
import linearProgramming.SwimModel;
import localsearch.ExponentialSchedule;
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

        // Erstellung vom BetterClub
        List<Swimmer> betterSwimmers = CSVReader.createSwimmer("OptimalDMSAlgo/resources/betterClub.csv");

        SwimmingClub betterClub = new SwimmingClub(betterSwimmers);
        for (Swimmer schwimmer : betterClub.getAllSwimmer()) {
            schwimmer.updatePoints(); // update points for each swimmer based on their times
        }
        betterClub.generateLeaderboards();

        TeamState betterState = new TeamState(betterClub, true);

        // Erstellung vom SVM
        List<Swimmer> svmSwimmers = CSVReader.createSwimmer("OptimalDMSAlgo/resources/SVM.csv");

        SwimmingClub svm = new SwimmingClub(svmSwimmers);
        for (Swimmer schwimmer : svm.getAllSwimmer()) {
            schwimmer.updatePoints(); // update points for each swimmer based on their times
        }
        svm.generateLeaderboards();

        TeamState svmState = new TeamState(svm, true);

        // Erstellung von FSD
        List<Swimmer> fsdSwimmers = CSVReader.createSwimmer("OptimalDMSAlgo/resources/Duesseldorf_Old.csv");

        SwimmingClub fsd = new SwimmingClub(fsdSwimmers);
        for (Swimmer schwimmer : fsd.getAllSwimmer()) {
            schwimmer.updatePoints(); // update points for each swimmer based on their times
        }
        fsd.generateLeaderboards();

        TeamState fsdFemale = new TeamState(fsd, false);
        TeamState fsdMale = new TeamState(fsd, true);

        // System.out.println(LocalSearch.hillClimbing(teamState).toStringLineUp());
        // System.out.println(LinearProgramming.toStringsimplexXBnB(betterClub, true,
        // 2));

        System.out.println(fsd.toStringLeaderboards(7, true));
        // CSVWriter lineupWriter = new
        // CSVWriter("OptimalDMSAlgo/data/teams/firstChoice" + 5 + ".csv");
        // lineupWriter.writeLineUp(LocalSearch.firstChoiceHillClimbingWithSwaps(teamState,
        // 4));

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

        // TeamState best = LocalSearch.SimulatedAnnealing(teamState, new
        // ExponentialSchedule(10, 0.99, 0.0001));
        // System.out.println(best.toStringLineUp());
        // ExperimentLocalSearch.compareStartingStatesCompleatly(fsdFemale);
        // ExperimentLocalSearch.compareStartingStates(betterState);
        // ExperimentLocalSearch.compareDataSets(betterState, svmState, fsdFemale,
        // fsdMale);
        // ExperimentLocalSearch.compareAllTimes(teamState);
        // ExperimentLocalSearch.hillClimbingOnTheFly(betterState);
        // ExperimentLocalSearch.beamSearch(teamState, 20);
        // ExperimentLocalSearch.kRestartsFirstChoiceHillClimbing(100, teamState);
        // ExperimentLocalSearch.firstChoiceHillClimbing(teamState);
        ExperimentLocalSearch.kRestartsHillClimbinFast(1_000_0, betterState);
        // ExperimentLocalSearch.simulatedAnnealingShavedN(teamState, 10);
        // ExperimentLocalSearch.simulatedAnnealing(teamState);
        // ExperimentLocalSearch.kSideStepsHillClimbing(2, teamState);
        // ExperimentLocalSearch.kSideStepsHillClimbing(20, teamState);
        // ExperimentLocalSearch.kRestartsHillClimbing(50, teamState);
        // ExperimentLocalSearch.kRestartsHillClimbing(5, teamState);
        // ExperimentLocalSearch.firstChoiceHillClimbingKSwapsIterationsTeamName(betterState,
        // 2, "Test");

        // ExperimentLocalSearch.kRestartsHillClimbinFast(100000, teamState);
        // ExperimentLocalSearch.beamSearchFast(teamState, 20);
        // ExperimentLocalSearch.standardHillClimbing(betterState);
        // ExperimentLocalSearch.standardHillClimbingFastIterations(teamState);
        // ExperimentLocalSearch.firstChoiceHillClimbingWithKSwaps(betterState, 3);

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
