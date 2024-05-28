package dev.fayzullokh.roombooking.bot.bothandler;

import dev.fayzullokh.roombooking.bot.enums.UserBotState;
import dev.fayzullokh.roombooking.bot.utils.InlineKeyboardMarkupFactory;
import dev.fayzullokh.roombooking.dtos.RoomDto;
import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.enums.Role;
import dev.fayzullokh.roombooking.enums.State;
import dev.fayzullokh.roombooking.services.RoomService;
import dev.fayzullokh.roombooking.services.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MessageHandler implements Handler<BotApiMethod<Message>> {

    private final Map<Long, UserBotState> usersState = new HashMap<>();


    private final InlineKeyboardMarkupFactory inlineKeyboardMarkupFactory;
    private final UserService userService;
    @Getter
    private final Map<Long, State> adminState = new HashMap<>();
    @Getter
    private final Map<Long, RoomDto> roomCreateMap = new HashMap<>();
    private final RoomService roomService;

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
            case "/admin" -> handleAdminMessage(chatId, languageCode);
            case "/profile" -> handleProfileMessage(chatId, languageCode);
            case "/rooms" -> handleRoomsMessage(chatId, languageCode);
            default -> {
                SendMessage handledPlainText = handlePlainText(chatId, languageCode, text);
                /*if (handledPlainText.getText().contains("Login successful.")) {
                    CompletableFuture.runAsync(()->{
                        DeleteMessage deleteMessage = new DeleteMessage();
                        Integer messageId = update.getMessage().getMessageId();
                        deleteMessage.setMessageId(messageId);
                        deleteMessage.setChatId(String.valueOf(chatId));

                    })
                }*/  // this is to delete after successful login
                yield handledPlainText;
            }
        };
        return sendMessage;
    }

    private SendMessage handleRoomsMessage(long chatId, String languageCode) {
        SendMessage sendMessage = new SendMessage();
        Page<Room> rooms = roomService.getAllRooms(chatId, true);
        sendMessage.setChatId(String.valueOf(chatId));
        if (rooms.getTotalElements() == 0) {
            sendMessage.setText("There are no rooms");
            return sendMessage;
        }
        InlineKeyboardMarkup factoryRoomsList = inlineKeyboardMarkupFactory.createRoomsList(rooms, chatId, languageCode);
        sendMessage.setReplyMarkup(factoryRoomsList);
        sendMessage.setText("Rooms list: ");
        return sendMessage;
    }

    private SendMessage handleProfileMessage(long chatId, String languageCode) {
        User userByChatId = userService.getUserByChatId(chatId, true);
        if (Objects.isNull(userByChatId)) {
            return new SendMessage(String.valueOf(chatId), "You are not logged in.\nSend /login to login");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Username:       ").append("<b>").append(userByChatId.getUsername()).append("\n").append("</b>")
                .append("Name:           ").append("<b>").append(userByChatId.getFirstName() != null ? userByChatId.getFirstName() : "unknown").append("</b>").append(" ").append(userByChatId.getLastName() != null ? userByChatId.getLastName() : "").append("\n")
                .append("Last name:      ").append("<b>").append(userByChatId.getLastName() != null ? userByChatId.getLastName() : "unknown").append("</b>").append("\n")
                .append("Phone number:   ").append("<b>").append(userByChatId.getPhone() != null ? userByChatId.getPhone() : "unknown").append("</b>").append("\n")
                .append("Email:          ").append("<b>").append(userByChatId.getEmail() != null ? userByChatId.getEmail() : "unknown").append("</b>").append("\n");
        if (!userByChatId.getRole().equals(Role.USER)) {
            sb.append("Role:           ").append("<b>").append(userByChatId.getRole().equals(Role.ADMIN) ? "Admin" : "Super admin").append("</b>").append("\n");
        }
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), sb.toString());
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    private SendMessage handleAdminMessage(long chatId, String languageCode) {
        User userByChatId = userService.getUserByChatId(chatId, true);
        if (Objects.isNull(userByChatId)) {
            return new SendMessage(String.valueOf(chatId), "You are not logged in.\nSend /login to login");
        }
        if (userByChatId.getRole().equals(Role.USER)) {
            return new SendMessage(String.valueOf(chatId), "Unknown command");
        }
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkupFactory.adminMenu(chatId, languageCode);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(keyboardMarkup);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Admin menu: ");
        return sendMessage;
    }

    private SendMessage handleLogoutMessage(long chatId, String languageCode) {
        User userByChatId = userService.getUserByChatId(chatId, true);
        if (Objects.isNull(userByChatId)) {
            return new SendMessage(String.valueOf(chatId), "You are not logged in");
        }
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkupFactory.logout(languageCode);
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "Are you sure you want to log out account <b>" + userByChatId.getUsername() + "</b>?");
        sendMessage.setReplyMarkup(keyboardMarkup);
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    private SendMessage handlePlainText(long chatId, String languageCode, String text) {
        UserBotState userBotState = usersState.get(chatId);

        String userChatId = String.valueOf(chatId);
        if (!Objects.isNull(userBotState) && userBotState.equals(UserBotState.LOGIN)) {
            //process of login
            User user = userService.login(chatId, text, true);
            if (Objects.isNull(user)) {
                SendMessage sendMessage = new SendMessage(userChatId, "Bad credentials. Send username and password below format\n<b>username#password</b>\"");
                sendMessage.setParseMode("HTML");
                return sendMessage;
            }
            usersState.remove(chatId);  // remove user's state
            SendMessage sendMessage = new SendMessage(userChatId, "Login successful. Welcome <b>" + user.getUsername() + "</b>");
            sendMessage.setParseMode("HTML");
            return sendMessage;
        }
        User userByChatId = userService.getUserByChatId(chatId, true);
        if (Objects.isNull(userByChatId)) {
            return new SendMessage(userChatId, "No such command");
        }
        if (!userByChatId.getRole().equals(Role.USER)) {
            State state = adminState.get(chatId);
            if (Objects.isNull(state)) {
                return new SendMessage(String.valueOf(chatId), "No such command");
            }
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            switch (state) {
                case ROOM_NUMBER -> {
                    handleCreateRoom(chatId, text, sendMessage, false);
                }
                case DESCRIPTION -> {
                    handleInDescription(chatId, text, sendMessage, false);
                }
                case MAX_SEATS -> {

                    handleMaxSeats(chatId, text, sendMessage, false);
                }
                case MIN_SEATS -> {
                    handleMinSeats(chatId, text, sendMessage, false);
                }

                case SINGLE_ROOM_NUMBER -> {
                    handleCreateRoom(chatId, text, sendMessage, true);
                }
                case SINGLE_DESCRIPTION -> {
                    handleInDescription(chatId, text, sendMessage, true);
                }
                case SINGLE_MAX_SEATS -> {
                    handleMaxSeats(chatId, text, sendMessage, true);
                }
                case SINGLE_MIN_SEATS -> {
                    handleMinSeats(chatId, text, sendMessage, true);
                }
                default -> {
                    sendMessage.setText("Something went wrong");
                }
            }
            return sendMessage;
        }
        return null;
    }


    private void handleMinSeats(long chatId, String text, SendMessage sendMessage, boolean isSingle) {
        try {
            short minSeats = Short.parseShort(text);
            RoomDto roomDto = roomCreateMap.get(chatId);
            if (minSeats < 1) {
                sendMessage.setText("Maximum number of seats can not be less than 1. Send maximum number of seats:");
                return;
            }
            roomDto.setMinSeats(minSeats);
            adminState.put(chatId, State.MIN_SEATS);
            String string = "Do you confirm these data?\n\n";
            String sb = "Room number: " + roomDto.getRoomNumber() + "\n" +
                    "Room description: " + roomDto.getDescription() + "\n" +
                    "Max seats: " + roomDto.getMaxSeats() + "\n" +
                    "Min seats: " + roomDto.getMinSeats() + "\n";
            sendMessage.setText(string + sb);
            sendMessage.setReplyMarkup(inlineKeyboardMarkupFactory.createRoomConfirmation(chatId));
        } catch (NumberFormatException e) {
            sendMessage.setText("Wrong number of max seats\nSend again:");
        }
    }

    private void handleMaxSeats(long chatId, String text, SendMessage sendMessage, boolean isSingle) {
        try {
            short maxSeats = Short.parseShort(text);
            RoomDto roomDto = roomCreateMap.get(chatId);
            if (maxSeats < 1) {
                sendMessage.setText("Maximum number of seats can not be less than 1. \nSend maximum number of seats:");
                return;
            }
            roomDto.setMaxSeats(maxSeats);
            if (!isSingle) {
                adminState.put(chatId, State.MIN_SEATS);
                sendMessage.setText("Max seats accepted\nSend minimum number of seats:");
            } else {
                String string = "Do you confirm these data?\n\n";
                String sb = "Room number: " + roomDto.getRoomNumber() + "\n" +
                        "Room description: " + roomDto.getDescription() + "\n" +
                        "Max seats: " + roomDto.getMaxSeats() + "\n" +
                        "Min seats: " + roomDto.getMinSeats() + "\n";
                sendMessage.setText(string + sb);
                sendMessage.setReplyMarkup(inlineKeyboardMarkupFactory.createRoomConfirmation(chatId));
            }
        } catch (NumberFormatException e) {
            sendMessage.setText("Wrong number of max seats\nSend again:");
        }
    }

    private void handleInDescription(long chatId, String text, SendMessage sendMessage, boolean isSingle) {
        RoomDto roomDto = roomCreateMap.get(chatId);
        if (text.isBlank() || text.isEmpty()) {
            sendMessage.setText("Room description can not be blank. Send room description:");
            return;
        }
        if (!isSingle) {

            roomDto.setDescription(text);
            adminState.put(chatId, State.MAX_SEATS);
            sendMessage.setText("Description accepted\nSend room max seats:");
        } else {
            roomDto.setDescription(text);
            String string = "Do you confirm these data?\n\n";
            String sb = "Room number: " + roomDto.getRoomNumber() + "\n" +
                    "Room description: " + roomDto.getDescription() + "\n" +
                    "Max seats: " + roomDto.getMaxSeats() + "\n" +
                    "Min seats: " + roomDto.getMinSeats() + "\n";
            sendMessage.setText(string + sb);
            sendMessage.setReplyMarkup(inlineKeyboardMarkupFactory.createRoomConfirmation(chatId));
        }
    }

    private void handleCreateRoom(long chatId, String text, SendMessage sendMessage, boolean isSingle) {
        RoomDto roomDto = roomCreateMap.get(chatId);
        if (roomDto == null) {
            RoomDto dto = new RoomDto();
            roomCreateMap.put(chatId, dto);
            if (text.isBlank() || text.isEmpty()) {
                sendMessage.setText("Room number can not be blank. Send room number:");
                return;
            }
            dto.setRoomNumber(text);
            adminState.put(chatId, State.DESCRIPTION);
            sendMessage.setText("Send room description:");
            return;
        }
        if (text.isBlank() || text.isEmpty()) {
            sendMessage.setText("Room description can not be blank. Send room description:");
            return;
        }
        if (!isSingle) {
            roomDto.setRoomNumber(text);
            adminState.put(chatId, State.DESCRIPTION);
            sendMessage.setText("Room number accepted\nSend room description:");
        } else {
            roomDto.setRoomNumber(text);
            String string = "Do you confirm these data?\n\n";
            String sb = "Room number: " + roomDto.getRoomNumber() + "\n" +
                    "Room description: " + roomDto.getDescription() + "\n" +
                    "Max seats: " + roomDto.getMaxSeats() + "\n" +
                    "Min seats: " + roomDto.getMinSeats() + "\n";
            sendMessage.setText(string + sb);
            sendMessage.setReplyMarkup(inlineKeyboardMarkupFactory.createRoomConfirmation(chatId));
        }
    }

    private SendMessage handleLoginMessage(long chatId, String languageCode) {
        User userByChatId = userService.getUserByChatId(chatId, true);
        if (!Objects.isNull(userByChatId)) {
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "You are already logged in to <b>" + userByChatId.getUsername() + "</b>.\nSend /logout to logout from current account");
            sendMessage.setParseMode("HTML");
            return sendMessage;
        }
        usersState.put(chatId, UserBotState.LOGIN);
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "Send your username and password with format below:\n" +
                "\n<b>username#password</b>");
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }


    private SendMessage handleHelpMessage(long chatId, String languageCode) {
        String text = """
                Here is the list of commands:\s

                /start - start the bot
                /help - get help
                /login - login into account
                /logout - logout from account
                /profile - see profile
                /rooms - see rooms""";
        return new SendMessage(String.valueOf(chatId), text);
    }

    private SendMessage handleStartMessage(long chatId, Update update) {

        org.telegram.telegrambots.meta.api.objects.User from = update.getMessage().getFrom();
        User user = userService.getUserByChatId(chatId, true);
        if (Objects.isNull(user)) {
            return new SendMessage(String.valueOf(chatId), "Login to use bot. \nSend /login");
        }
        return new SendMessage(String.valueOf(chatId), "Welcome " + user.getUsername());
    }


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
