package fr.alexpado.cgc.interactions;

import fr.alexpado.jda.interactions.annotations.Interact;
import fr.alexpado.jda.interactions.annotations.Option;
import fr.alexpado.jda.interactions.annotations.Param;
import fr.alexpado.jda.interactions.responses.ButtonResponse;
import fr.alexpado.cgc.annotations.InteractAt;
import fr.alexpado.cgc.annotations.InteractionBean;
import fr.alexpado.cgc.data.Game;
import fr.alexpado.cgc.data.Player;
import fr.alexpado.cgc.enums.InteractionType;
import fr.alexpado.cgc.heplers.SimpleResponse;
import fr.alexpado.cgc.services.GameService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.stereotype.Component;

@Component
@InteractionBean
public class RoundInteractions {

    private final GameService service;

    public RoundInteractions(GameService service) {

        this.service = service;
    }

    @Interact(
            name = "round/winner", description = "Choisir un vainqueur", options = {
            @Option(name = "id", description = "id", type = OptionType.INTEGER, required = true)
    }
    )
    @InteractAt(InteractionType.BUTTON)
    public ButtonResponse selectWinner(Guild guild, Member member, @Param("id") long winnerId) {

        UserSnowflake winner = UserSnowflake.fromId(winnerId);
        Game          game   = this.service.getGameWith(guild, member);
        Player        player = game.getPlayer(member);

        if (!player.equals(game.getBoss())) {
            return new SimpleResponse("Vous ne pouvez pas voter à la place du boss.", false, true);
        }

        game.selectWinner(winner);
        return game.getState();
    }

    @Interact(name = "round/next", description = "Passer au tour suivant")
    @InteractAt(InteractionType.BUTTON)
    public ButtonResponse nextRound(Guild guild, Member member) {

        Game game = this.service.getGameWith(guild, member);

        if (!game.nextTurn()) {
            this.service.deleteGame(guild);
        }
        return game.getState();
    }

}
