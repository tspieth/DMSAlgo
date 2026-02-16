package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import objects.Competition;
import objects.Swimmer;
import objects.SwimmingClub;
import objects.SwimmingEvent;

public class TeamState {

    private int totalPoints; // total points of the current team state
    private List<Swimmer> availableSwimmers; // list of swimmers that are currently available
    private List<Swimmer> teamSwimmers; // list of swimmers that are currently in the team

    private Map<Integer, Swimmer> lineUp; // maps each Competition eventIndex to the swimmer that is currently assigned

    private Map<SwimmingEvent, List<Swimmer>> leaderboards; // leaderboard for every event

    public TeamState(SwimmingClub club, boolean isMale) {
        this.availableSwimmers = club.getAllSwimmer().stream().filter(swimmer -> swimmer.isMale() == isMale).toList();
        this.leaderboards = club.getLeaderboards(isMale); // get the appropriate leaderboards from the club
        this.lineUp = generateRandomLineUp();
        this.totalPoints = getTotalPoints();

    }

    private Map<Integer, Swimmer> generateRandomLineUp() {
        Map<Integer, Swimmer> lineUp = new HashMap<>();

        for (int i = 0; i < Competition.order.length; i++) {
            Swimmer randomSwimmer = getRandomSwimmerForCompetition(i);
            lineUp.put(i, randomSwimmer);
        }
        this.teamSwimmers = lineUp.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return lineUp;
    }

    public Swimmer getRandomSwimmerForEvent(SwimmingEvent event, int orderIndex) {
        List<Swimmer> valid = leaderboards.get(event).stream()
                .filter(s -> s.canChooseEvent(event))
                .toList();

        if (valid.isEmpty())
            return null;

        int randomIndex = (int) (Math.random() * valid.size());
        Swimmer randomSwimmer = valid.get(randomIndex);
        randomSwimmer.chooseEvent(orderIndex);
        return randomSwimmer;
    }

    public Swimmer getRandomSwimmerForCompetition(int orderIndex) {

        int eventIndex = Competition.order[orderIndex][0];

        if (eventIndex == -1) {
            return null; // if there is a break, return null
        }

        return getRandomSwimmerForEvent(SwimmingEvent.values()[eventIndex], orderIndex);
    }

    public int getTotalPoints() {
        int totalPoints = 0;
        for (Swimmer swimmer : teamSwimmers) {
            totalPoints += swimmer.getTotalPoints(); // adds the total points of each swimmer to the total
        }
        return totalPoints;
    }

    public String toStringLineUp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current Lineup:\n");
        int j = 1;
        for (int i = 0; i < Competition.order.length; i++) {
            int eventIndex = Competition.order[i][0];
            if (eventIndex == -1) {
                sb.append(String.format("------ BREAK (approx. %d min) ------ %n", Competition.order[i][1]));
                continue;
            }
            Swimmer swimmer = lineUp.get(i);
            String swimmerName = swimmer != null ? swimmer.getName() : "No swimmer assigned";
            String gender = swimmer != null ? (swimmer.isMale() ? " (m)" : " (f)") : "";
            sb.append(String.format("%02d %5s: %s%s%n", j,
                    SwimmingEvent.values()[eventIndex].getDisplayName(), swimmerName, gender));
            j++;
        }
        sb.append("Total Points: ").append(getTotalPoints()).append("\n");
        return sb.toString();
    }

}
