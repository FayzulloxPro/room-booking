package dev.fayzullokh.roombooking.bot.bothandler;

import dev.fayzullokh.roombooking.bot.utils.InlineKeyboardMarkupFactory;
import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MessageCallBackHandler implements Handler<BotApiMethod<Message>> {

    private final MessageHandler messageHandler;


    private final InlineKeyboardMarkupFactory inlineKeyboardMarkupFactory;
    private final UserService userService;

    @Override
    public BotApiMethod<Message> handle(Update update) {


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
        if (data.equals("logout")){
            user = userService.logout(chatId, true);
            if (Objects.isNull(user)){
                sendMessage.setText("You are not logged in");
            }else {
                sendMessage.setText("You logged out successfully");
            }
        } else if (data.equals("cancel")) {

        }
        return sendMessage;
    }

    private SendMessage handleAdminCallBack(long chatId, int messageId, String data, String systemLanguageCode) {
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
/*
            case "admin_unblock_user" -> {

            }
            case "admin_block_user" -> {

            }
            case "admin_users_list" -> {

            }*/
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
    }


}
