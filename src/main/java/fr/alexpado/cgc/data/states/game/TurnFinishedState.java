package fr.alexpado.cgc.data.states.game;

import fr.alexpado.cgc.Emojis;
import fr.alexpado.cgc.data.Game;
import fr.alexpado.cgc.data.GameState;
import fr.alexpado.cgc.data.Player;
import fr.alexpado.cgc.enums.PlayTurn;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TurnFinishedState implements GameState {

    private final Game game;

    public TurnFinishedState(Game game) {

        this.game = game;
    }

    @Override
    public PlayTurn getPlayTurn() {

        return PlayTurn.NEXT;
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
            String playedCard = this.game.getLatestWinner().getSubmittedCard().stream()
                                         .map(Emojis::toCard)
                                         .collect(Collectors.joining("\n"));


            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(String.format("Limite Limite: Manche %s terminée", this.game.getRound()));

            builder.setDescription(String.format(
                    "%s a remporté la victoire !\n",
                    this.game.getLatestWinner().getAsMention()
            ));

            builder.appendDescription("```");
            builder.appendDescription(this.game.getCard().getText());
            builder.appendDescription("```\n");

            builder.appendDescription(playedCard);
            builder.appendDescription("\n\nLeaderboard:");

            for (int i = 0 ; i < players.size() ; i++) {
                Player player = players.get(i);
                int position = i + 1;

                String name = String.format("%s. %s points", position, player.getScore());
                String desc = String.format("%s", player.getAsMention());

                builder.addField(name, desc, false);
            }

            if (this.game.isExpiringSoon()) {
                builder.setFooter("Cet affichage va bientôt expirer. Lorsque cela arrivera, supprimez ce message et utilisez la commande /game attach.");
            }

            mr.setEmbeds(builder.build());

            if (players.stream().anyMatch(player -> player.getScore() >= this.game.getPointToWin())) {
                mr.setComponents(ActionRow.of(
                        Button.of(ButtonStyle.SUCCESS, "button://round/next", "Voir les résultats")
                ));
            } else {
                mr.setComponents(
                        ActionRow.of(
                                Button.of(ButtonStyle.SUCCESS, "button://round/next", "Manche suivante")
                                      .withDisabled(this.game.getPlayers().size() < 3),
                                Button.of(ButtonStyle.PRIMARY, "button://game/join", "Rejoindre"),
                                Button.of(ButtonStyle.SECONDARY, "button://game/leave", "Quitter"),
                                Button.of(ButtonStyle.DANGER, "button://game/stop", "Arrêter la partie")
                        )
                );
            }
        };
    }

    @Override
    public boolean isEphemeral() {

        return false;
    }
}
