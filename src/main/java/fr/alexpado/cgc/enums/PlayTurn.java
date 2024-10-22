package fr.alexpado.cgc.enums;

public enum PlayTurn {

    WAITING(true),
    PLAYERS(false),
    BOSS(false),
    NEXT(true),
    FINISHED(false);

    private final boolean allowingPlayerMovement;

    PlayTurn(boolean allowingPlayerMovement) {

        this.allowingPlayerMovement = allowingPlayerMovement;
    }

    public boolean isAllowingPlayerMovement() {

        return this.allowingPlayerMovement;
    }
}
