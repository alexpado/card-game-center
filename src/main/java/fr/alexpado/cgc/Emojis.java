package fr.alexpado.cgc;

import fr.alexpado.cgc.entities.Card;

import java.util.Arrays;
import java.util.List;

public class Emojis {

    public static final String LOAD = "<a:load:1040436813468282900>";
    public static final String READY = "<:ready:1040437568065511526>";
    public static final String CARD = "\uD83C\uDFB4";
    public static final String BOSS = "\uD83C\uDFA9";

    public static final String ONE = "1️⃣";
    public static final String TWO = "2️⃣";
    public static final String THREE = "3️⃣";
    public static final String FOUR = "4️⃣";
    public static final String FIVE = "5️⃣";
    public static final String SIX = "6️⃣";
    public static final String SEVEN = "7️⃣";

    public static final List<String> NUMBERS = Arrays.asList(
            ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN
    );

    public static String toCard(Card card) {
        return String.format("%s %s", CARD, card.getText());
    }

    public static String fromCardIndex(int i) {
        return NUMBERS.get(i);
    }
}
