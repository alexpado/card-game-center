package fr.alexpado.cgc.data.states.game;

import fr.alexpado.cgc.data.GameState;
import fr.alexpado.cgc.data.Player;
import fr.alexpado.cgc.enums.PlayTurn;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WaitingState implements GameState {

    private final List<Player> players;

    public WaitingState(List<Player> players) {

        this.players = players;
    }

    @Override
    public PlayTurn getPlayTurn() {

        return PlayTurn.WAITING;
    }

    @Override
    public boolean shouldEditOriginalMessage() {

        return true;
    }

    @Override
    public Consumer<MessageRequest<?>> getHandler() {

        return (mr) -> {

            String participants = this.players.stream()
                                              .map(player -> UserSnowflake.fromId(player.getId()))
                                              .map(UserSnowflake::getAsMention)
                                              .collect(Collectors.joining("\n"));

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Limite Limite: En attente de joueurs...");
            builder.setDescription("*Il faut au minimum 3 joueurs pour lancer la partie.*");
            builder.addField("Participants:", participants, false);

            mr.setEmbeds(builder.build());
            mr.setComponents(
                    ActionRow.of(
                            this.getJoinButton(),
                            this.getLeaveButton(),
                            this.getStartButton()
                    )
            );
        };
    }

    private Button getJoinButton() {

        return Button.of(ButtonStyle.PRIMARY, "button://game/join", "Rejoindre");
    }

    private Button getLeaveButton() {

        return Button.of(ButtonStyle.DANGER, "button://game/leave", "Quitter");
    }

    private Button getStartButton() {

        return Button.of(ButtonStyle.SUCCESS, "button://game/start", "Démarrer")
                     .withDisabled(this.players.size() < 3);
    }

    @Override
    public boolean isEphemeral() {

        return false;
    }
}
