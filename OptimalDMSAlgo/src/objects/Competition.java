package objects;

public class Competition {

    /*
     * League mapping:
     * 0 - 1. Bundesliga
     * 1 - 2. Bundesliga
     * 2 - Oberliga
     * 3 - Regionaliga
     * 4 - Bezirksliga
     * 
     */

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

    public static int eventCount = SwimmingEvent.values().length; // total number of events in the competition
    public static double[] baseTimesMale; // contains the base times for each event, used to calculate points
    public static double[] baseTimesFemale; // contains the base times for each event, used to calculate points
    public static int league;
    public static int maxEventsPerSwimmer = 5; // maximum number of events a swimmer can choose, used to check if a
                                               // swimmer can choose more events based on their endurance
    public static int[][] order; // orders events by time, the first eventIndex in the order is the one that
    // takes place first, and so on
    // order[i][0] gives eventIndex
    // order[i][1] gives the approximate time the event takes
    // if there is a break order[i][0] is -1 and order[i][1] is the duration of the
    // break
    // NOTE: every event needs to be included 4times (except 800F/1500F)

    public static int calculatePoints(SwimmingEvent event, double time, boolean isMale) {
        // calculates points for a given event and time using the formula:
        // points = (baseTime / time) * 1000
        // where baseTime is the base time for the event in the current league
        int eventIndex = event.getIndex();
        double baseTime = isMale ? baseTimesMale[eventIndex] : baseTimesFemale[eventIndex];
        return (int) (Math.pow(baseTime / time, 3) * 1000); // points are truncated to an integer
    }

    public static boolean setBaseTimesMale(double[] baseTimes) {
        if (baseTimes.length != eventCount) {
            return false; // base times array must have 18 elements, one for each event
        }
        baseTimesMale = baseTimes;
        return true;
    }

    public static boolean setBaseTimesFemale(double[] baseTimes) {
        if (baseTimes.length != eventCount) {
            return false; // base times array must have 18 elements, one for each event
        }
        baseTimesFemale = baseTimes;
        return true;
    }

    public static void setOrder(int[][] order) {
        Competition.order = order;
    }

    public static String toStringOrder() {
        StringBuilder sb = new StringBuilder();
        sb.append("Event Order:\n");
        int j = 1;
        for (int i = 0; i < order.length; i++) {
            int eventIndex = order[i][0];
            int eventTime = order[i][1];
            if (eventIndex == -1) {
                sb.append(String.format("------ BREAK (approx. %d min) ------ %n", eventTime));
                continue;
            }
            sb.append(String.format("%02d %5s (approx. %d min)%n", j++,
                    SwimmingEvent.values()[eventIndex].getDisplayName(), eventTime));
        }
        return sb.toString();
    }

    public static String toStringBaseTimes(boolean isMale) {
        double[] baseTimes = isMale ? baseTimesMale : baseTimesFemale;
        StringBuilder sb = new StringBuilder();

        // Tableheader
        String headerFormat = "%-8s | %-10s%n";
        sb.append(String.format(headerFormat, "Event", "BaseTime" + (isMale ? " (m)" : " (f)")));
        sb.append("----------------------\n");

        // rows
        String rowFormat = "%-8s | %-10.3f%n";
        for (SwimmingEvent event : SwimmingEvent.values()) {
            sb.append(String.format(rowFormat, event.getDisplayName(), baseTimes[event.getIndex()]));
        }

        return sb.toString();
    }

    public static double getTimeFromString(String timeString) {
        // converts a time string in the format "mm:ss.SS" or "ss.SS" to a double
        // representing the time in seconds
        String[] parts = timeString.split(":");
        double time = 0;
        if (parts.length == 2) {
            time += Integer.parseInt(parts[0]) * 60; // minutes to seconds
            time += Double.parseDouble(parts[1]); // seconds
        } else if (parts.length == 1) {
            time += Double.parseDouble(parts[0]); // seconds only
        } else if (parts.length == 0) {
            time = -1; // -1 indicates no time was provided
        } else {
            throw new IllegalArgumentException("Invalid time format: " + timeString);
        }
        return time;
    }

}