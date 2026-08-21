package dev.netrox.quickbridge;

public enum TechniquePhase {
    IDLE("OFF"),
    CRUISE("RUN"),
    STRAFE_A("STRAFE L"),
    STRAFE_B("STRAFE R"),
    RUNUP("RUNUP"),
    JUMP("JUMP"),
    TURN("TURN"),
    BURST("BURST"),
    RESET("RESET"),
    RECOVERY("RECOVERY");

    private final String displayName;

    TechniquePhase(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
