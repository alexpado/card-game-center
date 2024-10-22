package fr.alexpado.cgc;

import fr.alexpado.cgc.services.LimiteLimiteBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {


    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

    public Application(LimiteLimiteBot bot) {
        LOGGER.info("Starting bot...");
        bot.login();
    }

}
