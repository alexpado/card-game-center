package fr.alexpado.cgc.data;

import fr.alexpado.cgc.data.states.PlayerHand;
import fr.alexpado.cgc.data.states.game.*;
import fr.alexpado.cgc.entities.Card;
import fr.alexpado.cgc.enums.CardType;
import fr.alexpado.cgc.enums.PlayTurn;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Game {

    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

    public static final int  CARD_PER_PLAYER = 7;
    public static final long POINT_TO_WIN    = 5;

    private final long            server;
    private       InteractionHook hook;
    private       GameState       state;
    private final List<Player>    players;
    private final List<Card>      deck;

    private final BlockingDeque<Card> blackCards;
    private final BlockingDeque<Card> redCards;

    private final long pointToWin;

    private ZonedDateTime lastHookRefresh;
    private Player        latestWinner;
    private Player        boss;
    private Card          card;
    private int           round;

    public Game(InteractionHook hook, ISnowflake guild, Collection<Card> cards, long pointToWin) {

        this.server     = guild.getIdLong();
        this.hook       = hook;
        this.deck       = new ArrayList<>(cards);
        this.pointToWin = pointToWin;
        this.players    = new ArrayList<>();
        this.round      = 0;

        this.blackCards = new LinkedBlockingDeque<>();
        this.redCards   = new LinkedBlockingDeque<>();

        this.shuffleDeck();
        this.lastHookRefresh = ZonedDateTime.now();
        this.setTurn(PlayTurn.WAITING, new WaitingState(this.players));
    }

    public void refreshDisplay(boolean withPlayer) {

        MessageEditBuilder builder = new MessageEditBuilder();
        this.getState().getHandler().accept(builder);
        this.hook.editOriginal(builder.build()).complete();

        if (withPlayer) {
            this.players.forEach(player -> {
                if (player.getHook() != null) {
                    InteractionHook    playerHook    = player.getHook();
                    MessageEditBuilder playerBuilder = new MessageEditBuilder();
                    new PlayerHand(this, player, true).getHandler().accept(playerBuilder);
                    try {
                        playerHook.editOriginal(playerBuilder.build()).complete();
                    } catch (Exception e) {
                        player.setHook(null);
                    }
                }
            });
        }
    }

    public void setHook(InteractionHook hook) {

        this.hook            = hook;
        this.lastHookRefresh = ZonedDateTime.now();
    }

    public long getServerId() {

        return this.server;
    }

    public void addParticipant(UserSnowflake who) {

        if (!this.isParticipating(who) && this.getState().getPlayTurn().isAllowingPlayerMovement()) {
            Player player = new Player(who);
            player.setReady(true);
            this.players.add(player);
        }
    }

    public void removeParticipant(UserSnowflake who) {

        if (this.isParticipating(who) && this.getState().getPlayTurn().isAllowingPlayerMovement()) {
            Player player = this.getPlayer(who);
            player.getCards().forEach(this.redCards::offer);
            this.players.remove(player);
        }
    }

    public void setTurn(PlayTurn turn, GameState state) {

        if (turn != state.getPlayTurn()) {
            throw new IllegalArgumentException("Incompatible GameState with PlayTurn provided.");
        }

        this.state = state;
    }

    public GameState getState() {

        return this.state;
    }

    public void check() {

        if (this.getPlayersWithoutBoss().stream().allMatch(Player::isReady)) {
            this.setTurn(PlayTurn.BOSS, new BossTurnState(this));
        }
    }

    public boolean isExpiringSoon() {

        return Duration.between(this.lastHookRefresh, ZonedDateTime.now()).getSeconds() >= 13 * 60;
    }

    // <editor-fold desc="Game Controls">
    private void shuffleDeck() {

        Collections.shuffle(this.deck);
        this.blackCards.clear();
        this.redCards.clear();

        this.deck.stream().filter(card -> !this.isCardPossessed(card)).forEach(card -> {
            if (card.getType() == CardType.BLACK) {
                this.blackCards.offer(card);
            } else {
                this.redCards.offer(card);
            }
        });
    }

    public Card drawCard(CardType type) {

        if (this.blackCards.isEmpty() || this.redCards.isEmpty()) {
            this.shuffleDeck();
        }

        if (type == CardType.BLACK) {
            return this.blackCards.poll();
        } else {
            return this.redCards.poll();
        }
    }

    public void selectWinner(UserSnowflake who) {

        Player player = this.getPlayer(who);
        player.score();

        this.latestWinner = player;
        this.setTurn(PlayTurn.NEXT, new TurnFinishedState(this));
    }

    public void startGame() {

        for (Player player : this.players) {
            player.play();
            while (player.getCards().size() < Game.CARD_PER_PLAYER) {
                player.addCard(this.drawCard(CardType.RED));
            }
        }

        this.players.forEach(player -> player.setReady(false));
        this.boss  = this.players.get(new Random().nextInt(this.players.size()));
        this.card  = this.drawCard(CardType.BLACK);
        this.round = 1;
        this.setTurn(PlayTurn.PLAYERS, new PlayerTurnState(this));
    }

    public boolean nextTurn() {

        if (!this.getPlayersWithoutBoss().stream().allMatch(Player::isReady)) {
            return true;
        }

        if (this.getPlayers().stream().anyMatch(player -> player.getScore() >= this.pointToWin)) {
            this.setTurn(PlayTurn.FINISHED, new GameFinishedState(this));
            return false;
        }

        this.boss = this.latestWinner;
        this.card = this.drawCard(CardType.BLACK);
        this.players.forEach(player -> {
            player.setReady(false);
            player.getSubmittedCard().clear();
        });
        this.round++;
        this.setTurn(PlayTurn.PLAYERS, new PlayerTurnState(this));
        return true;
    }

    // </editor-fold>

    // <editor-fold desc="Accessors">
    public Player getPlayer(UserSnowflake who) {

        return this.findPlayer(who)
                   .orElseThrow(() -> new IllegalArgumentException("The provided player is not taking part to this game."));
    }

    public Optional<Player> findPlayer(UserSnowflake who) {

        return this.players.stream().filter(player -> player.getId() == who.getIdLong()).findAny();
    }

    public List<Player> getPlayersWithoutBoss() {

        return this.players.stream().filter(this::isPlayer).toList();
    }

    public int getRound() {

        return this.round;
    }

    public Player getLatestWinner() {

        return this.latestWinner;
    }

    public List<Player> getPlayers() {

        return this.players;
    }

    public Card getCard() {

        return this.card;
    }

    public Player getBoss() {

        return this.boss;
    }

    public long getPointToWin() {
        return this.pointToWin;
    }
    // </editor-fold>

    // <editor-fold desc="Utilities">
    public boolean isParticipating(UserSnowflake who) {

        return this.findPlayer(who).isPresent();
    }

    public boolean isBoss(UserSnowflake who) {

        Player player = this.getPlayer(who);
        return this.isBoss(player);
    }

    public boolean isBoss(Player player) {

        return player.equals(this.boss);
    }

    public boolean isPlayer(UserSnowflake who) {

        return !this.isBoss(who);
    }

    public boolean isPlayer(Player player) {

        return !this.isBoss(player);
    }

    public boolean isCardPossessed(Card card) {

        return this.getPlayers().stream().anyMatch(player -> player.getCards().contains(card));
    }

    // </editor-fold>

}
