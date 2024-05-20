package dev.fayzullokh.roombooking.bot.bothandler;

import dev.fayzullokh.roombooking.bot.enums.UserBotState;
import dev.fayzullokh.roombooking.bot.utils.InlineKeyboardMarkupFactory;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MessageHandler implements Handler<BotApiMethod<Message>> {

    private final Map<Long, UserBotState> usersState = new HashMap<>();


    private final InlineKeyboardMarkupFactory inlineKeyboardMarkupFactory;
    private final UserService userService;


    @Override
    public BotApiMethod<Message> handle(Update update) {
        Message message = update.getMessage();
        BotApiMethod<Message> messageBotApiMethod;
        if (message.hasText()) {
            messageBotApiMethod = handleText(update);
        } else if (message.hasContact()) {
            messageBotApiMethod = handleContact(update);
        } else {
            messageBotApiMethod = handleUnknownUpdates(update);
        }
        return messageBotApiMethod;
    }

    public BotApiMethod<Message> handleText(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        String text = message.getText();
        String languageCode = message.getFrom().getLanguageCode();
        SendMessage sendMessage = switch (text) {
            case "/start" -> handleStartMessage(chatId, update);
            case "/help" -> handleHelpMessage(chatId, languageCode);
            case "/login" -> handleLoginMessage(chatId, languageCode);
            case "/logout" -> handleLogoutMessage(chatId, languageCode);
//            case "/admin" -> new SendMessage();
            default -> handlePlainText(chatId, languageCode, text);
        };
        return sendMessage;
    }

    private SendMessage handleLogoutMessage(long chatId, String languageCode) {
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkupFactory.logout(languageCode);
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "Are you sure you want to log out?");
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }

    private SendMessage handlePlainText(long chatId, String languageCode, String text) {
        UserBotState userBotState = usersState.get(chatId);

        String userChatId = String.valueOf(chatId);
        if (!Objects.isNull(userBotState) && userBotState.equals(UserBotState.LOGIN)) {
            //process of login
            User user = userService.login(chatId, text, true);
            if (Objects.isNull(user)) {
                return new SendMessage(userChatId, "Bad credentials");
            }
            usersState.remove(chatId);  // remove user's state
            return new SendMessage(userChatId, "Login successful. Welcome " + user.getUsername());
        }
        return new SendMessage(userChatId, "No such command");
    }

    private SendMessage handleLoginMessage(long chatId, String languageCode) {
        usersState.put(chatId, UserBotState.LOGIN);
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "Send your username and password with format below:\n" +
                "\n<b>username#password</b>");
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }


    private SendMessage handleHelpMessage(long chatId, String languageCode) {
        return new SendMessage(String.valueOf(chatId), "Help message");
    }

    private SendMessage handleStartMessage(long chatId, Update update) {

        org.telegram.telegrambots.meta.api.objects.User from = update.getMessage().getFrom();
        User user = userService.getUserByChatId(chatId, true);
        if (Objects.isNull(user)) {
            return new SendMessage(String.valueOf(chatId), "Login to use bot. \nSend /login");
        }
        return new SendMessage(String.valueOf(chatId), "Welcome " + user.getUsername());
    }

    /*private User createUser(long chatId, Update update) {
        org.telegram.telegrambots.meta.api.objects.User from = update.getMessage().getFrom();
        User user = User.builder().id(chatId).firstName(from.getFirstName()).lastName(from.getLastName()).username(from.getUserName()).systemLanguageCode(from.getLanguageCode()).build();
        return userService.create(user);
    }*/

    private BotApiMethod<Message> handleContact(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        String phoneNumber = message.getContact().getPhoneNumber();
        System.out.println("phoneNumber = " + phoneNumber);
        String firstName = message.getContact().getFirstName();
        System.out.println("firstName = " + firstName);
        String lastName = message.getContact().getLastName();
        System.out.println("lastName = " + lastName);
        return null;
    }

    private BotApiMethod<Message> handleUnknownUpdates(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Something went wrong");
        return sendMessage;
    }
}
