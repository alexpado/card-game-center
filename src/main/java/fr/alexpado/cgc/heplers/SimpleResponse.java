package fr.alexpado.cgc.heplers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.util.Collections;
import java.util.function.Consumer;

public class SimpleResponse implements MixedResponse {

    private final EmbedBuilder builder;
    private final boolean      edit;
    private final boolean      ephemeral;

    public SimpleResponse(CharSequence message, boolean edit, boolean ephemeral) {

        this(new EmbedBuilder().setDescription(message), edit, ephemeral);
    }

    public SimpleResponse(EmbedBuilder builder, boolean edit, boolean ephemeral) {

        this.builder   = builder;
        this.edit      = edit;
        this.ephemeral = ephemeral;
    }

    @Override
    public boolean shouldEditOriginalMessage() {

        return this.edit;
    }

    @Override
    public Consumer<MessageRequest<?>> getHandler() {

        return (amb) -> {
            amb.setEmbeds(this.builder.build());
            amb.setComponents(Collections.emptyList());
        };
    }

    @Override
    public boolean isEphemeral() {

        return this.ephemeral;
    }

}
