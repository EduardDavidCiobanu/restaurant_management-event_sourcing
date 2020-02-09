package Common;

/**
 * author Ciobanu Eduard David
 */
public class Table {
    public enum Status {
        FREE,
        OCCUPIED,
        RESERVED;

        public static Status map(String stringVal) {
            switch (stringVal) {
                case "OCCUPIED":
                    return OCCUPIED;
                case "RESERVED":
                    return RESERVED;
                default:
                    return FREE;
            }
        }
    }

    ;

    public enum Location {
        OUTSIDE,
        INSIDE;

        public static Location map(String stringVal) {
            if ("OUTSIDE".equals(stringVal)) {
                return OUTSIDE;
            }
            return INSIDE;
        }
    }

    private int number;
    private Status status;
    private int seats;
    private Location location;

    public Table(Status status, int number, int seats, Location location) {
        this.status = status;
        this.number = number;
        this.seats = seats;
        this.location = location;
    }

    public Status getStatus() {
        return status;
    }

    public int getNumber() {
        return number;
    }

    public int getSeats() {
        return seats;
    }

    public Location getLocation() {
        return location;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
