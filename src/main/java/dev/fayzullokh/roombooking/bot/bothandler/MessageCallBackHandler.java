package dev.fayzullokh.roombooking.bot.bothandler;

import dev.fayzullokh.roombooking.bot.utils.InlineKeyboardMarkupFactory;
import dev.fayzullokh.roombooking.config.StateManagement;
import dev.fayzullokh.roombooking.dtos.RoomDto;
import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.enums.Role;
import dev.fayzullokh.roombooking.enums.State;
import dev.fayzullokh.roombooking.services.RoomService;
import dev.fayzullokh.roombooking.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MessageCallBackHandler implements Handler<BotApiMethod<Message>> {

    private final MessageHandler messageHandler;


    private final InlineKeyboardMarkupFactory inlineKeyboardMarkupFactory;
    private final UserService userService;
    private final RoomService roomService;

    private final StateManagement stateManagement;

    @Override
    public BotApiMethod<Message> handle(Update update) {
        Map<Long, State> adminState = messageHandler.getAdminState();

        CallbackQuery callback = update.getCallbackQuery();
        long chatId = callback.getFrom().getId();
        int messageId = callback.getMessage().getMessageId();
        String data = callback.getData();
        String languageCode = callback.getFrom().getLanguageCode();
        User user = userService.getUserByChatId(chatId, true);
        String chatIdS = String.valueOf(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatIdS);
        if (user == null) {
            sendMessage.setText("You are not logged in");
            return sendMessage;
        }
        switch (data) {
            case "logout" -> {
                user = userService.logout(chatId, true);
                if (Objects.isNull(user)) {
                    sendMessage.setText("You are not logged in");
                } else {
                    sendMessage.setText("You logged out successfully");
                }
            }
            case "rooms" -> {
                if (user.getRole().equals(Role.USER)) {
                    sendMessage.setText("Unknown command");
                    return sendMessage;
                }
                Page<Room> rooms = roomService.getAllRooms(chatId, true);
                if (rooms.getTotalElements() == 0) {
                    sendMessage.setText("There are no rooms");
                    return sendMessage;
                }
                InlineKeyboardMarkup factoryRoomsList = inlineKeyboardMarkupFactory.createRoomsList(rooms, chatId, languageCode);
                sendMessage.setReplyMarkup(factoryRoomsList);
                sendMessage.setText("Rooms list: ");
                return sendMessage;
            }
            case "users" -> {
                if (user.getRole().equals(Role.USER)) {
                    sendMessage.setText("Unknown command");
                    return sendMessage;
                }
            }
            case "add_room" -> {
                if (user.getRole().equals(Role.USER)) {
                    sendMessage.setText("Unknown command");
                    return sendMessage;
                }
                messageHandler.getRoomCreateMap().put(chatId, new RoomDto());
                adminState.put(chatId, State.ROOM_NUMBER);
                sendMessage.setText("Enter room number: ");
                return sendMessage;
            }
            case "confirm_room_creation" -> {
                RoomDto roomDto = messageHandler.getRoomCreateMap().get(chatId);
                adminState.remove(chatId);
                if (roomDto == null) {
                    sendMessage.setText("Unknown command");
                    return sendMessage;
                }
                try {
                    roomService.create(roomDto);
                    sendMessage.setText("Room created successfully");
                } catch (Exception e) {
                    sendMessage.setText("Error while creating room. Try again");
                }
                return sendMessage;
            }
            default -> {
                handleOtherCallBack(chatId, messageId, data, languageCode, sendMessage);
            }
        }
        return sendMessage;
    }

    private void handleOtherCallBack(long chatId, int messageId, String data, String languageCode, SendMessage sendMessage) {

        if (data.startsWith("update_create_room")) {
            createRoomUpdateFields(chatId, messageId, data, languageCode, sendMessage);
            return;
        }

        if (data.startsWith("ROOM_ID_#")) {
            String[] split = data.split("#");
            Long roomId = Long.parseLong(split[1]);
            RoomDto roomDto = roomService.findById(roomId);
            StringBuilder sb = new StringBuilder();
            sb.append("Room number: ").append(roomDto.getRoomNumber()).append("\n");
            sb.append("Description: ").append(roomDto.getDescription()).append("\n");
            sb.append("Max seats: ").append(roomDto.getMaxSeats()).append("\n");
            sb.append("Min seats: ").append(roomDto.getMinSeats()).append("\n");
            /*sb.append("Open time: ").append(roomDto.getOpenTime()).append("\n");
            sb.append("Close time: ").append(roomDto.getCloseTime()).append("\n");*/
            InlineKeyboardMarkup keyboardMarkup = inlineKeyboardMarkupFactory.roomMenu(chatId, roomId, languageCode);
            sendMessage.setText(sb.toString());
            sendMessage.setReplyMarkup(keyboardMarkup);
        } else if (data.contains("_PAGE#")) {
            String[] split = data.split("#");
            int page = Integer.parseInt(split[1]);
            Page<Room> rooms = roomService.getAllRooms(page, chatId, true);
            if (rooms.getTotalElements() == 0) {
                sendMessage.setText("There are no rooms");
                return;
            }
            InlineKeyboardMarkup factoryRoomsList = inlineKeyboardMarkupFactory.createRoomsList(rooms, chatId, languageCode);
            sendMessage.setReplyMarkup(factoryRoomsList);
            sendMessage.setText("Rooms list: ");
        }
    }

    private void createRoomUpdateFields(long chatId, int messageId, String data, String languageCode, SendMessage sendMessage) {
        Map<Long, State> adminState = messageHandler.getAdminState();
        sendMessage.setChatId(String.valueOf(chatId));
        switch (data) {
            case "update_create_room_number" -> {
                adminState.put(chatId, State.SINGLE_ROOM_NUMBER);
                sendMessage.setText("Enter room number: ");
            }
            case "update_create_room_description" -> {
                adminState.put(chatId, State.SINGLE_DESCRIPTION);
                sendMessage.setText("Enter room description: ");
            }
            case "update_create_room_max_seats" -> {
                adminState.put(chatId, State.SINGLE_MAX_SEATS);
                sendMessage.setText("Enter maximum number of seats: ");
            }
            case "update_create_room_min_seats" -> {
                adminState.put(chatId, State.SINGLE_MIN_SEATS);
                sendMessage.setText("Enter minimum number of seats: ");
            }
            default -> sendMessage.setText("Something went wrong");
        }
    }

    /*private SendMessage handleAdminCallBack(long chatId, int messageId, String data, String systemLanguageCode) {
        switch (data) {
            case "admin_advertisement" -> {
                return sendAdvertisementAndPutAdminState(chatId, messageId, systemLanguageCode);
            }
            case "admin_search_user" -> {
                return setAdminStateToSearchAndSendMessage(chatId, systemLanguageCode);
            }
            case "admin_count_users" -> {
                return sendCountUsersAndPutAdminState(chatId, systemLanguageCode);
            }
//            case "admin_users_list" -> {
//                return new SendDocument();
//            }
*//*
            case "admin_unblock_user" -> {

            }
            case "admin_block_user" -> {

            }
            case "admin_users_list" -> {

            }*//*
            default -> {
                if (data.startsWith("admin_set")) {
                    return setNewRoleToUser(chatId, messageId, data, systemLanguageCode);
                } else if (data.startsWith("admin_send_message_to_user")) {
                    adminService.updateStatus(String.valueOf(chatId), AdminStatus.SEND_MESSAGE_TO_USER);
                    adminService.addChatIdToSendMessage(String.valueOf(chatId), data.split("&")[1]);
                    return new SendMessage(String.valueOf(chatId),
                            languageService.getLocalizedMessage("enter.message", systemLanguageCode));
                } else if (data.startsWith("admin_as_specific_user")) {
                    String userId = data.split("&")[1];
//                    userService.setSpecificUser(userId);
                    User user = userService.get(Long.valueOf(userId));
                    user.setSpecificUser(true);
                    userService.update(Long.valueOf(userId), user);
                    return new SendMessage(String.valueOf(chatId),
                            "Maxsus user etib belgilandi");
                } else if (data.startsWith("admin_as_ordinary_user")) {
                    String userId = data.split("&")[1];
                    User user = userService.get(Long.valueOf(userId));
                    user.setSpecificUser(false);
                    userService.update(Long.valueOf(userId), user);
//                    userService.setOrdinaryUser(userId);
                    return new SendMessage(String.valueOf(chatId),
                            "Oddiy user etib belgilandi");
                }
                return new SendMessage(String.valueOf(chatId),
                        languageService.getLocalizedMessage("something.wrong", systemLanguageCode));
            }
        }
    }

    private SendMessage sendCountUsersAndPutAdminState(long chatId, String systemLanguageCode) {
        adminService.updateStatus(String.valueOf(chatId), AdminStatus.NONE);
        int number = userService.getNumberOfUsers();
        return new SendMessage(String.valueOf(chatId),
                languageService.getLocalizedMessage("count.users", systemLanguageCode).formatted(number));
    }

    private SendMessage setNewRoleToUser(long chatId, int messageId, String data, String systemLanguageCode) {
        String[] split = data.split("&");
        User user = userService.get(Long.valueOf(split[1]));
        if (data.startsWith("admin_set_user")) {
            user.setRole(Role.USER);
            adminService.removeAdmin(String.valueOf(user.getId()));
        } else if (data.startsWith("admin_set_admin")) {
            user.setRole(Role.ADMIN);
            adminService.updateStatus(String.valueOf(user.getId()), AdminStatus.NONE);
        } else if (data.startsWith("admin_set_super_admin")) {
            user.setRole(Role.SUPER_ADMIN);
            adminService.updateStatus(String.valueOf(user.getId()), AdminStatus.NONE);
        }
        userService.update(user.getId(), user);
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "<a href=\"tg://user?id=%s\">%s    .</a>\n\n".formatted(user.getId(), user.getFirstName() + "  \n") +
                languageService.getLocalizedMessage("role.changed", systemLanguageCode).formatted(user.getRole().name()));
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(inlineKeyboardMarkupFactory.userMenuForAdmin(systemLanguageCode, String.valueOf(user.getId()), user));
        return sendMessage;
    }

    private SendMessage setAdminStateToSearchAndSendMessage(long chatId, String systemLanguageCode) {
        adminService.updateStatus(String.valueOf(chatId), AdminStatus.SEARCH_USER);
        return new SendMessage(String.valueOf(chatId),
                languageService.getLocalizedMessage("send.user.id", systemLanguageCode));
    }

    private SendMessage sendAdvertisementAndPutAdminState(long chatId, int messageId, String systemLanguageCode) {
        adminService.updateStatus(String.valueOf(chatId), AdminStatus.SEND_ADVERTISEMENT);
        return new SendMessage(String.valueOf(chatId),
                languageService.getLocalizedMessage("send.advertisement", systemLanguageCode));
    }

    private SendMessage changeLanguage(long chatId, String data, String userSystemLanguage) {
        String code = data.substring(5);
        messageHandler.changeUserLanguageState(chatId, code);
        Language language = languageService.getLanguage(code);
        return new SendMessage(String.valueOf(chatId), languageService.getLocalizedMessage("language.changed.to", userSystemLanguage).formatted(language.getName(userSystemLanguage)));
    }*/


}
