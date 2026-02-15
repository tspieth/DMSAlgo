package objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Die Klasse SwimmingClub repräsentiert einen Schwimmverein, der an der DMS
 * teilnimmt.
 * <p>
 * Jeder Schwimmverein besteht aus einer Liste von Schwimmern und einer Liste
 * von Schwimmer
 * die aktuell in der DMS teilnehmen (teamSwimmer). Die Klasse enthält eine
 * Methode zur Berechnung der Gesamtpunkte des Vereins.
 * </p>
 * 
 * @author Timon Spieth
 * @version 1.0
 * @since 2026-02-11
 */
public class SwimmingClub {

    private List<Swimmer> swimmers;
    private List<Swimmer> teamSwimmer; // contains the swimmers that are currently in the competition, used to
                                       // calculate points
    private Map<SwimmingEvent, List<Swimmer>> leaderboardsMale = new HashMap<SwimmingEvent, List<Swimmer>>();
    private Map<SwimmingEvent, List<Swimmer>> leaderboardsFemale = new HashMap<SwimmingEvent, List<Swimmer>>();

    public SwimmingClub(List<Swimmer> swimmers) {
        this.swimmers = swimmers;
    }

    public void addSwimmerToTeam(Swimmer swimmer) {
        if (swimmers.contains(swimmer) && !teamSwimmer.contains(swimmer)) {
            teamSwimmer.add(swimmer); // adds a swimmer to the team if they are in the swimmers list and not already
                                      // in the team
        }
    }

    public void addMultipleSwimmersToTeam(List<Swimmer> swimmersToAdd) {
        for (Swimmer swimmer : swimmersToAdd) {
            addSwimmerToTeam(swimmer); // adds multiple swimmers to the team using the addSwimmerToTeam method

        }
    }

    public void generateLeaderboards(boolean isMale) {
        for (SwimmingEvent event : SwimmingEvent.values()) {
            List<Swimmer> swimmerByPoints = swimmers.stream()
                    .filter(swimmer -> swimmer.getPointsForEvent(event) != -1 && swimmer.isMale() == isMale)
                    .sorted((s1, s2) -> Integer.compare(s2.getPointsForEvent(event), s1.getPointsForEvent(event)))
                    .toList();
            if (isMale) {
                leaderboardsMale.put(event, swimmerByPoints);
            } else {
                leaderboardsFemale.put(event, swimmerByPoints);
            }
        }
    }

    public void generateLeaderboards() {
        generateLeaderboards(true);
        generateLeaderboards(false);
    }

    public int getTotalPoints() {
        int totalPoints = 0;
        for (Swimmer swimmer : teamSwimmer) {
            totalPoints += swimmer.getTotalPoints(); // adds the total points of each swimmer to the total
        }
        return totalPoints;
    }

    public List<Swimmer> getAllSwimmer() {
        return swimmers;
    }

    public String toStringLeaderboard(int topN, SwimmingEvent event, boolean isMale) {
        StringBuilder sb = new StringBuilder();
        sb.append("Leaderboard for ").append(event.getDisplayName()).append(isMale ? " (m)" : " (f)").append(":\n");
        List<Swimmer> swimmersForEvent = isMale ? leaderboardsMale.get(event) : leaderboardsFemale.get(event);
        for (int i = 0; i < Math.min(topN, swimmersForEvent.size()); i++) {
            Swimmer swimmer = swimmersForEvent.get(i);
            sb.append((i + 1)).append(". ").append(swimmer.getName()).append(" - ")
                    .append(swimmer.getPointsForEvent(event)).append(" points\n");
        }
        return sb.toString();
    }

    public String toStringLeaderboards(int topN, boolean isMale) {
        StringBuilder sb = new StringBuilder();
        for (SwimmingEvent event : SwimmingEvent.values()) {
            sb.append(toStringLeaderboard(topN, event, isMale)).append("\n");
            sb.append("****************************\n");
        }
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Swimming Club:\n");
        for (Swimmer swimmer : swimmers) {
            sb.append(swimmer.toString()).append("\n");
            sb.append("*******************************\n");
        }
        return sb.toString();
    }

}