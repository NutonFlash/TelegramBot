package ru.TelegramBot.NavigationButtons;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Settings implements Buttons {
    final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    final KeyboardRow row1 = new KeyboardRow();
    final KeyboardRow row2 = new KeyboardRow();
    final KeyboardRow row3 = new KeyboardRow();
    final List<KeyboardRow> row = new ArrayList<>();

    public ReplyKeyboardMarkup getSettingsKeyboard() {
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setSelective(true);
        row1.add(userName);
        row1.add(userAddress);
        row2.add(userTelephoneNumber);
        row3.add(back);
        row.add(row1);
        row.add(row2);
        row.add(row3);
        replyKeyboardMarkup.setKeyboard(row);
        return replyKeyboardMarkup;
    }
}
