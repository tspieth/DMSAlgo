package localsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.Function;

import experiments.ExperimentLocalSearch;
import objects.Competition;
import objects.Swimmer;
import objects.SwimmingClub;
import objects.SwimmingEvent;

public class TeamState {

    private boolean isMale;
    private int totalPoints = 0; // total points of the current team state
    private int[][] order; // safes Copy for GenderSpecific order from Competition

    // Probably only lineUp and availableSwimmers is really needed
    // Eventually relevant for efficiency

    private List<Swimmer> availableSwimmers; // list of swimmers that are currently available

    private Map<Integer, Swimmer> lineUp; // maps each Competition eventIndex to the swimmer that is currently assigned

    private Map<SwimmingEvent, List<Swimmer>> leaderboards; // leaderboard for every event

    // =============================================================
    // Konstruktoren
    // =============================================================

    /**
     * Generiert einen TeamState für das gegebene Geschlecht aus einem gegebenen
     * Club
     * 
     * Die Listen und Maps des erzeugten States enthalten nur Kopien aus den
     * Athleten des Clubs
     * 
     * @param club
     * @param isMale
     */
    public TeamState(SwimmingClub club, boolean isMale) {

        this.isMale = isMale;
        this.order = isMale ? Competition.orderMale : Competition.orderFemale;

        // **************************
        // Logik for Copies
        // **************************

        // Map original -> copy
        Map<Swimmer, Swimmer> copies = new HashMap<>();

        // Helper function (inline)
        Function<Swimmer, Swimmer> copySwimmer = s -> {
            return copies.computeIfAbsent(s, k -> new Swimmer(k));
        };

        // **************************
        // Getting Lists to Copy from
        // **************************

        List<Swimmer> tempAvailable = club.getAllSwimmer().stream().filter(swimmer -> swimmer.isMale() == isMale)
                .toList();
        Map<SwimmingEvent, List<Swimmer>> tempLead = club.getLeaderboards(isMale);

        // **************************
        // Filling Structures
        // **************************

        this.availableSwimmers = new ArrayList<>();
        for (Swimmer s : tempAvailable) {
            this.availableSwimmers.add(copySwimmer.apply(s));
        }

        this.leaderboards = new HashMap<>();
        for (Map.Entry<SwimmingEvent, List<Swimmer>> e : tempLead.entrySet()) {

            List<Swimmer> listCopy = new ArrayList<>();
            for (Swimmer s : e.getValue()) {
                listCopy.add(copySwimmer.apply(s));
            }

            this.leaderboards.put(e.getKey(), listCopy);
        }

        // **************************
        // Generating Random LineUp
        // **************************

        if (!newRandomLineUp()) {
            System.out.println("ERROR RANDOM LINUP NOT FOUND IN 1000 TRYS");
        }
    }

    /**
     * Erzeugt eine Tiefere Kopie des uebergebenen TeamStates.
     * 
     * Kann zum Generieren von Nachbarn verwendet werden.
     * 
     * @param other
     */
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

    // =============================================================
    // RandomLinupGenerator
    // =============================================================

    // Methode for Random LocalBeamSearch
    public List<TeamState> createRandomStates(int k) {
        List<TeamState> randomStates = new ArrayList<>();
        for (int i = 1; i <= k; i++) {
            TeamState newRand = new TeamState(this);
            newRand.newRandomLineUp();
            randomStates.add(newRand);
        }
        return randomStates;
    }

    /**
     * Generiert ein Zufälliges LineUp und ordnet es this.lineUp zu
     * 
     * Abbruchbedingung kann verbessert werden
     * 
     * @return true, wenn ein LinUp innerhalb 1000 Trys gefunden wurde, false sonst
     */
    public boolean newRandomLineUp() {
        int maxAttempts = 1000;
        int attempts = 0;

        this.lineUp = null;
        while (this.lineUp == null && attempts < maxAttempts) {
            this.resetTeamState();
            this.lineUp = this.generateRandomLineUp();
            attempts++;
        }

        if (this.lineUp == null) {
            System.out.println("Failed to generate a valid lineup after " + maxAttempts + " attempts.");
            return false;
        }
        return true;
    }

    // Helper Method for newRandomLineUp()
    private Map<Integer, Swimmer> generateRandomLineUp() {
        Map<Integer, Swimmer> lineUp = new HashMap<>();
        this.totalPoints = 0; // Reset points for new lineup

        for (int i = 0; i < this.order.length; i++) {
            Swimmer randomSwimmer = getRandomSwimmerForCompetition(i);

            if ((randomSwimmer == null && order[i][0] != -1) &&
                    order[i][0] != -2) {
                // System.out.println("Es konnte kein Linup gebildet werden.");
                return null;
            }
            if (randomSwimmer != null) {
                totalPoints += randomSwimmer.getPointsForOrderIndex(i);
            }
            lineUp.put(i, randomSwimmer);
        }
        return lineUp;
    }

    // Gets RandomSwimmer for orderIndex if exists, else null is returned
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

    // Helper Method for getRandomSwimmerForEvent(int orderIndex)
    private Swimmer getRandomSwimmerForEvent(SwimmingEvent event, int orderIndex) {
        List<Swimmer> valid = leaderboards.get(event).stream()
                .filter(s -> s.canChooseOrderIndex(orderIndex))
                .toList();

        if (valid.isEmpty())
            return null;

        int randomIndex = ExperimentLocalSearch.rng.nextInt(valid.size());
        Swimmer randomSwimmer = valid.get(randomIndex);
        randomSwimmer.chooseEvent(orderIndex);
        return randomSwimmer;
    }

    // =============================================================
    // Neighbor Creator Methodes
    // =============================================================

    // Create Top K Neighbors
    public List<TeamState> createTopKNeighbors(int k) {
        PriorityQueue<TeamState> maxHeap = new PriorityQueue<>(
                (a, b) -> Integer.compare(b.getTotalPointsFast(), a.getTotalPointsFast()));
        for (int i = 0; i < this.order.length; i++) {
            if (this.order[i][0] == -1 || this.order[i][0] == -2) {
                continue; // No neighbor for breaks needed

            }
            maxHeap.addAll(createNeighborsForIndex(i));
        }

        List<TeamState> topKNeighors = new ArrayList<>();
        for (int i = 0; i < k / 2 && !maxHeap.isEmpty(); i++) {
            topKNeighors.add(maxHeap.poll()); // bestes Element holen
        }
        return topKNeighors;

    }

    // Creates All Neighbors
    public List<TeamState> createAllNeighbors() {
        List<TeamState> neighbors = new ArrayList<TeamState>();
        for (int i = 0; i < this.order.length; i++) {
            if (this.order[i][0] == -1 || this.order[i][0] == -2) {
                continue; // No neighbor for breaks needed
            }
            neighbors.addAll(createNeighborsForIndex(i));
        }
        return neighbors;
    }

    // creates All Neighbors of a specific order Index
    // eventIndex should be an available EVENT in class SwimmingEvent
    public List<TeamState> createNeighborsForIndex(int orderIndex) {
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

    public TeamState createRandomNeighbor(int orderIndex, int swimmerIndex) {
        TeamState neighbor = null;

        Swimmer swimmer = availableSwimmers.get(swimmerIndex);

        if (swimmer.canChooseOrderIndex(orderIndex)) {
            neighbor = new TeamState(this);
            neighbor.swapAthletes(orderIndex, swimmer.getID());
        }

        return neighbor;
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

    // =============================================================
    // Getter Methodes
    // =============================================================

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

    public static TeamState getBestState(List<TeamState> states) {
        TeamState bestState = states.getFirst();
        for (TeamState s : states) {
            if (s.getTotalPointsFast() > bestState.getTotalPointsFast()) {
                bestState = s;
            }
        }
        return bestState;
    }

    public TeamState getRandomNeighbor() {
        List<TeamState> allNeighbors = this.createAllNeighbors();
        int randomIndex = ExperimentLocalSearch.rng.nextInt(allNeighbors.size());
        TeamState randomNeighbor = allNeighbors.get(randomIndex);
        return randomNeighbor;
    }

    public TeamState getRandomNeighborFromTopN(int n) {
        List<TeamState> allNeighbors = this.createAllNeighbors();
        Collections.sort(allNeighbors, (a, b) -> Double.compare(b.getTotalPointsFast(), a.getTotalPointsFast()));

        List<TeamState> topNeighbors = allNeighbors.subList(0, Math.min(n, allNeighbors.size()));
        int randomIndex = ExperimentLocalSearch.rng.nextInt(topNeighbors.size());
        TeamState randomNeighbor = topNeighbors.get(randomIndex);
        return randomNeighbor;
    }

    public TeamState getFirstBetterRandomNeighbor() {

        int orderCount = this.order.length;
        int swimmerCount = availableSwimmers.size();
        List<int[]> moves = new ArrayList<>();

        for (int o = 0; o < orderCount; o++) {
            if (this.order[o][0] < 0) {
                continue; // This event is eather a break or not for the gender;
            }
            for (int s = 0; s < swimmerCount; s++) {
                moves.add(new int[] { o, s });
            }
        }

        Collections.shuffle(moves, ExperimentLocalSearch.rng);

        for (int[] m : moves) {
            TeamState neighbor = createRandomNeighbor(m[0], m[1]);

            // greedy auswertug verhindert hier nullPointer exceptions
            if (neighbor != null && neighbor.getTotalPointsFast() > this.totalPoints) {
                return neighbor;
            }
        }

        return null;
    }

    // =============================================================
    // Setter Methodes
    // =============================================================

    public void resetTeamState() {
        this.totalPoints = 0;
        for (Swimmer swimmer : this.availableSwimmers) {
            swimmer.resetEvents();
        }
    }

    // =============================================================
    // toString() Methodes
    // =============================================================

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

    // =============================================================
    // AI GENERATED COMPLETLY BY COPILOT
    // =============================================================

    public TeamState getFirstBetterRandomNeighborkSwaps(int k) {
        if (k <= 0) {
            return null;
        }

        int orderCount = this.order.length;
        int swimmerCount = availableSwimmers.size();
        List<int[]> moves = new ArrayList<>();

        for (int o = 0; o < orderCount; o++) {
            if (this.order[o][0] < 0) {
                continue; // This event is either a break or not for the gender;
            }
            for (int s = 0; s < swimmerCount; s++) {
                moves.add(new int[] { o, s });
            }
        }

        Collections.shuffle(moves, ExperimentLocalSearch.rng);

        // Use recursive search up to depth k. The helper will try moves in the
        // shuffled order and return the first found improving neighbor.
        return findFirstBetterByDepth(this, moves, k, this.totalPoints);
    }

    // recursive helper: current = current state to expand, remainingSwaps = how
    // many
    // more swaps are allowed, baseline = original points to compare improvements
    // against
    private TeamState findFirstBetterByDepth(TeamState current, List<int[]> moves, int remainingSwaps,
            int baseline) {
        if (remainingSwaps <= 0) {
            return null;
        }

        for (int[] m : moves) {
            TeamState next = current.createRandomNeighbor(m[0], m[1]);
            if (next == null) {
                continue;
            }
            if (next.getTotalPointsFast() > baseline) {
                return next;
            }
            TeamState deeper = findFirstBetterByDepth(next, moves, remainingSwaps - 1, baseline);
            if (deeper != null) {
                return deeper;
            }
        }
        return null;
    }

}
