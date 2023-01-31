package ru.TelegramBot.NavigationButtons;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Help implements Buttons {
    final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    final KeyboardRow row2 = new KeyboardRow();
    final List<KeyboardRow> row = new ArrayList<>();

    public ReplyKeyboardMarkup getHelpKeyboard() {
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setSelective(true);
        row2.add(back);
        row.add(row2);
        replyKeyboardMarkup.setKeyboard(row);
        return replyKeyboardMarkup;
    }
}
