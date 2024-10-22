package fr.alexpado.cgc.interactions;

import fr.alexpado.jda.interactions.annotations.Interact;
import fr.alexpado.jda.interactions.annotations.Option;
import fr.alexpado.jda.interactions.annotations.Param;
import fr.alexpado.jda.interactions.responses.ButtonResponse;
import fr.alexpado.cgc.annotations.InteractAt;
import fr.alexpado.cgc.annotations.InteractionBean;
import fr.alexpado.cgc.data.Game;
import fr.alexpado.cgc.data.Player;
import fr.alexpado.cgc.data.states.PlayerHand;
import fr.alexpado.cgc.enums.CardType;
import fr.alexpado.cgc.enums.InteractionType;
import fr.alexpado.cgc.enums.PlayTurn;
import fr.alexpado.cgc.heplers.SimpleResponse;
import fr.alexpado.cgc.services.GameService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@InteractionBean
public class CardInteractions {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardInteractions.class);

    private final GameService service;

    public CardInteractions(GameService service) {

        this.service = service;
    }

    @Interact(name = "card/view", description = "Voir ses cartes")
    @InteractAt(InteractionType.BUTTON)
    public ButtonResponse viewCards(InteractionHook hook, Guild guild, Member member) {

        Game   game   = this.service.getGameWith(guild, member);
        Player player = game.getPlayer(member);
        player.setHook(hook);
        return new PlayerHand(game, player, false);
    }

    @Interact(name = "card/draw", description = "Piocher une carte")
    @InteractAt(InteractionType.BUTTON)
    public ButtonResponse drawCard(Guild guild, Member member) {

        Game   game   = this.service.getGameWith(guild, member);
        Player player = game.getPlayer(member);

        if (player.getCards().size() == Game.CARD_PER_PLAYER) {
            return new SimpleResponse("Vous avez le nombre maximum de carte autorisé.", false, true);
        }

        player.addCard(game.drawCard(CardType.RED));
        return new PlayerHand(game, player, true);
    }

    private ButtonResponse editPlayerCard(InteractionHook hook, Guild guild, Member member, Consumer<Player> consumer) {

        Game   game   = this.service.getGameWith(guild, member);
        Player player = game.getPlayer(member);
        player.setHook(hook);

        if (player.equals(game.getBoss())) {
            return new SimpleResponse("Le boss ne peut jouer de carte.", false, true);
        }

        if (game.getState().getPlayTurn() != PlayTurn.PLAYERS) {
            return new SimpleResponse("Vous ne pouvez pas jouer de carte pour l'instant.", false, true);
        }

        if (player.isReady()) {
            return new SimpleResponse("Vous ne pouvez plus changer d'avis après avoir finalisé votre choix.", false, true);
        }

        consumer.accept(player);
        game.check();
        game.refreshDisplay(false);
        return new PlayerHand(game, player, true);
    }

    @Interact(
            name = "card/play", description = "Jouer une carte", options = {
            @Option(name = "i", description = "Index de la carte", type = OptionType.INTEGER, required = true)
    }
    )
    @InteractAt(InteractionType.BUTTON)
    public ButtonResponse playCard(InteractionHook hook, Guild guild, Member member, @Param("i") Long index) {

        return this.editPlayerCard(hook, guild, member, player -> {
            player.setActiveCard(index.intValue());
        });
    }

    @Interact(
            name = "card/take", description = "Reprendre une carte", options = {
            @Option(name = "i", description = "Index de la carte", type = OptionType.INTEGER, required = true)
    }
    )
    @InteractAt(InteractionType.BUTTON)
    public ButtonResponse takeCard(InteractionHook hook, Guild guild, Member member, @Param("i") Long index) {

        return this.editPlayerCard(hook, guild, member, player -> {
            player.setInactiveCard(index.intValue());
        });
    }

    @Interact(name = "card/submit", description = "Valider son choix de carte")
    @InteractAt(InteractionType.BUTTON)
    public ButtonResponse submitCard(InteractionHook hook, Guild guild, Member member) {

        return this.editPlayerCard(hook, guild, member, player -> {
            player.setReady(true);
            player.play();
        });
    }
}
