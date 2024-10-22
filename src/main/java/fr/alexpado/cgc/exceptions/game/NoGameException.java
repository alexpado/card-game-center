package fr.alexpado.cgc.exceptions.game;

import fr.alexpado.jda.interactions.interfaces.DiscordEmbeddable;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class NoGameException extends RuntimeException implements DiscordEmbeddable {

    /**
     * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     */
    public NoGameException() {

    }

    @Override
    public EmbedBuilder asEmbed() {

        return new EmbedBuilder()
                .setDescription("Aucune partie n'est en cours sur ce serveur")
                .setColor(Color.RED);
    }

    @Override
    public boolean showToEveryone() {

        return false;
    }
}
