package dev.fayzullokh.roombooking.bot.bothandler;


import org.telegram.telegrambots.meta.api.objects.Update;

public interface Handler<T> {
    T handle(Update update);
}
