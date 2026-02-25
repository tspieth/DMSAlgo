package branchNbound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import objects.Competition;
import objects.Swimmer;
import objects.SwimmingClub;
import objects.SwimmingEvent;

public class TeamNode {

    public static Map<Integer, Swimmer> allSwimmer = new HashMap<>();
    // Integer marks the SwimmerID for each Swimmer
    public static List<LeaderBoardEntry> globalLead = new ArrayList<>();

    public static int lowerBound = 0;

    private int[][] currentLineUp = new int[SwimmingEvent.values().length][2];
    // saves current Linup in an efficient Array
    // currentLineUp[eventIndex][0] saves first choice SwimmerID
    // currentLineUp[eventIndex][1] sves second choice SwimmerID
    // order of choices is triveal in first version
    // currentLineUp[eventIndex][0] is -1 if there is no Swimmer assigned

    private boolean hasConflicts = false;
    private int[][] conflictSet;
    // conflicts when swimmer has Max eventCount
    // conflict[0] = -1
    // conflict[1] = swimmerID

    // conflicts when event is Full
    // conflict[0] = -2
    // conlict[1] = eventID

    private int emptySpotsLeft = SwimmingEvent.values().length * 2;
    private int totalPoints = 0;
    private int nextLeadIndex = 0;

    // =============================================================
    // KONSTRUKTOREN
    // =============================================================

    public TeamNode(SwimmingClub club, boolean isMale) {
        this.currentLineUp = new int[SwimmingEvent.values().length][2];
        this.hasConflicts = false;
        // initialize slots to -1 (empty)
        for (int i = 0; i < currentLineUp.length; i++) {
            currentLineUp[i][0] = -1;
            currentLineUp[i][1] = -1;
        }
        this.emptySpotsLeft = SwimmingEvent.values().length * 2;
        this.totalPoints = 0;
        this.nextLeadIndex = 0;
    }

    public TeamNode(TeamNode other) {
        // deep copy 2D array
        this.currentLineUp = new int[other.currentLineUp.length][2];
        for (int i = 0; i < other.currentLineUp.length; i++) {
            this.currentLineUp[i][0] = other.currentLineUp[i][0];
            this.currentLineUp[i][1] = other.currentLineUp[i][1];
        }
        this.totalPoints = other.totalPoints;
        this.emptySpotsLeft = other.emptySpotsLeft;

    }

    // =============================================================
    // LOGIK UM NAECHSTBESTEN ZU BERECHNEN
    // =============================================================

    public TeamNode nextTeamNode() {

        TeamNode nextTeamNode = new TeamNode(this);

        boolean hasInsertedNext = false;

        // Wenn wir nicht den Nächstbesten Nehmen können wählen wir einfach den nächsten
        // der Geht
        // Wir merken und nur den Conflict vom nächsbesten wenns nicht ging
        LeaderBoardEntry nextBest = globalLead.get(nextLeadIndex);

        // Determine if we can insert nextBestSwimmer
        int eventIndex = nextBest.getEventIndex();
        int swimmerID = nextBest.getSchwimmer().getID();
        boolean canSwim = canSwim(swimmerID);
        int insertAt = whereInsert(eventIndex);

        if (insertAt != -1) {
            if (canSwim) {
                nextTeamNode.currentLineUp[eventIndex][insertAt] = swimmerID;
                nextTeamNode.totalPoints += nextBest.getPoints();
                // next Node has 1 less Spot left
                nextTeamNode.emptySpotsLeft -= 1;
                nextTeamNode.nextLeadIndex = this.nextLeadIndex + 1;
                hasInsertedNext = true;
                return nextTeamNode;
            } else {
                // Swimmer HasMaxEvent Conflict
                this.conflictSet = new int[][] { { -1, swimmerID } };
                this.hasConflicts = true; // Continue to next candidate, but skip current index for next attempt
                nextTeamNode.nextLeadIndex = this.nextLeadIndex + 1;
            }
        } else {
            if (canSwim) {
                // Swimming event is already filled
                this.conflictSet = new int[][] { { -2, eventIndex } };
            } else {
                // Swimming event is filled AND swimmer HasMaxEvent Conflict
                this.conflictSet = new int[][] { { -1, swimmerID }, { -2, eventIndex } };
            }
            this.hasConflicts = true;
        }

        // SchleifenLogik bricht direkt ab wenn der Nächstbeste eingefügt wurde
        int nextLeadTemp = nextLeadIndex + 1;
        while (!hasInsertedNext && nextLeadTemp < globalLead.size()) {

            nextBest = globalLead.get(nextLeadTemp);

            eventIndex = nextBest.getEventIndex();
            swimmerID = nextBest.getSchwimmer().getID();
            canSwim = canSwim(swimmerID);
            insertAt = whereInsert(eventIndex);

            if (insertAt != -1) {
                if (canSwim) {

                    // Schwimmer wurde gefunden
                    nextTeamNode.currentLineUp[eventIndex][insertAt] = swimmerID;
                    nextTeamNode.totalPoints += nextBest.getPoints();

                    // Wenn man mit diesem Knoten weitermacht muss man beim nexten Index des
                    // LeaderBoards
                    // weitermachen.
                    nextTeamNode.nextLeadIndex = nextLeadTemp + 1;

                    nextTeamNode.emptySpotsLeft -= 1;
                    hasInsertedNext = true;
                    return nextTeamNode;
                }
            }
            // Wird nichtmehr aufgerufen, wenn Schwimmer gefunden wurde
            nextLeadTemp++;
        }

        return null;
    }

    // returns first Index where we can Insert -1 if eventIsFull
    public int whereInsert(int eventIndex) {
        if (currentLineUp[eventIndex][0] == -1) {
            return 0;
        }
        if (currentLineUp[eventIndex][1] == -1) {
            return 1;
        }
        return -1;
    }

    // Swimmer is allowed to Swim if he already compeates in less than
    // MaxEventsPerSwimmer
    public boolean canSwim(int swimmerID) {
        int eventCount = 0;
        for (int[] selcted : currentLineUp) {
            if (selcted[0] == swimmerID) {
                eventCount++;
                if (selcted[1] == swimmerID) {
                    System.out.println("WRONG: SWIMMER IS ONLY SUPPOSED TO SWIM EVENT ONCE" + swimmerID);
                    throw new IllegalAccessError();
                }
            }
            if (selcted[1] == swimmerID) {
                eventCount++;
            }
        }
        return eventCount < Competition.maxEventsPerSwimmer;
    }

    // =============================================================
    // BRANCH&BOUND METHODEN
    // =============================================================

    // Easy version could be Optimized with looking at individual free Spots left in
    // the event
    public int getUpperBound() {
        int tempNext = this.nextLeadIndex;
        int upper = this.totalPoints;

        for (int i = 1; i <= this.emptySpotsLeft; i++) {
            upper += globalLead.get(tempNext).getPoints();
            tempNext++;
        }
        return upper;
    }

    public boolean isPrunable() {
        return this.getUpperBound() < TeamNode.lowerBound;
    }

    public boolean isComplete() {
        return this.emptySpotsLeft == 0;
    }

    public boolean hasConflicts() {
        return this.hasConflicts;
    }

    public boolean fixConflicts() {
        for (int i = 0; i < conflictSet.length; i++) {

            LeaderBoardEntry nextBest = globalLead.get(nextLeadIndex);
            // To Much eventsConflict
            if (conflictSet[i][0] == -1) {
                this.removeWorstSwimmerID(nextBest.getSchwimmer().getID());
            }
            if (conflictSet[i][0] == -2) {
                this.emptyOneEventSlot(conflictSet[i][1]);
            }
        }
        return true;
    }

    private void removeWorstSwimmerID(int swimmerID) {

        int minPoints = Integer.MAX_VALUE;
        int eventIndex = -1;
        int pos = -1;

        for (int i = 0; i < currentLineUp.length; i++) {
            Swimmer s = allSwimmer.get(swimmerID);
            if (currentLineUp[i][0] == swimmerID) {

                int points = s.getPointsForEventIndex(i);
                if (minPoints > points) {
                    minPoints = points;
                    eventIndex = i;
                    pos = 0;
                }
                if (currentLineUp[i][1] == swimmerID) {
                    System.out.println("WRONG: SWIMMER IS ONLY SUPPOSED TO SWIM EVENT ONCE");
                    throw new IllegalAccessError();
                }
            }
            if (currentLineUp[i][1] == swimmerID) {
                int points = s.getPointsForEventIndex(i);
                if (minPoints > points) {
                    minPoints = points;
                    eventIndex = i;
                    pos = 1;
                }
            }
        }
        // Nachdem das Minimum gefunden wurde kann dieses Entfernt werden und

        // Abzug der Punkte
        this.totalPoints -= minPoints;
        // Als frei Kennzeichnen
        this.currentLineUp[eventIndex][pos] = -1;
    }

    public void emptyOneEventSlot(int eventID) {
        Swimmer a = allSwimmer.get(currentLineUp[eventID][0]);
        Swimmer b = allSwimmer.get(currentLineUp[eventID][1]);
        int pointsA = a.getPointsForEventIndex(eventID);
        int pointsB = b.getPointsForEventIndex(eventID);
        if (pointsA < pointsB) {
            this.currentLineUp[eventID][0] = -1;
            this.totalPoints -= pointsA;
        } else {
            this.currentLineUp[eventID][1] = -1;
            this.totalPoints -= pointsB;
        }
    }

    // =============================================================
    // GETTER METHODEN
    // =============================================================

    public int getTotalPoints() {
        return this.totalPoints;
    }

    public String toStringLineUp() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentLineUp.length; i++) {
            sb.append("Event ").append(SwimmingEvent.values()[i].getDisplayName()).append(": \n");
            if (currentLineUp[i][0] != -1) {
                sb.append(allSwimmer.get(currentLineUp[i][0]).getName()).append(" ");
                sb.append(allSwimmer.get(currentLineUp[i][0]).getPointsForEventIndex(i));
                sb.append("\n");
            }
            if (currentLineUp[i][1] != -1) {
                sb.append(allSwimmer.get(currentLineUp[i][1]).getName()).append(" ");
                sb.append(allSwimmer.get(currentLineUp[i][1]).getPointsForEventIndex(i));
            }
            sb.append("\n****************************\n");
        }
        return sb.toString();
    }

    // =============================================================
    // KLASSENMETHODEN
    // =============================================================

    public static void setAvailableSwimmer(SwimmingClub club, boolean isMale) {
        for (Swimmer s : club.getAllSwimmer()) {
            if (s.isMale() == isMale) {
                allSwimmer.put(s.getID(), s);
            }
        }
    }

    public static void setGlobalLeaderboard() {
        for (Swimmer s : allSwimmer.values()) {
            int[] pointsFromS = s.getPointsArr();

            for (int i = 0; i < pointsFromS.length; i++) {
                if (pointsFromS[i] != -1) {
                    LeaderBoardEntry newEntry = new LeaderBoardEntry(s, i);
                    globalLead.add(newEntry);
                }
            }
        }
        globalLead.sort((a, b) -> Integer.compare(b.getPoints(), a.getPoints()));
    }

    public static void toStringGlobalLead() {
        for (LeaderBoardEntry l : globalLead) {
            System.out.println(l.getPoints());
        }
    }

}
