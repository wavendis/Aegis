package club.aurovion.aegis;

/** The checks Aegis ships with. */
public enum Check {
    SPEED("Speed"),
    FLY("Fly"),
    REACH("Reach");

    private final String display;

    Check(String display) {
        this.display = display;
    }

    public String display() {
        return display;
    }
}
