package dev.netrox.quickbridge;

public enum ControlMode {
    HOLD("Hold"),
    TOGGLE("Toggle");

    private final String displayName;

    ControlMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public ControlMode next() {
        ControlMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
