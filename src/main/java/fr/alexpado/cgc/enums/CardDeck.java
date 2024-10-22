package fr.alexpado.cgc.enums;

public enum CardDeck {

    LIMITE_LIMITE("(L)"),
    BLANC_MANGER_COCO("(B)");

    private final String deckName;

    CardDeck(String deckName) {

        this.deckName = deckName;
    }

    public String getDeckName() {

        return this.deckName;
    }
}
