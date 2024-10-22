package fr.alexpado.cgc.interactions;

import fr.alexpado.jda.interactions.annotations.Interact;
import fr.alexpado.jda.interactions.responses.ButtonResponse;
import fr.alexpado.jda.interactions.responses.SlashResponse;
import fr.alexpado.cgc.annotations.InteractAt;
import fr.alexpado.cgc.annotations.InteractionBean;
import fr.alexpado.cgc.data.Game;
import fr.alexpado.cgc.enums.InteractionType;
import fr.alexpado.cgc.heplers.MixedResponse;
import fr.alexpado.cgc.heplers.SimpleResponse;
import fr.alexpado.cgc.services.GameService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@InteractionBean
@Component
public class GameInteractions {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameInteractions.class);

    private final GameService service;

    public GameInteractions(GameService service) {

        this.service = service;
    }

    @Interact(name = "game/create", description = "Démarre une partie")
    public MixedResponse createNewGame(InteractionHook hook, Guild guild, Member member) {

        return this.service.createGame(hook, guild, member).getState();
    }

    @Interact(name = "game/join", description = "Rejoindre une partie")
    @InteractAt(InteractionType.BUTTON)
    public ButtonResponse joinGame(Guild guild, Member member) {

        Game game = this.service.getGameWithout(guild, member);
        game.addParticipant(member);
        return game.getState();
    }

    @Interact(name = "game/leave", description = "Quitter une partie")
    @InteractAt(InteractionType.BUTTON)
    public ButtonResponse leaveGame(Guild guild, Member member) {

        Game game = this.service.getGameWith(guild, member);
        game.removeParticipant(member);

        if (game.getPlayers().isEmpty()) {
            this.service.deleteGame(guild);
            return new SimpleResponse("La partie a été annulée.", true, false);
        }

        return game.getState();
    }

    @Interact(name = "game/start", description = "Lance la partie")
    @InteractAt(InteractionType.BUTTON)
    public ButtonResponse startGame(Guild guild, Member member) {

        Game game = this.service.getGameWith(guild, member);
        game.startGame();
        return game.getState();
    }

    @Interact(name = "game/stop", description = "Arrête la partie en cours")
    public MixedResponse stopGame(Guild guild, Member member) {

        this.service.getGameWith(guild, member);
        this.service.deleteGame(guild);
        return new SimpleResponse(member.getAsMention() + " a arrêté la partie.", true, false);
    }

    @Interact(name = "game/panic", description = "Force le rafraichissement de tous les visuels en cas de désynchronisation.")
    public SlashResponse forceRefresh(Guild guild) {

        Game game = this.service.getGame(guild);
        game.refreshDisplay(true);
        return new SimpleResponse("Fait.", false, true);
    }

    @Interact(name = "game/attach", description = "Attache l'affichage du jeu à cette commande dans le cas ou le message original n'est pas disponible")
    public SlashResponse attachGame(Guild guild, InteractionHook hook) {

        Game game = this.service.getGame(guild);
        game.setHook(hook);
        return game.getState();
    }
}
