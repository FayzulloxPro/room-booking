package dev.fayzullokh.roombooking.bot.utils;

import dev.fayzullokh.roombooking.entities.Room;
import dev.fayzullokh.roombooking.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
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
        row.add(getInlineButton("Yes", "logout"));
        row.add(getInlineButton("\uD83D\uDDD1", "delete"));
        twoDimensionList.add(row);
        inlineKeyboardMarkup.setKeyboard(twoDimensionList);
        return inlineKeyboardMarkup;

    }

    private InlineKeyboardButton getInlineButton(final String text, final String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public InlineKeyboardMarkup adminMenu(long chatId, String languageCode) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> twoDimensionList = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(getInlineButton("Rooms", "rooms"));
        row.add(getInlineButton("Users", "users"));
        twoDimensionList.add(row);
        twoDimensionList.add(List.of(getInlineButton("Add room", "add_room")));
        twoDimensionList.add(List.of(getInlineButton("\uD83D\uDDD1", "delete")));
        inlineKeyboardMarkup.setKeyboard(twoDimensionList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createRoomsList(Page<Room> roomPage, long chatId, String languageCode) {
        // Initialize the inline keyboard markup
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        // Iterate through the rooms in the current page and create a button for each room
        for (Room room : roomPage.getContent()) {
            // Add the button to the current row
            rowInline.add(getInlineButton(room.getRoomNumber(), "ROOM_ID_#" + room.getId()));
            // If the row is full (3 buttons), add it to the list of rows and start a new row
            if (rowInline.size() == 3) {
                rowsInline.add(rowInline);
                rowInline = new ArrayList<>();
            }
        }
        // Add the last row if it has any buttons
        if (!rowInline.isEmpty()) {
            rowsInline.add(rowInline);
        }

        // Add navigation buttons
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();

        if (roomPage.hasPrevious()) {
            navigationRow.add(getInlineButton("⬅\uFE0F", "PREV_PAGE#" + (roomPage.getNumber() - 1)));
        }
        navigationRow.add(getInlineButton("\uD83D\uDDD1", "delete"));

        if (roomPage.hasNext()) {
            navigationRow.add(getInlineButton("➡\uFE0F", "NEXT_PAGE#" + (roomPage.getNumber() + 1)));
        }
        // Add the navigation row to the list of rows
        rowsInline.add(navigationRow);
        // Set the keyboard to the markup
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup roomMenu(long chatId, Long roomId, String languageCode, boolean isAdmin) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        if (isAdmin) {
            rowInline.add(getInlineButton("Room number", "update_room_number#" + roomId));
            rowInline.add(getInlineButton("Room description", "update_room_description#" + roomId));
            rowsInline.add(rowInline);
            rowInline = new ArrayList<>();
            ;
            rowInline.add(getInlineButton("Max seats", "update_room_max_seats#" + roomId));
            rowInline.add(getInlineButton("Min seats", "update_room_min_seats#" + roomId));
            rowsInline.add(rowInline);
        } else {
            rowInline.add(getInlineButton("Reserve a room", "order#" + roomId));
            rowsInline.add(rowInline);
        }
        rowInline = new ArrayList<>();

        rowInline.add(getInlineButton("\uD83D\uDDD1", "delete"));
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createRoomConfirmation(long chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> footerLine = new ArrayList<>();
        ArrayList<InlineKeyboardButton> updateButtons = new ArrayList<>();

        updateButtons.add(getInlineButton("Update room number", "update_create_room_number"));
        updateButtons.add(getInlineButton("Update description", "update_create_room_description"));
        rowsInline.add(updateButtons);
        updateButtons = new ArrayList<>();

        updateButtons.add(getInlineButton("Update max seats", "update_create_room_max_seats"));
        updateButtons.add(getInlineButton("Update min seats", "update_create_room_min_seats"));
        rowsInline.add(updateButtons);
        footerLine.add(getInlineButton("Yes", "confirm_room_creation"));
        footerLine.add(getInlineButton("No", "cancel_room_creation"));
        rowsInline.add(footerLine);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
