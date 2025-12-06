package supermarket.event;

import java.time.LocalDate;

public class Event {
    private final EventType type;
    private final LocalDate date;
    private final String description;

    public Event(EventType type, LocalDate date, String description, Object data) {
        this.type = type;
        this.date = date;
        this.description = description;
    }

    public Event(EventType type, LocalDate date, String description) {
        this(type, date, description, null);
    }

    public EventType getType() { return type; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "[" + date + "] " + type + ": " + description;
    }
}