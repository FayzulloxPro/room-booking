package dev.fayzullokh.roombooking.bot.botconfig;

import dev.fayzullokh.roombooking.bot.bothandler.DeleteCallBackHandler;
import dev.fayzullokh.roombooking.bot.bothandler.MessageCallBackHandler;
import dev.fayzullokh.roombooking.bot.bothandler.MessageHandler;
import dev.fayzullokh.roombooking.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
@RequiredArgsConstructor
public class TelegramBotConfiguration {
    private final UserService userService;
    private final MessageHandler messageHandler;
    private final DeleteCallBackHandler deleteCallBackHandler;
    private final MessageCallBackHandler messageCallBackHandler;
    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;


    @Bean
    public Bot myTelegramBot() {
        Bot bot = new Bot(botToken, botUsername, userService, messageHandler, deleteCallBackHandler, messageCallBackHandler);
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return bot;
    }
}
