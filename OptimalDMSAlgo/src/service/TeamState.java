package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import objects.Competition;
import objects.Swimmer;
import objects.SwimmingClub;
import objects.SwimmingEvent;

public class TeamState {

    private int totalPoints = 0; // total points of the current team state
    private List<Swimmer> availableSwimmers; // list of swimmers that are currently available

    // Maybe little bit complicated to have teamSwimmers and lineUp
    // teamSwimmer is mainly for calculating the total points of the team
    private List<Swimmer> teamSwimmers; // list of swimmers that are currently in the team

    private Map<Integer, Swimmer> lineUp; // maps each Competition eventIndex to the swimmer that is currently assigned

    private Map<SwimmingEvent, List<Swimmer>> leaderboards; // leaderboard for every event

    public TeamState(SwimmingClub club, boolean isMale) {
        this.availableSwimmers = club.getAllSwimmer().stream().filter(swimmer -> swimmer.isMale() == isMale).toList();
        this.leaderboards = club.getLeaderboards(isMale); // get the appropriate leaderboards from the club
        this.lineUp = generateRandomLineUp();
        this.totalPoints = getTotalPoints();

    }

    public TeamState(TeamState other) {

        this.totalPoints = other.totalPoints;

        // Map original -> copy
        Map<Swimmer, Swimmer> copies = new HashMap<>();

        // helper function (inline)
        Function<Swimmer, Swimmer> copySwimmer = s -> {
            return copies.computeIfAbsent(s, k -> new Swimmer(k));
        };

        // availableSwimmers
        this.availableSwimmers = new ArrayList<>();
        for (Swimmer s : other.availableSwimmers) {
            this.availableSwimmers.add(copySwimmer.apply(s));
        }

        // teamSwimmers
        this.teamSwimmers = new ArrayList<>();
        for (Swimmer s : other.teamSwimmers) {
            this.teamSwimmers.add(copySwimmer.apply(s));
        }

        // lineUp
        this.lineUp = new HashMap<>();
        for (Map.Entry<Integer, Swimmer> e : other.lineUp.entrySet()) {
            if (e.getKey().intValue() == -1) {
                continue; // breaks dont have to be copied
            }
            this.lineUp.put(e.getKey(), copySwimmer.apply(e.getValue()));
        }

        // leaderboards
        this.leaderboards = new HashMap<>();
        for (Map.Entry<SwimmingEvent, List<Swimmer>> e : other.leaderboards.entrySet()) {

            List<Swimmer> listCopy = new ArrayList<>();
            for (Swimmer s : e.getValue()) {
                listCopy.add(copySwimmer.apply(s));
            }

            this.leaderboards.put(e.getKey(), listCopy);
        }
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
        int total = 0;
        for (Swimmer swimmer : lineUp.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList()) {
            total += swimmer.getTotalPoints(); // adds the total points of each swimmer to the total
        }
        return total;
    }

    public List<TeamState> createAllNeighbors() {
        List<TeamState> neighbors = new ArrayList<TeamState>();
        for (int i = 0; i < Competition.order.length; i++) {
            if (Competition.order[i][0] == -1) {
                continue; // No neighbor for breaks needed
            }
            neighbors.addAll(createNeighbors(i));
        }
        return neighbors;
    }

    public List<TeamState> createNeighbors(int orderIndex) {
        List<TeamState> neighbors = new ArrayList<>();

        for (Swimmer swimmer : availableSwimmers) {
            if (swimmer.canChooseEvent(SwimmingEvent.values()[Competition.order[orderIndex][0]])) {
                TeamState neighbor = new TeamState(this); // WARNING: FLAT COPY need to be fixed
                neighbor.swapAthletes(orderIndex, swimmer.getID()); // WARNING: SWIMMER DOESN'T KNOW HE CHOOSE THIS
                                                                    // EVENT
                neighbor.totalPoints = neighbor.getTotalPoints();
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    // MAYBE A little uneffective O(n) because we have to search through all
    // athletes
    // O(1) could be reachable
    public void swapAthletes(int orderIndex, int athleteID) {
        Swimmer original = lineUp.get(Integer.valueOf(orderIndex));
        original.removeEvent(orderIndex);
        Swimmer athlete = null;
        for (Swimmer s : this.availableSwimmers) {
            if (s.getID() == athleteID) {
                athlete = s;
            }
        }
        if (athlete != null) {
            athlete.chooseEvent(orderIndex);
        }

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

    public String toStringTeamSwimmers() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current Swimmers in Team\n");
        for (Swimmer s : lineUp.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList()) {
            sb.append(s.getName() + " " + s.getTotalPoints());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static TeamState getBestState(List<TeamState> states) {
        TeamState bestState = states.getFirst();
        for (TeamState s : states) {
            if (s.getTotalPoints() > bestState.getTotalPoints()) {
                bestState = s;
            }
        }
        return bestState;
    }

}
