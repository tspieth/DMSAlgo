package branchNbound;

import objects.Swimmer;

public class LeaderBoardEntry {
    private final Swimmer schwimmer;
    private final int eventIndex;

    public LeaderBoardEntry(Swimmer schwim, int idx) {
        this.schwimmer = schwim;
        this.eventIndex = idx;
    }

    public Swimmer getSchwimmer() {
        return this.schwimmer;
    }

    public int getEventIndex() {
        return this.eventIndex;
    }

    public int getPoints() {
        return schwimmer.getPointsForEventIndex(eventIndex);
    }

}
