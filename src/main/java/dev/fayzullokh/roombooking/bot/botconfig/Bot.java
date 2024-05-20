package dev.fayzullokh.roombooking.bot.botconfig;

import dev.fayzullokh.roombooking.bot.bothandler.MessageHandler;
import dev.fayzullokh.roombooking.services.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;

    private final UserService userService;
    private final MessageHandler messageHandler;


    @Override
    public void onUpdateReceived(Update update) {
        BotApiMethod<Message> message;
        if (update.hasMessage()) {
            BotApiMethod<Message> response = messageHandler.handle(update);
            if (response != null) {
                send(response);
            }
        }
    }




    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    private void send(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void send(EditMessageReplyMarkup editedMessageReply) {
        try {
            execute(editedMessageReply);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void send(BotApiMethod<Message> sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }


    @PostConstruct
    public void start() {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId("912429653");
        sendMessage.setText("Bot started");
        send(sendMessage);
    }
}
