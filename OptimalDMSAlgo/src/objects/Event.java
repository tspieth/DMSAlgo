package objects;

/**
 * Die Klasse Event repräsentiert die verschiedenen Schwimmevents, die in der
 * DMS stattfinden.
 * <p>
 * Jedes Event hat einen eindeutigen Index und einen Anzeigenamen.
 * </p>
 * 
 * @author Timon Spieth
 * @version 1.0
 * @since 2026-02-10
 * 
 */
public enum Event {
    FREESTYLE_50(0, "50F"),
    FREESTYLE_100(1, "100F"),
    FREESTYLE_200(2, "200F"),
    FREESTYLE_400(3, "400F"),
    FREESTYLE_800(4, "800F"),
    FREESTYLE_1500(5, "1500F"),

    BACKSTROKE_50(6, "50R"),
    BACKSTROKE_100(7, "100R"),
    BACKSTROKE_200(8, "200R"),

    BREASTSTROKE_50(9, "50B"),
    BREASTSTROKE_100(10, "100B"),
    BREASTSTROKE_200(11, "200B"),

    BUTTERFLY_50(12, "50S"),
    BUTTERFLY_100(13, "100S"),
    BUTTERFLY_200(14, "200S"),

    MEDLEY_100(15, "100IM"),
    MEDLEY_200(16, "200IM"),
    MEDLEY_400(17, "400IM");

    private final int index;
    private final String displayName;

    Event(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return displayName;
    }
}