package branchNbound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import objects.Competition;
import objects.Swimmer;
import objects.SwimmingClub;
import objects.SwimmingEvent;

/**
 * First VERSION is extremly unoptimized
 * May decide if it would be better to just safe wich athlete is new assigned
 * 
 */
public class TeamNode {

    private boolean isMale;
    private int totalPoints = 0; // total points of the current team state
    private int[][] order; // safes Copy for GenderSpecific order from Competition

    // Probably only lineUp and availableSwimmers is really needed
    // Eventually relevant for efficiency

    private List<Swimmer> availableSwimmers; // list of swimmers that are currently available

    private Map<Integer, Swimmer> lineUp; // maps each Competition eventIndex to the swimmer that is currently assigned

    private Map<SwimmingEvent, List<Swimmer>> leaderboards; // leaderboard for every event

    // New Attributes specially for BranchNBound
    private int nextSwimmerIndexForPosition = 0; // trackiert welcher Schwimmer für aktuelle Position versucht wird
    public static int lowerBound = 0; // Lower Bound
    // if updated we need to know in every other Node
    // only Upates when this.isFull and lowerBound < this.totalPoints
    private int UpperBound = 0; // upperBound

    private int[][] simpleLeaderbord; // for Calculation of upperBound
    // first Idea:
    // simpleL[i][0] countains best points for event with index i
    // simpleL[i][1] contains second best points for event with index i
    // These are the only needed to calculate an excact upperBound

    // second Idea:
    // simpleL[i][0] countains best Swimmer ID
    // simpleL[i][1] contains second best Swimmer ID
    // I dont know what the inention here is
    // because we need O(2n) to get the points
    // Could allow better upperBound

    // =============================================================
    // Konstruktoren
    // =============================================================

    /**
     * Generiert einen TeamNode für das gegebene Geschlecht aus einem gegebenen
     * Club
     * 
     * Die Listen und Maps des erzeugten States enthalten nur Kopien aus den
     * Athleten des Clubs
     * 
     * @param club
     * @param isMale
     */
    public TeamNode(SwimmingClub club, boolean isMale) {

        this.isMale = isMale;
        this.order = isMale ? Competition.orderMale : Competition.orderFemale;
        // initialize lineUp with all order indices mapped to null
        this.lineUp = new HashMap<>();
        for (int i = 0; i < this.order.length; i++) {
            this.lineUp.put(i, null);
        }

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

        createSimpleLead();
        setUpperBound();
    }

    /**
     * Erzeugt eine Tiefere Kopie des uebergebenen TeamNodes.
     * 
     * Uebernommen aus TeamState sollte so angepasst werden,
     * dass nur Schwimmer die geaendert wurden tief kopiert werden
     * 
     * @param other
     */
    public TeamNode(TeamNode other) {

        this.totalPoints = other.totalPoints;
        this.isMale = other.isMale;
        this.order = other.order;
        this.simpleLeaderbord = other.simpleLeaderbord;
        this.nextSwimmerIndexForPosition = 0; // Zurücksetzen für nächste Position

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

        // lineUp: preserve same orderIndex and copy non-null swimmers
        this.lineUp = new HashMap<>();
        for (int i = 0; i < other.order.length; i++) {
            this.lineUp.put(i, null);
        }
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
    // Setter
    // =============================================================

    // Maybe needs Check so it works reliable Lists in the Map have to be sorted
    public void createSimpleLead() {

        int[][] simpleLead = new int[leaderboards.entrySet().size()][2];

        for (Map.Entry<SwimmingEvent, List<Swimmer>> e : leaderboards.entrySet()) {

            int eventId = e.getKey().getIndex();

            // Maybe check is needed that e.getValue() is already sorted so best comes first
            Swimmer best = e.getValue().get(0);
            Swimmer second = e.getValue().get(1);

            simpleLead[eventId][0] = (best != null) ? best.getPointsForEventIndex(eventId) : 0;
            simpleLead[eventId][1] = (second != null) ? second.getPointsForEventIndex(eventId) : 0;
        }

        this.simpleLeaderbord = simpleLead;
    }

    /**
     * First Version suposes Team States are calculated by going throug Linup
     * by filling the Linup Linear
     * 
     * 
     * SHOULD BE OPTIMICED
     * Could be Simplyfied by using totalPoints
     * Result would be FASTER because we dont have to call methodes from Swimmers
     * 
     * 
     */
    public void setUpperBound() {

        // Indicates if the first entry is already in team
        boolean[] firstIsTaken = new boolean[simpleLeaderbord.length];
        Arrays.fill(firstIsTaken, false);

        int upper = 0;

        for (int i = 0; i < this.order.length; i++) {

            int eventIndex = order[i][0];

            if (eventIndex < 0) {
                continue; // break or other gender
            }

            Swimmer eventSwimmer = lineUp.get(i);
            if (eventSwimmer != null) {

                int eventPoints = eventSwimmer.getPointsForOrderIndex(i);
                upper += eventPoints;

                if (eventPoints >= simpleLeaderbord[eventIndex][0]) {
                    firstIsTaken[eventIndex] = true;
                }
            } else {
                if (firstIsTaken[eventIndex]) {
                    upper += simpleLeaderbord[eventIndex][1];
                } else {
                    upper += simpleLeaderbord[eventIndex][0];
                    firstIsTaken[eventIndex] = true;
                    /*
                     * System.out.println(
                     * "First Event for: " + SwimmingEvent.values()[eventIndex].getDisplayName() +
                     * " was taken.");
                     */
                }
            }
        }
        this.UpperBound = upper;
    }

    // =============================================================
    // DFS Logic to Create Childs
    // =============================================================

    public TeamNode nextChildNode() {
        TeamNode nextChild = new TeamNode(this);

        int toChoose = getNextLineUpSpot();

        // There is no free Spot
        if (toChoose < 0) {
            return null;
        }

        int eventIndex = Competition.getEventIndexByOrderIndex(toChoose);
        SwimmingEvent event = SwimmingEvent.values()[eventIndex];

        // Nutze nextSwimmerIndexForPosition um verschiedene Schwimmer zu versuchen
        if (nextSwimmerIndexForPosition >= nextChild.leaderboards.get(event).size()) {
            // Haben alle Schwimmer versucht, zurücksetzen für nächste Position
            this.nextSwimmerIndexForPosition = 0;
            return null; // No more swimmers for this position
        }

        Swimmer nextBest = nextChild.leaderboards.get(event).get(nextSwimmerIndexForPosition);

        if (nextBest != null && nextBest.canChooseOrderIndex(toChoose)) {
            // Update total points und Schwimmer im CHILD Knoten
            nextBest.chooseEvent(toChoose);
            int pointsForEvent = nextBest.getPointsForOrderIndex(toChoose);
            nextChild.totalPoints += pointsForEvent;

            // Put nextBestSwimmer in the lineUp der CHILD
            nextChild.lineUp.put(toChoose, nextBest);

            nextChild.setUpperBound();

            // Inkrementiere für nächsten Aufruf
            this.nextSwimmerIndexForPosition++;

            return nextChild;
        } else {
            // Dieser Schwimmer kann diese Position nicht schwimmen, versuch nächsten
            this.nextSwimmerIndexForPosition++;
            return this.nextChildNode(); // Rekursiv nächsten versuchen
        }
    }

    // Can be pruned if the UpperBound is lower than lowerBound
    public boolean canBePruned() {
        return this.UpperBound < TeamNode.lowerBound;
    }

    // Has to be Implemented or we can just use nextLineUp Spot
    public boolean isCompleteSolution() {
        return false;
    }

    // =============================================================
    // Getter
    // =============================================================

    public int getUpperBound() {
        return this.UpperBound;
    }

    public boolean isMale() {
        return this.isMale;
    }

    public int getNextLineUpSpot() {

        for (int i = 0; i < this.order.length; i++) {

            int eventIndex = order[i][0];

            // We dont have to test Breaks
            if (eventIndex < 0) {
                continue; // break or other gender
            }

            // Returns event Swimmer for current OrderIndex
            Swimmer eventSwimmer = lineUp.get(i);

            // if this swimmer is Null return current OrderIndex
            if (eventSwimmer == null) {
                return i;
            }
        }
        // if every LineUpSpot is taken Mark it with returning -1
        return -1;
    }

    public int getTotalPoints() {
        return this.totalPoints;
    }

    // =============================================================
    // toString() Methodes
    // =============================================================

    public String toStringSimpleLeaderBoard() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < simpleLeaderbord.length; i++) {
            SwimmingEvent event = SwimmingEvent.values()[i];
            sb.append("Leaderboard for ").append(event.getDisplayName()).append(isMale ? " (m)" : " (f)").append(":\n");
            sb.append("Best Points: ").append(simpleLeaderbord[i][0]).append("\n");
            sb.append("Second Best Points: ").append(simpleLeaderbord[i][1]).append("\n");
            sb.append("**********************************\n");
        }
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
            int pointsForEvent = swimmer != null ? swimmer.getPointsForEvent(SwimmingEvent.values()[eventIndex]) : 0;
            String breakTime = swimmer != null ? swimmer.toStringBreakBefore(i) : "0";
            sb.append(String.format("%02d %5s: %-19s%s %04d %s%n", j,
                    SwimmingEvent.values()[eventIndex].getDisplayName(), swimmerName, gender, pointsForEvent,
                    breakTime));
            j++;
        }
        sb.append("Total Points: ").append(getTotalPoints()).append("\n");
        return sb.toString();
    }

}