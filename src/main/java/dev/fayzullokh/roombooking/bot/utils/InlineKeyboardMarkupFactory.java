package dev.fayzullokh.roombooking.bot.utils;

import dev.fayzullokh.roombooking.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InlineKeyboardMarkupFactory {

    private final UserService userService;
    private int PAGE_SIZE = 15;
    private int ROW_SIZE = 3;


    public InlineKeyboardMarkup logout(String languageCode) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> twoDimensionList = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(
                getInlineButton("Yes",
                        "logout"));
        row.add(
                getInlineButton("Cancel",
                        "delete"));
        twoDimensionList.add(row);
        inlineKeyboardMarkup.setKeyboard(twoDimensionList);
        return inlineKeyboardMarkup;

    }

    private InlineKeyboardButton getInlineButton(final String text, final String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
