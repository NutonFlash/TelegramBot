package ru.TelegramBot.NavigationButtons.EditSettings;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.TelegramBot.NavigationButtons.Buttons;
import ru.TelegramBot.NavigationButtons.Emojis;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPhone implements Buttons {
    final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    final KeyboardRow row1 = new KeyboardRow();
    final KeyboardRow row2 = new KeyboardRow();
    final List<KeyboardRow> row = new ArrayList<>();

    public ReplyKeyboardMarkup getUserPhoneKeyboard() {
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setSelective(true);
        KeyboardButton sharePhoneNumber = new KeyboardButton();
        sharePhoneNumber.setText("Отправить свой номер" + Emojis.SELFIE);
        sharePhoneNumber.setRequestContact(true);
        row1.add(sharePhoneNumber);
        row2.add(home);
        row2.add(back);
        row.add(row1);
        row.add(row2);
        replyKeyboardMarkup.setKeyboard(row);
        return replyKeyboardMarkup;
    }
}
