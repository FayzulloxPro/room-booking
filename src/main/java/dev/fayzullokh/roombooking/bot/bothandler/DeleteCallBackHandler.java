package dev.fayzullokh.roombooking.bot.bothandler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
public class DeleteCallBackHandler implements Handler<DeleteMessage>{
    @Override
    public DeleteMessage handle(Update update) {
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getFrom().getId();
        return new DeleteMessage(String.valueOf(chatId), messageId);
    }
}
