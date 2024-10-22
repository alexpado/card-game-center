package fr.alexpado.cgc.entities;

import fr.alexpado.cgc.enums.CardDeck;
import fr.alexpado.cgc.enums.CardType;
import fr.alexpado.cgc.heplers.StringHelper;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Card {

    private static final String FILLER = "▁▁▁▁▁▁▁";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardDeck deck;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardType type;

    @Column(columnDefinition = "LONGTEXT")
    private String text;

    public Long getId() {

        return this.id;
    }

    public CardDeck getDeck() {

        return this.deck;
    }

    public CardType getType() {

        return this.type;
    }

    public String getText() {

        return this.text;
    }

    public int getCardToPlay() {

        if (this.getType() == CardType.BLACK) {
            return Math.max(1, StringHelper.count(this.getText(), FILLER));
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {return true;}
        if (o == null || this.getClass() != o.getClass()) {return false;}
        Card card = (Card) o;
        return this.getId().equals(card.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.getId());
    }
}
