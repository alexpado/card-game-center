package fr.alexpado.cgc.services;

import fr.alexpado.cgc.data.Game;
import fr.alexpado.cgc.exceptions.game.AlreadyPlayerException;
import fr.alexpado.cgc.exceptions.game.GameRunningException;
import fr.alexpado.cgc.exceptions.game.NoGameException;
import fr.alexpado.cgc.exceptions.game.NotPlayingException;
import fr.alexpado.cgc.repository.CardRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final CardRepository repository;
    private final List<Game>     games;

    public GameService(CardRepository repository) {

        this.repository = repository;
        this.games      = new ArrayList<>();
    }

    public Game getGame(Guild guild) {

        return this.findGame(guild).orElseThrow(NoGameException::new);
    }

    public Game getGameWithout(Guild guild, Member user) {
        Game game = this.getGame(guild);
        if (game.isParticipating(user)) {
            throw new AlreadyPlayerException();
        }
        return game;
    }

    public Game getGameWith(Guild guild, Member participant) {

        Game game = this.getGame(guild);

        if (!game.isParticipating(participant)) {
            throw new NotPlayingException();
        }

        return game;
    }

    public Optional<Game> findGame(Guild guild) {

        return this.games.stream().filter(game -> game.getServerId() == guild.getIdLong()).findAny();
    }

    public Game createGame(InteractionHook hook, Guild guild, Member member, long pointToWin) {

        if (this.findGame(guild).isEmpty()) {
            Game game = new Game(hook, guild, this.repository.findAll(), pointToWin);
            this.games.add(game);
            game.addParticipant(member);
            return game;
        }
        throw new GameRunningException();
    }

    public void deleteGame(Guild guild) {

        this.findGame(guild).ifPresent(this.games::remove);
    }

}
