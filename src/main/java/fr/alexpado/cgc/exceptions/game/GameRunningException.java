package fr.alexpado.cgc.exceptions.game;

import fr.alexpado.jda.interactions.interfaces.DiscordEmbeddable;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class GameRunningException extends RuntimeException implements DiscordEmbeddable {

    /**
     * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not initialized, and
     * may subsequently be initialized by a call to {@link #initCause}.
     */
    public GameRunningException() {

    }

    @Override
    public EmbedBuilder asEmbed() {

        return new EmbedBuilder()
                .setDescription("Une partie est déjà en cours sur ce serveur.")
                .setColor(Color.ORANGE);
    }

    @Override
    public boolean showToEveryone() {

        return false;
    }
}
