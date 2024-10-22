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

import java.util.function.Consumer;

public class PlayerTurnState implements GameState {

    private final Game game;

    public PlayerTurnState(Game game) {

        this.game = game;
    }

    @Override
    public PlayTurn getPlayTurn() {

        return PlayTurn.PLAYERS;
    }

    @Override
    public boolean shouldEditOriginalMessage() {

        return true;
    }

    @Override
    public Consumer<MessageRequest<?>> getHandler() {

        return (mr) -> {

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(String.format("Limite Limite: Manche %s", this.game.getRound()));

            builder.setDescription(String.format(
                    "%s est le boss\n",
                    this.game.getBoss().getAsMention()
            ));

            builder.appendDescription("```");
            builder.appendDescription(this.game.getCard().getText());
            builder.appendDescription("```\n");

            for (Player player : this.game.getPlayersWithoutBoss()) {
                builder.appendDescription(String.format(
                        "%s %s %s\n",
                        player.isReady() ? Emojis.READY : Emojis.LOAD,
                        player.getAsMention(),
                        Emojis.CARD.repeat(Math.max(player.getActiveCards().size(), player.getSubmittedCard().size()))
                ));
            }

            if (this.game.isExpiringSoon()) {
                builder.setFooter("Cet affichage va bientôt expirer. Lorsque cela arrivera, supprimez ce message et utilisez la commande /game attach.");
            }

            mr.setEmbeds(builder.build());
            mr.setComponents(
                    ActionRow.of(
                            Button.primary("button://card/view", "Voir mes cartes")
                    )
            );
        };
    }

    @Override
    public boolean isEphemeral() {

        return false;
    }
}
