package fr.alexpado.cgc.data.states.game;

import fr.alexpado.cgc.data.Game;
import fr.alexpado.cgc.data.GameState;
import fr.alexpado.cgc.data.Player;
import fr.alexpado.cgc.enums.PlayTurn;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class GameFinishedState implements GameState {

    private final Game game;

    public GameFinishedState(Game game) {

        this.game = game;
    }

    @Override
    public PlayTurn getPlayTurn() {

        return PlayTurn.FINISHED;
    }

    @Override
    public boolean shouldEditOriginalMessage() {

        return true;
    }

    @Override
    public Consumer<MessageRequest<?>> getHandler() {

        return (mr) -> {
            List<Player> players = new ArrayList<>(this.game.getPlayers());
            players.sort(Comparator.comparing(Player::getScore).reversed().thenComparing(Player::getId));

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Limite Limite: Partie terminée");

            for (int i = 0 ; i < players.size() ; i++) {
                Player player   = players.get(i);
                int    position = i + 1;

                String name = String.format("%s. %s points", position, player.getScore());
                String desc = String.format("%s", player.getAsMention());

                builder.addField(name, desc, false);
            }

            mr.setEmbeds(builder.build());
            mr.setComponents(Collections.emptyList());
        };
    }

    @Override
    public boolean isEphemeral() {

        return false;
    }
}
