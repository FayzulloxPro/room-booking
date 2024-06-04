package dev.fayzullokh.roombooking.bot.bothandler;

import dev.fayzullokh.roombooking.bot.enums.UserBotState;
import dev.fayzullokh.roombooking.bot.utils.InlineKeyboardMarkupFactory;
import dev.fayzullokh.roombooking.dtos.BookingDto;
import dev.fayzullokh.roombooking.dtos.RoomDto;
import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.enums.BookingState;
import dev.fayzullokh.roombooking.enums.Role;
import dev.fayzullokh.roombooking.enums.State;
import dev.fayzullokh.roombooking.services.RoomService;
import dev.fayzullokh.roombooking.services.UserService;
import dev.fayzullokh.roombooking.utils.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MessageHandler implements Handler<BotApiMethod<Message>> {

    private final Map<Long, UserBotState> usersState = new HashMap<>();
    @Getter
    private final Map<Long, BookingState> bookingStateMap = new HashMap<>();
    @Getter
    private final Map<Long, BookingDto> bookingDtoMap = new HashMap<>();

    private final InlineKeyboardMarkupFactory inlineKeyboardMarkupFactory;
    private final UserService userService;
    @Getter
    private final Map<Long, State> adminState = new HashMap<>();
    @Getter
    private final Map<Long, RoomDto> roomCreateMap = new HashMap<>();
    private final RoomService roomService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    @Value("${telegram.bot.username}")
    private String botUsername;


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
            case "/cancel" -> handleCancelMessage(chatId, languageCode);
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

    private SendMessage handleCancelMessage(long chatId, String languageCode) {
        UserBotState userBotState = usersState.remove(chatId);
        BookingState bookingState = bookingStateMap.remove(chatId);
        BookingDto remove = bookingDtoMap.remove(chatId);
        State state = adminState.remove(chatId);
        RoomDto roomDto = roomCreateMap.remove(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if (!Objects.isNull(userBotState) || !Objects.isNull(bookingState) ||
                !Objects.isNull(remove) || !Objects.isNull(state) || !Objects.isNull(roomDto)) {
            sendMessage.setText("Operation canceled");
            return sendMessage;
        }
        sendMessage.setText("You are not doing any operation now");
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
                    return handleAnotherTypeState(chatId, languageCode, text);
                }
            }
            return sendMessage;
        }
        return handleAnotherTypeState(chatId, languageCode, text);
    }

    private SendMessage handleAnotherTypeState(long chatId, String languageCode, String text) {
        BookingState bookingState = bookingStateMap.get(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("HTML");
        sendMessage.setChatId(String.valueOf(chatId));
        switch (bookingState) {
            case ENTER_BEGINNING_TIME -> {
                if (isValidTimeFormat(text)) {
                    BookingDto bookingDto = bookingDtoMap.get(chatId);
                    bookingDto.setStartTime(LocalTime.parse(text));
                    sendMessage.setText("Please enter the ending time for the booking (HH:mm):");
                    bookingStateMap.put(chatId, BookingState.ENTER_ENDING_TIME);
                } else {
                    sendMessage.setText("Invalid time format. Please enter the beginning time (HH:mm):");
                }
            }
            case ENTER_ENDING_TIME -> {
                if (isValidTimeFormat(text)) {
                    BookingDto bookingDto = bookingDtoMap.get(chatId);
                    bookingDto.setEndTime(LocalTime.parse(text));
                    bookingStateMap.put(chatId, BookingState.DATE);
                    sendMessage.setText("Enter the date in format <b>dd/MM/yyyy</b>(e.g. 25/05/2023):");
                } else {
                    sendMessage.setText("Invalid time format. Please enter the ending time (HH:mm):");
                }
            }
            case DATE -> {
                try {
                    LocalDate date = LocalDate.parse(text, formatter);
                    BookingDto bookingDto = bookingDtoMap.get(chatId);
                    bookingDto.setDate(date);
                    bookingStateMap.put(chatId, BookingState.COMMENT);
                    sendMessage.setText("Leave a comment for the booking:");
                } catch (Exception e) {
                    sendMessage.setText("Date is incorrect! Send again: ");
                }
            }
            case COMMENT -> {
                BookingDto bookingDto = bookingDtoMap.get(chatId);
                bookingDto.setComment(text);
                String code = StringUtils.generateCode();
                bookingDto.setCode(code);
                try {

                    String roomNumber = roomService.getRoomById(bookingDto.getRoomId()).getRoomNumber();
                    String sb = "Do you confirm these data?\n\nBooking details: \n\n" +
                            "Room number: " + roomNumber + "\n" +
                            "Start time: " + bookingDto.getStartTime() + "\n" +
                            "End time: " + bookingDto.getEndTime() + "\n" +
                            "Date: " + bookingDto.getDate() + "\n" +
                            "Comment: " + bookingDto.getComment() + "\n" +
                            /*"Code: " + bookingDto.getCode() + "\n" +*/
                            "Note: <b>You can cancel the booking until 15 minutes before the start time and as you are creating " +
                            "a booking you will be considered responsible for the room during the booking time.</b>";
                    sendMessage.setText(sb);
                    sendMessage.setReplyMarkup(inlineKeyboardMarkupFactory.createBookingConfirmation(chatId));
                } catch (Exception e) {
                    sendMessage.setText("Something went wrong. Please try again later.");
                }
            }
            default -> new SendMessage(String.valueOf(chatId), "Unknown command");
        }
        return sendMessage;
    }

    private boolean isValidTimeFormat(String time) {
        try {
            LocalTime.parse(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
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
                /rooms - see rooms
                /cancel - cancel the operation""";
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
