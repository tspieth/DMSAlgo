
import java.util.ArrayList;
import java.util.List;

import io.CSVReader;
import objects.Competition;
import objects.Swimmer;
import objects.SwimmingClub;
import service.LocalSearch;
import service.TeamState;

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
        // System.out.println(Competition.toStringOrder());

        // System.out.println(Competition.toStringBaseTimes(true));
        // System.out.println(Competition.toStringBaseTimes(false));

        List<Swimmer> schwimmerListe = CSVReader.createSwimmer("OptimalDMSAlgo/resources/betterClub.csv");

        SwimmingClub club = new SwimmingClub(schwimmerListe);
        for (Swimmer schwimmer : club.getAllSwimmer()) {
            schwimmer.updatePoints(); // update points for each swimmer based on their times
        }
        club.generateLeaderboards();
        // System.out.println(club.toStringLeaderboards(5, true));
        // System.out.println(club.toStringLeaderboards(5, false));

        TeamState teamState = new TeamState(club, true);

        System.out.println("Punkte vor HillClimb " + teamState.getTotalPoints());
        System.out.println(teamState.toStringTeamSwimmers());
        System.out.println(teamState.toStringLineUp());

        long start = System.nanoTime();

        TeamState best = LocalSearch.hillClimbingWithKStarts(teamState, 2);
        // TeamState best = LocalSearch.hillClimbing(teamState);
        long end = System.nanoTime();

        System.out.println(best.toStringTeamSwimmers());
        System.out.println(best.toStringLineUp());

        double seconds = (end - start) / 1_000_000_000.0;

        System.out.println("Dauer: " + seconds + " s");

    }

}
