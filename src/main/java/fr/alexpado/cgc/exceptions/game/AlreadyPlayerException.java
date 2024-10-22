package fr.alexpado.cgc.exceptions.game;

import fr.alexpado.jda.interactions.interfaces.DiscordEmbeddable;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class AlreadyPlayerException extends RuntimeException implements DiscordEmbeddable {

    /**
     * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not initialized, and
     * may subsequently be initialized by a call to {@link #initCause}.
     */
    public AlreadyPlayerException() {

    }

    @Override
    public EmbedBuilder asEmbed() {

        return new EmbedBuilder()
                .setDescription("Vous avez déjà rejoins cette partie.")
                .setColor(Color.ORANGE);
    }

    @Override
    public boolean showToEveryone() {

        return false;
    }
}
