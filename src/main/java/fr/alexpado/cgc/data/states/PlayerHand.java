package fr.alexpado.cgc.data.states;

import fr.alexpado.cgc.Emojis;
import fr.alexpado.cgc.data.Game;
import fr.alexpado.cgc.data.Player;
import fr.alexpado.cgc.entities.Card;
import fr.alexpado.cgc.heplers.MixedResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlayerHand implements MixedResponse {

    private final Game    game;
    private final Player  player;
    private final boolean edit;

    public PlayerHand(Game game, Player player, boolean edit) {

        this.game   = game;
        this.player = player;
        this.edit   = edit;
    }

    @Override
    public boolean shouldEditOriginalMessage() {

        return this.edit;
    }

    @Override
    public Consumer<MessageRequest<?>> getHandler() {

        return (mr) -> {
            int    currentCardAmount = this.player.getCards().size();
            int    maxCardAmount     = Game.CARD_PER_PLAYER;
            String activeCards       = this.player.getActiveCards()
                                                  .stream()
                                                  .map(Emojis::fromCardIndex)
                                                  .collect(Collectors.joining());

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(String.format("Limite Limite: Votre jeu (%s/%s)", currentCardAmount, maxCardAmount));
            builder.appendDescription(String.format("Vous vous apprêtez à jouer: %s\n\n", activeCards));

            for (int i = 0 ; i < this.player.getCards().size() ; i++) {
                Card card = this.player.getCards().get(i);

                builder.appendDescription(String.format(
                        "%s %s\n",
                        Emojis.fromCardIndex(i),
                        card.getText()
                ));
            }


            mr.setEmbeds(builder.build());

            mr.setComponents(
                    ActionRow.of(
                            Button.of(ButtonStyle.PRIMARY, "button://card/draw", "Piocher"),
                            Button.of(ButtonStyle.SUCCESS, "button://card/submit", "Valider")
                    ),
                    ActionRow.of(
                            this.getCardButton(0),
                            this.getCardButton(1),
                            this.getCardButton(2),
                            this.getCardButton(3)
                    ),
                    ActionRow.of(
                            this.getCardButton(4),
                            this.getCardButton(5),
                            this.getCardButton(6)
                    )
            );

        };
    }

    private Button getCardButton(int cardIndex) {

        boolean hasBeenSelected = this.player.getActiveCards().contains(cardIndex);
        boolean canPlayCard     = this.player.getActiveCards().size() < this.game.getCard().getCardToPlay();
        boolean possessCard     = this.player.getCards().size() > cardIndex;
        boolean disabled        = (!canPlayCard || !possessCard) && !hasBeenSelected;

        String id = String.format(
                "button://card/%s?i=%s",
                hasBeenSelected ? "take" : "play",
                cardIndex
        );

        String name = String.format("Carte %s", cardIndex + 1);

        return Button.of(hasBeenSelected ? ButtonStyle.DANGER : ButtonStyle.SECONDARY, id, name).withDisabled(disabled);
    }

    @Override
    public boolean isEphemeral() {

        return true;
    }
}
