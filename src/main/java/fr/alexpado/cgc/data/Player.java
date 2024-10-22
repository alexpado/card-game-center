package fr.alexpado.cgc.data;

import fr.alexpado.cgc.entities.Card;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.*;

public class Player {

    private final long            id;
    private final List<Card>      cards;
    private final List<Card>      submittedCard;
    private final List<Integer>   activeCards;
    private       InteractionHook hook;
    private       int             score;
    private       boolean         ready;

    public Player(UserSnowflake user) {

        this.id            = user.getIdLong();
        this.cards         = new ArrayList<>();
        this.activeCards   = new ArrayList<>();
        this.submittedCard = new ArrayList<>();
        this.score         = 0;
        this.ready         = false;
    }

    public long getId() {

        return this.id;
    }

    public boolean isReady() {

        return this.ready;
    }

    public void setReady(boolean ready) {

        this.ready = ready;
    }

    public String getAsMention() {

        return UserSnowflake.fromId(this.getId()).getAsMention();
    }

    public List<Card> getCards() {

        return this.cards;
    }

    public List<Integer> getActiveCards() {

        return this.activeCards;
    }

    public List<Card> getPlayedCards() {

        List<Card> cards = new ArrayList<>();
        this.activeCards.forEach(i -> cards.add(this.cards.get(i)));
        return cards;
    }

    public List<Card> getSubmittedCard() {

        return this.submittedCard;
    }

    public void setActiveCard(int index) {

        if (index >= 0 && index < this.cards.size() && !this.activeCards.contains(index)) {
            this.activeCards.add(index);
        }
    }

    public void setInactiveCard(Integer index) {

        if (index >= 0 && index < this.cards.size()) {
            this.activeCards.remove(index);
        }
    }

    public void addCard(Card card) {

        this.cards.add(card);
    }

    public void play() {

        this.submittedCard.clear();
        this.submittedCard.addAll(this.getPlayedCards());
        this.cards.removeAll(this.submittedCard);
        this.activeCards.clear();
    }

    public InteractionHook getHook() {

        return this.hook;
    }

    public void setHook(InteractionHook hook) {

        this.hook = hook;
    }

    public void score() {

        this.score++;
    }

    public int getScore() {

        return this.score;
    }

    public boolean hasPlayed(int amount) {

        return this.getActiveCards().size() == amount;
    }

    public Button asWinButton(int index) {

        return Button.success(String.format("button://round/winner?id=%s", this.getId()), "Joueur " + index);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {return true;}
        if (o == null || this.getClass() != o.getClass()) {return false;}
        Player player = (Player) o;
        return this.id == player.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.id);
    }
}
