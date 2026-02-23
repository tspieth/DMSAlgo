package branchNbound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    /**
     * Innere Klasse zur Speicherung eines Event-Ergebnisses
     * mit Schwimmer, Event und geholten Punkten
     */
    private static class EventResult implements Comparable<EventResult> {
        Swimmer swimmer; // Der Schwimmer
        int eventIndex; // Event-Index
        int orderIndex; // Mögliche Position in der Order (-1 wenn multiple möglich)
        int points; // Punkte für diese Zuweisung

        EventResult(Swimmer swimmer, int eventIndex, int points) {
            this.swimmer = swimmer;
            this.eventIndex = eventIndex;
            this.points = points;
            this.orderIndex = -1; // wird später gesetzt wenn nötig
        }

        @Override
        public int compareTo(EventResult other) {
            return Integer.compare(other.points, this.points); // absteigend sortiert (beste zuerst)
        }
    }

    private boolean isMale;
    private int totalPoints = 0; // total points of the current team state
    private int[][] order; // safes Copy for GenderSpecific order from Competition

    // Probably only lineUp and availableSwimmers is really needed
    // Eventually relevant for efficiency

    private List<Swimmer> availableSwimmers; // list of swimmers that are currently available

    private Map<Integer, Swimmer> lineUp; // maps each Competition eventIndex to the swimmer that is currently assigned

    private Map<SwimmingEvent, List<Swimmer>> leaderboards; // leaderboard for every event

    // New Attributes specially for BranchNBound
    private int optimizedEventResultIndex = 0; // Trackt aktuellen Index in sortedEventResults
    public static int lowerBound = 0; // Lower Bound
    // if updated we need to know in every other Node
    // only Upates when this.isFull and lowerBound < this.totalPoints
    private int UpperBound = 0; // upperBound

    private List<EventResult> sortedEventResults; // Alle Event-Ergebnisse global sortiert nach Punkten absteigend

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
        this.sortedEventResults = other.sortedEventResults;
        this.optimizedEventResultIndex = 0; // Zurücksetzen für nächste EventResult

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
        // Sammle ALLE Event-Ergebnisse von ALLEN Schwimmern
        List<EventResult> allResults = new ArrayList<>();

        for (Map.Entry<SwimmingEvent, List<Swimmer>> e : leaderboards.entrySet()) {
            int eventId = e.getKey().getIndex();

            // Für jeden Schwimmer in diesem Event: erstelle ein EventResult
            for (Swimmer swimmer : e.getValue()) {
                int points = swimmer.getPointsForEventIndex(eventId);
                allResults.add(new EventResult(swimmer, eventId, points));
            }
        }

        // Sortiere ALLE Ergebnisse global nach Punkten (absteigend - beste zuerst)
        allResults.sort(null);

        this.sortedEventResults = allResults;
    }

    /**
     * Super schnelle Upper Bound Berechnung mit globaler sortierter Liste.
     * 
     * Durchlaufe die sortierte Liste aller Event-Ergebnisse (beste zuerst).
     * Für jedes Ergebnis: Wenn Position frei und Event noch nicht 2x verwendet,
     * dann addiere Punkte und markiere als verwendet.
     * 
     * Komplexität: O(n) wobei n = Anzahl aller Event-Ergebnisse.
     */
    /**
     * Super schnelle Upper Bound Berechnung mit globaler sortierter Liste.
     * 
     * Durchlaufe die sortierte Liste aller Event-Ergebnisse (beste zuerst).
     * Für jedes beste Ergebnis: Versuche es in eine beliebige freie Position
     * für diesen Event einzuplatzieren (egal welcher orderIndex).
     * 
     * Schwimmer können in mehreren verschiedenen Events antreten!
     */
    public void setUpperBound() {
        int upper = totalPoints;

        // Tracke welche Order-Positionen bereits gefüllt sind
        boolean[] filledPosition = new boolean[order.length];
        for (int i = 0; i < order.length; i++) {
            if (lineUp.get(i) != null) {
                filledPosition[i] = true;
            }
        }

        // Tracke wie oft jedes Event schon verwendet wurde
        int[] eventUsageCount = new int[SwimmingEvent.values().length];
        for (int i = 0; i < order.length; i++) {
            int eventIndex = order[i][0];
            if (eventIndex >= 0 && filledPosition[i]) {
                eventUsageCount[eventIndex]++;
            }
        }

        // Gehe durch sortierte Liste (beste Ergebnisse zuerst)
        for (EventResult result : sortedEventResults) {

            // Prüfe ob dieses Event schon 2x verwendet wurde
            if (eventUsageCount[result.eventIndex] >= 2) {
                continue; // Dieses Event hat keine Plätze mehr
            }

            // Suche IRGENDEINE freie Position für diesen Event
            for (int i = 0; i < order.length; i++) {
                int eventIndex = order[i][0];

                // Ist diese Position frei und für diesen Event?
                if (eventIndex == result.eventIndex && !filledPosition[i]) {
                    // Kann der Schwimmer diese Position schwimmen?
                    if (result.swimmer.canChooseOrderIndex(i)) {
                        // JA → Platziere es!
                        filledPosition[i] = true;
                        eventUsageCount[result.eventIndex]++;
                        upper += result.points;
                        break; // Gehe zum nächsten best EventResult
                    }
                }
            }
        }

        this.UpperBound = upper;
    }

    // =============================================================
    // DFS Logic to Create Childs
    // =============================================================

    /**
     * Erstellt Child-Knoten aus der sortierten EventResults Liste.
     * Nimmt das beste verfügbare EventResult und versucht es zu platzieren.
     * 
     * @return Nächster Child-Knoten oder null wenn keine mehr existieren
     */
    public TeamNode nextChildNode() {
        // Tracke wie oft jedes Event schon verwendet wurde
        int[] eventUsageCount = new int[SwimmingEvent.values().length];
        for (int i = 0; i < order.length; i++) {
            int eventIndex = order[i][0];
            if (eventIndex >= 0 && lineUp.get(i) != null) {
                eventUsageCount[eventIndex]++;
            }
        }

        // Versuche jedes beste EventResult nacheinander zu platzieren
        while (optimizedEventResultIndex < sortedEventResults.size()) {
            EventResult result = sortedEventResults.get(optimizedEventResultIndex);
            optimizedEventResultIndex++;

            // Prüfe ob dieses Event schon 2x verwendet wurde
            if (eventUsageCount[result.eventIndex] >= 2) {
                continue; // Überspringe, dieses Event hat keine Plätze mehr
            }

            // Erstelle neuen Child-Knoten
            TeamNode nextChild = new TeamNode(this);

            // Finde die Kopie dieses Schwimmers im Child
            Swimmer swimmerCopy = null;
            SwimmingEvent event = SwimmingEvent.values()[result.eventIndex];
            for (Swimmer s : nextChild.leaderboards.get(event)) {
                if (s.getID() == result.swimmer.getID()) {
                    swimmerCopy = s;
                    break;
                }
            }

            if (swimmerCopy == null) {
                continue; // Schwimmer nicht gefunden, weiter zum nächsten
            }

            // Suche eine freie Position für diesen Event
            for (int i = 0; i < nextChild.order.length; i++) {
                int eventIndex = nextChild.order[i][0];

                // Ist diese Position frei und für diesen Event?
                if (eventIndex == result.eventIndex && nextChild.lineUp.get(i) == null) {
                    // Kann der Schwimmer diese Position schwimmen?
                    if (swimmerCopy.canChooseOrderIndex(i)) {
                        // JA → Platziere es!
                        swimmerCopy.chooseEvent(i);
                        int pointsForEvent = swimmerCopy.getPointsForOrderIndex(i);
                        nextChild.totalPoints += pointsForEvent;
                        nextChild.lineUp.put(i, swimmerCopy);

                        // Berechne neue Upper Bound
                        nextChild.setUpperBound();

                        // Setze Flag zurück für nächste Position
                        nextChild.optimizedEventResultIndex = 0;

                        return nextChild;
                    }
                }
            }
        }

        // Keine gültigen Child-Knoten mehr
        this.optimizedEventResultIndex = 0;
        return null;
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
        sb.append("Global Event Results (sorted by points, best first):\n");

        for (EventResult result : sortedEventResults) {
            SwimmingEvent event = SwimmingEvent.values()[result.eventIndex];
            sb.append(String.format("%s - %s: %d points%n",
                    result.swimmer.getName(),
                    event.getDisplayName(),
                    result.points));
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