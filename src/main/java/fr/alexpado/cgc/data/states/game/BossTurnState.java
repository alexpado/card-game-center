package fr.alexpado.cgc.data.states.game;

import fr.alexpado.cgc.Emojis;
import fr.alexpado.cgc.data.Game;
import fr.alexpado.cgc.data.GameState;
import fr.alexpado.cgc.data.Player;
import fr.alexpado.cgc.enums.PlayTurn;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BossTurnState implements GameState {

    private final Game game;

    public BossTurnState(Game game) {

        this.game = game;
    }

    @Override
    public PlayTurn getPlayTurn() {

        return PlayTurn.BOSS;
    }

    @Override
    public boolean shouldEditOriginalMessage() {

        return true;
    }

    @Override
    public Consumer<MessageRequest<?>> getHandler() {

        return (mr) -> {
            Collection<Button> components      = new ArrayList<>();
            List<Player>       shuffledPlayers = new ArrayList<>(this.game.getPlayersWithoutBoss());
            Collections.shuffle(shuffledPlayers);

            EmbedBuilder builder = new EmbedBuilder();

            builder.setTitle(String.format("Limite Limite: Manche %s", this.game.getRound()));

            builder.setDescription(String.format(
                    "%s %s choisi un gagnant...\n\n",
                    Emojis.BOSS,
                    this.game.getBoss().getAsMention()
            ));

            builder.appendDescription("```");
            builder.appendDescription(this.game.getCard().getText());
            builder.appendDescription("```");

            for (int i = 0 ; i < shuffledPlayers.size() ; i++) {
                Player player   = shuffledPlayers.get(i);
                int    position = i + 1;

                String playerName = String.format("Joueur %s", position);
                String playerCards = player.getSubmittedCard().stream()
                                           .map(Emojis::toCard)
                                           .collect(Collectors.joining("\n"));

                builder.addField(playerName, playerCards, false);
                components.add(player.asWinButton(position));
            }

            if (this.game.isExpiringSoon()) {
                builder.setFooter("Cet affichage va bientôt expirer. Lorsque cela arrivera, supprimez ce message et utilisez la commande /game attach.");
            }

            mr.setEmbeds(builder.build());
            mr.setComponents(ActionRow.of(components));
        };
    }

    @Override
    public boolean isEphemeral() {

        return false;
    }
}
