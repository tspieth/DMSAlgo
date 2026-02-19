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

    private boolean isMale;
    private int totalPoints = 0; // total points of the current team state
    private List<Swimmer> availableSwimmers; // list of swimmers that are currently available
    private int[][] order; // selects Copy for GenderSpecific order from Competition

    // Maybe little bit complicated to have teamSwimmers and lineUp
    // teamSwimmer is mainly for calculating the total points of the team
    // deprecated!!!!!!!!!!!!!!!!!
    // private List<Swimmer> teamSwimmers; // list of swimmers that are currently in
    // the team

    private Map<Integer, Swimmer> lineUp; // maps each Competition eventIndex to the swimmer that is currently assigned

    private Map<SwimmingEvent, List<Swimmer>> leaderboards; // leaderboard for every event

    public TeamState(SwimmingClub club, boolean isMale) {
        this.availableSwimmers = club.getAllSwimmer().stream().filter(swimmer -> swimmer.isMale() == isMale).toList();
        this.leaderboards = club.getLeaderboards(isMale); // get the appropriate leaderboards from the club
        this.isMale = isMale;
        this.order = isMale ? Competition.orderMale : Competition.orderFemale;

        this.lineUp = generateRandomLineUp();

        // this.totalPoints = getTotalPoints();
    }

    public TeamState(TeamState other) {

        this.totalPoints = other.totalPoints;
        this.isMale = other.isMale;
        this.order = other.order;

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

        // lineUp
        this.lineUp = new HashMap<>();
        for (Map.Entry<Integer, Swimmer> e : other.lineUp.entrySet()) {
            if (e.getValue() == null) {
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

    // MUST ADD FUNCTION THAT ABBORTS TEAM STATE IF THERE IS NO POSSIBLE RANDOM
    // LINEUP
    // MUST ENSURE THAT IF THERE IS MIN 1 RANDOM LINUP THAT THIS LINEUP IS FOUND
    private Map<Integer, Swimmer> generateRandomLineUp() {
        Map<Integer, Swimmer> lineUp = new HashMap<>();

        for (int i = 0; i < this.order.length; i++) {
            Swimmer randomSwimmer = getRandomSwimmerForCompetition(i);

            if ((randomSwimmer == null && order[i][0] != -1) &&
                    order[i][0] != -2) {
                System.out.println("Es konnte kein Linup gebildet werden.");
                return null;
            }
            if (randomSwimmer != null) {
                totalPoints += randomSwimmer.getPointsForOrderIndex(i);
            }
            lineUp.put(i, randomSwimmer);
        }
        return lineUp;
    }

    public void newRandomLineUp() {
        this.lineUp = this.generateRandomLineUp();
    }

    // helper Method for getRandomSwimmerForEvent(int orderIndex)
    private Swimmer getRandomSwimmerForEvent(SwimmingEvent event, int orderIndex) {
        List<Swimmer> valid = leaderboards.get(event).stream()
                .filter(s -> s.canChooseOrderIndex(orderIndex))
                .toList();

        if (valid.isEmpty())
            return null;

        int randomIndex = (int) (Math.random() * valid.size());
        Swimmer randomSwimmer = valid.get(randomIndex);
        randomSwimmer.chooseEvent(orderIndex);
        return randomSwimmer;
    }

    public Swimmer getRandomSwimmerForCompetition(int orderIndex) {

        int eventIndex = this.order[orderIndex][0];

        if (eventIndex == -1) {
            return null; // if there is a break, return null
        }
        if (eventIndex == -2) {
            return null; // if the event is for the other gender
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

    public int getTotalPointsFast() {
        return this.totalPoints;
    }

    public List<TeamState> createAllNeighbors() {
        List<TeamState> neighbors = new ArrayList<TeamState>();
        for (int i = 0; i < this.order.length; i++) {
            if (this.order[i][0] == -1 || this.order[i][0] == -2) {
                continue; // No neighbor for breaks needed
            }
            neighbors.addAll(createNeighbors(i));
        }
        return neighbors;
    }

    // eventIndex should be a available EVENT in class SwimmingEvent
    public List<TeamState> createNeighbors(int orderIndex) {
        List<TeamState> neighbors = new ArrayList<>();

        for (Swimmer swimmer : availableSwimmers) {
            if (swimmer.canChooseOrderIndex(orderIndex)) {
                TeamState neighbor = new TeamState(this);
                neighbor.swapAthletes(orderIndex, swimmer.getID());
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    // MAYBE A little uneffective O(n) because we have to search through all
    // athletes
    // O(1) could be reachable
    public void swapAthletes(int orderIndex, int athleteID) {
        SwimmingEvent event = SwimmingEvent.values()[Competition.order[orderIndex][0]];
        Swimmer original = lineUp.get(Integer.valueOf(orderIndex));
        original.removeEvent(orderIndex);
        this.totalPoints -= original.getPointsForEvent(event);

        Swimmer athlete = null;
        for (Swimmer s : this.availableSwimmers) {
            if (s.getID() == athleteID) {
                athlete = s;
            }
        }
        if (athlete != null) {
            lineUp.put(orderIndex, athlete);
            athlete.chooseEvent(orderIndex);
            this.totalPoints += athlete.getPointsForEvent(event);
        }

    }

    public String toStringLineUp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current Lineup:\n");
        int j = 1;
        for (int i = 0; i < this.order.length; i++) {
            int eventIndex = this.order[i][0];
            if (eventIndex == -1) {
                sb.append(String.format("------ BREAK (approx. %d min) ------ %n", this.order[i][1]));
                continue;
            }
            if (eventIndex == -2) {
                // sb.append("------ " + (isMale ? "Female" : "Male"));
                // sb.append(String.format(" Event (approx. %d min ------ %n",
                // this.order[i][1]));
                continue;
            }
            Swimmer swimmer = lineUp.get(i);
            String swimmerName = swimmer != null ? swimmer.getName() : "No swimmer assigned";
            String gender = swimmer != null ? (swimmer.isMale() ? " (m)" : " (f)") : "";
            int pointsForEvent = swimmer.getPointsForEvent(SwimmingEvent.values()[eventIndex]);
            String breakTime = swimmer.toStringBreakBefore(i);
            sb.append(String.format("%02d %5s: %-19s%s %04d %s%n", j,
                    SwimmingEvent.values()[eventIndex].getDisplayName(), swimmerName, gender, pointsForEvent,
                    breakTime));
            j++;
        }
        sb.append("Total Points: ").append(getTotalPointsFast()).append("\n");
        return sb.toString();
    }

    public String toStringOnly(int ind) {
        StringBuilder sb = new StringBuilder();
        // sb.append("Current Lineup:\n");
        int j = 1;
        for (int i = 0; i < this.order.length; i++) {
            if (i == ind - 2 || i == ind || i == ind - 4 || i == ind + 2) {
                int eventIndex = this.order[i][0];
                if (eventIndex == -1) {
                    sb.append(String.format("------ BREAK (approx. %d min) ------ %n", this.order[i][1]));
                    continue;
                }
                if (eventIndex == -2) {
                    // sb.append("------ " + (isMale ? "Female" : "Male"));
                    // sb.append(String.format(" Event (approx. %d min ------ %n",
                    // this.order[i][1]));
                    continue;
                }
                Swimmer swimmer = lineUp.get(i);
                String swimmerName = swimmer != null ? swimmer.getName() : "No swimmer assigned";
                String gender = swimmer != null ? (swimmer.isMale() ? " (m)" : " (f)") : "";
                int pointsForEvent = swimmer.getPointsForEvent(SwimmingEvent.values()[eventIndex]);
                String breakTime = swimmer.toStringBreakBefore(i);
                sb.append(String.format("%02d %5s: %-19s%s %04d %s%n", j,
                        SwimmingEvent.values()[eventIndex].getDisplayName(), swimmerName, gender, pointsForEvent,
                        breakTime));
                j++;
            }
        }
        // sb.append("Total Points: ").append(getTotalPoints()).append("\n");
        return sb.toString();

    }

    public String toStringTeamSwimmers() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current Swimmers in Team\n");
        for (Swimmer s : lineUp.values().stream()
                .filter(Objects::nonNull).sorted((a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()))
                .distinct()
                .toList()) {
            sb.append(String.format("%-19s %04d%n", s.getName(), s.getTotalPoints()));

        }
        return sb.toString();
    }

    public static TeamState getBestState(List<TeamState> states) {
        TeamState bestState = states.getFirst();
        for (TeamState s : states) {
            if (s.getTotalPointsFast() > bestState.getTotalPointsFast()) {
                bestState = s;
            }
        }
        return bestState;
    }

}
