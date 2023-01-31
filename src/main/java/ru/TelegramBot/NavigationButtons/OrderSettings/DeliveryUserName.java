package ru.TelegramBot.NavigationButtons.OrderSettings;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.TelegramBot.Basket.BasketProductsList;
import ru.TelegramBot.NavigationButtons.Buttons;
import ru.TelegramBot.NavigationButtons.Emojis;

import java.util.ArrayList;
import java.util.List;

public class DeliveryUserName implements Buttons {
    final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    final KeyboardRow row1 = new KeyboardRow();
    final KeyboardRow row2 = new KeyboardRow();
    final List<KeyboardRow> row = new ArrayList<>();

    public ReplyKeyboardMarkup getDeliveryUserNameKeyboard(BasketProductsList basket) {
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setSelective(true);
        row1.add("Имя из настроек" + Emojis.GEAR);
        row2.add(back);
        if (!basket.getUserName().equals("не указано")) {
            KeyboardRow row0 = new KeyboardRow();
            row0.add(Emojis.WHITE_CHECK_MARK + "Верно");
            row.add(row0);
        }
        row.add(row1);
        row.add(row2);
        replyKeyboardMarkup.setKeyboard(row);
        return replyKeyboardMarkup;
    }
}
