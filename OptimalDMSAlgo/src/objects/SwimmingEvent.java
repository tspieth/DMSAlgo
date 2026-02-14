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
public enum SwimmingEvent {
    FREESTYLE_50(0, "50K"),
    FREESTYLE_100(1, "100K"),
    FREESTYLE_200(2, "200K"),
    FREESTYLE_400(3, "400K"),
    FREESTYLE_800(4, "800K"),
    FREESTYLE_1500(5, "1500K"),

    BACKSTROKE_50(6, "50R"),
    BACKSTROKE_100(7, "100R"),
    BACKSTROKE_200(8, "200R"),

    BREASTSTROKE_50(9, "50B"),
    BREASTSTROKE_100(10, "100B"),
    BREASTSTROKE_200(11, "200B"),

    BUTTERFLY_50(12, "50S"),
    BUTTERFLY_100(13, "100S"),
    BUTTERFLY_200(14, "200S"),

    MEDLEY_100(15, "100Lg"),
    MEDLEY_200(16, "200Lg"),
    MEDLEY_400(17, "400Lg");

    private final int index;
    private final String displayName;

    SwimmingEvent(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SwimmingEvent getByDisplayName(String displayName) {
        for (SwimmingEvent event : values()) {
            if (event.displayName.equals(displayName)) {
                return event;
            }
        }
        throw new IllegalArgumentException("No event with display name " + displayName);
    }
}