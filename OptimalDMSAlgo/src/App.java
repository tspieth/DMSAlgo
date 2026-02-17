
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
        System.out.println(Competition.toStringOrder());

        System.out.println(Competition.toStringBaseTimes(true));
        System.out.println(Competition.toStringBaseTimes(false));

        List<Swimmer> schwimmerListe = CSVReader.createSwimmer("OptimalDMSAlgo/resources/testClub.csv");

        SwimmingClub club = new SwimmingClub(schwimmerListe);
        for (Swimmer schwimmer : club.getAllSwimmer()) {
            schwimmer.updatePoints(); // update points for each swimmer based on their times
        }
        club.generateLeaderboards();
        System.out.println(club.toStringLeaderboards(5, true));
        System.out.println(club.toStringLeaderboards(5, false));

        TeamState teamState = new TeamState(club, true);
        System.out.println("Punkte vor HillClimb " + teamState.getTotalPoints());
        System.out.println("Punkte nach HillClimb " + LocalSearch.hillClimbing(teamState));
    }

}
