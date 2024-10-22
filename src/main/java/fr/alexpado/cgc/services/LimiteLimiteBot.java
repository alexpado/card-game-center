package fr.alexpado.cgc.services;

import fr.alexpado.jda.interactions.InteractionExtension;
import fr.alexpado.jda.interactions.annotations.Interact;
import fr.alexpado.jda.interactions.entities.DispatchEvent;
import fr.alexpado.jda.interactions.impl.interactions.button.ButtonInteractionTargetImpl;
import fr.alexpado.jda.interactions.impl.interactions.slash.SlashInteractionTargetImpl;
import fr.alexpado.jda.interactions.interfaces.interactions.InteractionPreprocessor;
import fr.alexpado.jda.interactions.interfaces.interactions.button.ButtonInteractionTarget;
import fr.alexpado.jda.interactions.interfaces.interactions.slash.SlashInteractionTarget;
import fr.alexpado.jda.interactions.meta.InteractionMeta;
import fr.alexpado.jda.interactions.meta.OptionMeta;
import fr.alexpado.cgc.annotations.InteractAt;
import fr.alexpado.cgc.annotations.InteractionBean;
import fr.alexpado.cgc.enums.InteractionType;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class LimiteLimiteBot extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LimiteLimiteBot.class);

    private final ListableBeanFactory  listableBeanFactory;
    private final InteractionExtension extension;

    @Value("${discord.bot.token}")
    private String token;

    public LimiteLimiteBot(ListableBeanFactory listableBeanFactory) {

        this.listableBeanFactory = listableBeanFactory;
        this.extension           = new InteractionExtension();

        this.extension.getSlashContainer()
                      .addClassMapping(InteractionHook.class, (event, opt) -> () -> event.getInteraction().getHook());
        this.extension.getButtonContainer()
                      .addClassMapping(InteractionHook.class, (event, opt) -> () -> event.getInteraction().getHook());
    }

    private void hook(JDA jda, Supplier<CommandListUpdateAction> action) {

        this.listableBeanFactory.getBeansWithAnnotation(InteractionBean.class)
                                .values()
                                .forEach(this::register);

        jda.addEventListener(this.extension);
        this.extension.useDefaultMapping();
        this.extension.getSlashContainer().upsertCommands(action.get()).complete();
    }

    public void hook(JDA jda) {

        this.hook(jda, jda::updateCommands);
    }

    public void hook(Guild guild) {

        this.hook(guild.getJDA(), guild::updateCommands);

        this.extension.registerPreprocessor(new InteractionPreprocessor() {

            @Override
            public <T extends Interaction> boolean mayContinue(@NotNull DispatchEvent<T> dispatchEvent) {

                Guild g = dispatchEvent.getInteraction().getGuild();
                return g == null || guild.getIdLong() == g.getIdLong();
            }

            @Override
            public <T extends Interaction> Optional<Object> preprocess(@NotNull DispatchEvent<T> dispatchEvent) {

                return Optional.empty();
            }
        });
    }

    public void register(Object obj) {

        for (Method method : obj.getClass().getMethods()) {
            if (method.isAnnotationPresent(Interact.class)) {
                Interact annotation = method.getAnnotation(Interact.class);

                List<InteractionType> types = new ArrayList<>();

                if (method.isAnnotationPresent(InteractAt.class)) {
                    InteractAt at = method.getAnnotation(InteractAt.class);
                    types.addAll(Arrays.asList(at.value()));
                } else {
                    types.addAll(Arrays.asList(InteractionType.values()));
                }

                List<OptionMeta> options = Arrays.stream(annotation.options()).map(OptionMeta::new).toList();

                if (types.contains(InteractionType.SLASH)) {
                    InteractionMeta slashMeta = new InteractionMeta(
                            annotation.name(),
                            annotation.description(),
                            annotation.target(),
                            options,
                            annotation.hideAsSlash(),
                            annotation.defer(),
                            annotation.shouldReply()
                    );

                    SlashInteractionTarget slash = new SlashInteractionTargetImpl(obj, method, slashMeta);
                    this.extension.getSlashContainer().register(slash);
                }

                if (types.contains(InteractionType.BUTTON)) {
                    InteractionMeta buttonMeta = new InteractionMeta(
                            annotation.name(),
                            annotation.description(),
                            annotation.target(),
                            options,
                            annotation.hideAsButton(),
                            annotation.defer(),
                            annotation.shouldReply()
                    );
                    ButtonInteractionTarget button = new ButtonInteractionTargetImpl(obj, method, buttonMeta);
                    this.extension.getButtonContainer().register(button);
                }
            }
        }
    }

    public void login() {

        try {
            JDABuilder builder = JDABuilder.createLight(this.token);
            builder.addEventListeners(this);
            builder.build();
        } catch (Exception e) {
            LOGGER.warn("Unable to connect to Discord. The token provided is probably invalid.", e);

            Sentry.withScope(scope -> {
                scope.setLevel(SentryLevel.FATAL);
                Sentry.captureException(e);
            });
        }

    }


    @Override
    public void onReady(@NotNull ReadyEvent event) {

        User self = event.getJDA().getSelfUser();
        LOGGER.info("== == == == == == == Limite Limite Bot == == == == == == ==");
        LOGGER.info("Logged in as '{} ({})'", self.getName(), self.getId());
        this.hook(event.getJDA());
        LOGGER.info("Successfully hooked to Discord");
        LOGGER.info("== == == == == == == == == == == == == == == == == ==");
    }
}
