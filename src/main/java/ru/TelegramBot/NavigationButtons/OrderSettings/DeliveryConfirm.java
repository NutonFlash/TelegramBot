package ru.TelegramBot.NavigationButtons.OrderSettings;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.TelegramBot.NavigationButtons.Buttons;
import ru.TelegramBot.NavigationButtons.Emojis;

import java.util.ArrayList;
import java.util.List;

public class DeliveryConfirm implements Buttons {
    final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    final KeyboardRow row1 = new KeyboardRow();
    final KeyboardRow row2 = new KeyboardRow();
    final KeyboardRow row3 = new KeyboardRow();
    final List<KeyboardRow> row = new ArrayList<>();

    public ReplyKeyboardMarkup getDeliveryConfirmKeyboard() {
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setSelective(true);
        row1.add(Emojis.WHITE_CHECK_MARK + "Подтвердить и отправить");
        row2.add("Изменить");
        row3.add(home);
        row3.add(back);
        row.add(row1);
        row.add(row2);
        row.add(row3);
        replyKeyboardMarkup.setKeyboard(row);
        return replyKeyboardMarkup;
    }
}
