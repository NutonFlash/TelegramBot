package ru.TelegramBot.NavigationButtons.EditSettings;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.TelegramBot.NavigationButtons.Buttons;

import java.util.ArrayList;
import java.util.List;

import static ru.TelegramBot.NavigationButtons.CatalogList.setKeyboardMarkupSettings;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserName implements Buttons {
    final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    final KeyboardRow row1 = new KeyboardRow();
    final List<KeyboardRow> row = new ArrayList<>();

    public ReplyKeyboardMarkup getUserNameKeyboard() {
        return setKeyboardMarkupSettings(replyKeyboardMarkup, row1, home, back, row);
    }
}
