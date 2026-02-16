package objects;

import java.util.Arrays;

/**
 * Die Klasse Swimmer repräsentiert einen Schwimmer aus einem Verein.
 * <p>
 * Jeder Schwimmer besteht aus:
 * einer eindeutigen ID (automatisch generiert),
 * einem Namen,
 * einem Geschlecht (männlich oder weiblich),
 * einer Liste von Events, an denen er teilnimmt,
 * einer Liste von Punkten für jedes Event.
 * </p>
 *
 * @author Timon Spieth
 * @version 1.0
 * @since 2026-02-10
 */

public class Swimmer {
    public static int nextId = 1; // static variable to keep track of the next available ID
    public int id;
    private String name;
    private int endurance; // endurance level of the swimmer, determines how long breaks have to be between
                           // events
    private boolean isMale;
    private int countChoosenEvents = 0; // keeps track of how many events the swimmer has chosen, used to check if the
    // swimmer can choose more events based on their endurance
    private boolean[] eventIndices = new boolean[Competition.order.length]; // used to calculate brektimes between
                                                                            // events
    private double[] times = new double[Competition.eventCount];
    private boolean[] choosenEvents = new boolean[Competition.eventCount]; // contains true for events the swimmer
                                                                           // takes, false otherwise
    private int[] points = new int[Competition.eventCount]; // contains points for each event,
    // when there is no points for an event, the value is -1

    /*
     * Event index mapping:
     * 0 - 50m freestyle
     * 1 - 100m freestyle
     * 2 - 200m freestyle
     * 3 - 400m freestyle
     * 4 - 800m freestyle
     * 5 - 1500m freestyle
     * 6 - 50m backstroke
     * 7 - 100m backstroke
     * 8 - 200m backstroke
     * 9 - 50m breaststroke
     * 10 - 100m breaststroke
     * 11 - 200m breaststroke
     * 12 - 50m butterfly
     * 13 - 100m butterfly
     * 14 - 200m butterfly
     * 15 - 100m individual medley
     * 16 - 200m individual medley
     * 17 - 400m individual medley
     */

    public Swimmer(String name, boolean isMale, int endurance) {
        this.id = nextId++; // assign the current nextId to id and then increment nextId
        this.name = name;
        this.endurance = endurance;
        this.isMale = isMale;
        Arrays.fill(choosenEvents, false); // initialize all events as not chosen
        Arrays.fill(points, -1); // initialize all points to -1 (indicating not defined)
        Arrays.fill(times, -1); // initialize all times to -1

    }

    public Swimmer() {
        this.id = nextId++;
    }

    /**
     * Gibt den Namen des Schwimmers zurück.
     * 
     * @return Name des Schwimmers
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt zurück, ob der Schwimmer männlich ist.
     * 
     * @return true, wenn der Schwimmer männlich ist, sonst false
     */
    public boolean isMale() {
        return isMale;
    }

    public int getEndurance() {
        return endurance;
    }

    /**
     * Gibt die Punkte für ein bestimmtes Event zurück.
     * 
     * @param eventIndex
     * @return
     */
    public int getPointsForEvent(SwimmingEvent event) { // method to get points for a specific event
        int eventIndex = event.getIndex();
        if (eventIndex >= 0 && eventIndex < points.length) {
            return points[eventIndex];
        } else {
            throw new IllegalArgumentException("Invalid event index");
        }
    }

    public int getTotalPoints() { // method to calculate total points across all choosen events
        int total = 0;
        for (int i = 0; i < choosenEvents.length; i++) {
            if (choosenEvents[i]) { // only consider events that the swimmer has chosen
                total += points[i]; // add points for the event to the total
            }
        }
        return total;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEndurance(int endurance) {
        this.endurance = endurance;
    }

    public void setIsMale(boolean isMale) {
        this.isMale = isMale;
    }

    public void setPointsForEvent(SwimmingEvent event, double time) {
        int eventIndex = event.getIndex();
        if (eventIndex >= 0 && eventIndex < this.points.length) {
            this.points[eventIndex] = Competition.calculatePoints(event, time, this.isMale); // calculate and set
                                                                                             // points for the
                                                                                             // event
        } else {
            throw new IllegalArgumentException("Invalid event index");
        }
    }

    public void setPointsForEvent(String event, String time) {
        SwimmingEvent swimmingEvent = SwimmingEvent.getByDisplayName(event);
        this.setPointsForEvent(swimmingEvent, Competition.getTimeFromString(time));
    }

    public void setTimeForEvent(SwimmingEvent event, double time) {
        int eventIndex = event.getIndex();
        if (eventIndex >= 0 && eventIndex < this.times.length) {
            this.times[eventIndex] = time; // set time for the event
        } else {
            throw new IllegalArgumentException("Invalid event index");
        }
    }

    public void setTimeForEvent(String event, String time) {
        SwimmingEvent swimmingEvent = SwimmingEvent.getByDisplayName(event);
        this.setTimeForEvent(swimmingEvent, Competition.getTimeFromString(time));
    }

    /**
     * Choses an event for the swimmer if
     * -swimmer has not already chosen the maximum number of events
     * -event is not already chosen
     * -event index is valid
     * 
     * @param event
     * @return true if the event was successfully chosen
     * @author Timon Spieth
     * @since 2026-02-11
     */
    private boolean chooseEvent(SwimmingEvent event) {
        int eventIndex = event.getIndex();
        if (this.countChoosenEvents >= Competition.maxEventsPerSwimmer) {
            return false; // swimmer has already chosen the maximum number of events, return false to
                          // indicate that the event was not chosen
        }
        if (eventIndex >= 0 && eventIndex < this.choosenEvents.length) {
            if (this.choosenEvents[eventIndex]) {
                return false; // event is already chosen, return false to indicate it was not added again
            } else {
                this.choosenEvents[eventIndex] = true; // mark the event as chosen
                this.countChoosenEvents++; // increment the count of chosen events
                return true; // event was successfully chosen
            }
        } else {
            throw new IllegalArgumentException("Invalid event index");
        }
    }

    public boolean chooseEvent(int competitionIndex) {
        if (competitionIndex >= 0 && competitionIndex < Competition.order.length) {
            int eventIndex = Competition.order[competitionIndex][0];
            if (eventIndex == -1) {
                return false; // this is a break, not an event, return false to indicate that no event was
                              // chosen
            }
            SwimmingEvent event = SwimmingEvent.values()[eventIndex];

            if (chooseEvent(event)) {
                eventIndices[competitionIndex] = true; // mark the event index as chosen for break time calculation
                return true; // event was successfully chosen
            } else {
                return false; // event could not be chosen (either max events reached or event already
                              // chosen), return false to indicate that the event was not chosen
            }
        } else {
            throw new IllegalArgumentException("Invalid competition index");
        }
    }

    /**
     * Checks if the swimmer can choose a specific event
     * - swimmer has not already chosen the maximum number of events
     * - event is not already chosen
     * - event index is valid
     * 
     * @param event
     * @return true if the swimmer can choose the event, false otherwise
     * @author Timon Spieth
     * @since 2026-02-11
     */
    public boolean canChooseEvent(SwimmingEvent event) {
        int eventIndex = event.getIndex();
        if (this.countChoosenEvents >= Competition.maxEventsPerSwimmer) {
            return false; // swimmer has already chosen the maximum number of events, return false to
                          // indicate that the event cannot be chosen
        }
        if (eventIndex >= 0 && eventIndex < this.choosenEvents.length) {
            return !this.choosenEvents[eventIndex]; // return true if the event is not already chosen, false otherwise
        } else {
            throw new IllegalArgumentException("Invalid event index");
        }
    }

    public void updatePoints() {
        for (int i = 0; i < times.length; i++) {
            if (times[i] != -1) { // only update points for events that have a defined time
                setPointsForEvent(SwimmingEvent.values()[i], times[i]);
            }
        }
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();

        // Tableheader
        String headerFormat = "%-8s | %-10s | %-10s%n";
        ;
        sb.append(String.format(headerFormat, "Event", "Time", "Points " + (isMale ? " (m)" : " (f)")));
        sb.append("-----------------------------------\n");

        // rows
        String rowFormat = "%-8s | %-10.3f | %-10d%n";
        for (SwimmingEvent event : SwimmingEvent.values()) {
            sb.append(String.format(rowFormat, event.getDisplayName(), times[event.getIndex()],
                    points[event.getIndex()]));
        }

        return "Name: " + name + ", Geschlecht: " + (isMale ? "maennlich" : "weiblich") + "\n" + sb.toString();

    }
}
