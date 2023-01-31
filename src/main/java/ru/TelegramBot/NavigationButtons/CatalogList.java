package ru.TelegramBot.NavigationButtons;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class CatalogList implements Buttons {
    final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    final KeyboardRow row1 = new KeyboardRow();
    final List<KeyboardRow> row = new ArrayList<>();

    public static ReplyKeyboardMarkup setKeyboardMarkupSettings(ReplyKeyboardMarkup replyKeyboardMarkup, KeyboardRow row1, String beer, String pipes, List<KeyboardRow> row) {
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setSelective(true);
        row1.add(beer);
        row1.add(pipes);
        row.add(row1);
        replyKeyboardMarkup.setKeyboard(row);
        return replyKeyboardMarkup;
    }
}
