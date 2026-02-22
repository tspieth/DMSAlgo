package branchNbound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import objects.Competition;
import objects.Swimmer;
import objects.SwimmingClub;
import objects.SwimmingEvent;

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
    private int nextOrderIndex = 0; // marks next orderIndex has to be choosen
    private int lowerBound = 0; // Lower Bound
    private int UpperBound = 0; // UpperBound

    private int[][] simpleLeaderbord; // for Calculation of UpperBound
    // first Idea:
    // simpleL[i][0] countains best points for event with index i
    // simpleL[i][1] contains second best points for event with index i
    // These are the only needed to calculate an excact UpperBound

    // second Idea:
    // simpleL[i][0] countains best Swimmer ID
    // simpleL[i][1] contains second best Swimmer ID
    // I dont know what the inention here is
    // because we need O(2n) to get the points
    // Could allow better UpperBound

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

    public void setUpperBound() {

    }
}