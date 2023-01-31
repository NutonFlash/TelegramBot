package ru.TelegramBot.Handlers;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.TelegramBot.NavigationButtons.Basket;
import ru.TelegramBot.NavigationButtons.Emojis;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasketCallbackQueryHandler {
    SendMessage sendMessage;

    public SendMessage nextToDeliveryMessage(long chatID) {
        sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setText("В нашем магазине нет самовызова, однако есть быстрая и удобная доставка!\nСтоимость доставки зависит от зоны, в которой находится получатель.\nСнизу на картинке показаны границы зелёной и фиолетовой зоны" + Emojis.ARROW_DOWN);
        sendMessage.setChatId(String.valueOf(chatID));
        return sendMessage;
    }

    public SendPhoto nextToDeliveryPhoto(long chatID) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile().setMedia("AgACAgIAAxkBAAIjIWFN_qV513UCyPWdueVPYxeOWPxaAAJYtzEb54pxSpETJqKWb1TZAQADAgADeQADIQQ"));
        sendPhoto.setChatId(String.valueOf(chatID));
        sendPhoto.setParseMode("html");
        sendPhoto.setCaption(
                "<b>Зона 1 (зелёная)</b>:\n\n" +

                        "заказ <code>0-999</code> \u2014 стоимость доставки 100\u20BD;\n" +
                        "заказ <code>1000-1499</code> \u2014 стоимость доставки 50\u20BD;\n" +
                        "заказ от <code>1500</code> \u2014 бесплатная доставка.\n\n" +

                        "среднее время доставки \u2014 30 минут\n\n" +

                        "<b>Зона 2 (фиолетовая)</b>:\n\n" +

                        "заказ <code>0-999</code> \u2014 стоимость доставки 200\u20BD;\n" +
                        "заказ <code>1000-1499</code> \u2014 стоимость доставки 150\u20BD;\n" +
                        "заказ от <code>1500</code> \u2014 бесплатная доставка.\n\n" +

                        "среднее время доставки \u2014 50 минут");
        return sendPhoto;
    }

    public SendMessage sendBasketList(long chatID) {
        sendMessage = new SendMessage();
        sendMessage.setText("Корзина:");
        sendMessage.setChatId(String.valueOf(chatID));
        sendMessage.setReplyMarkup(new Basket().getBasketKeyboard());
        return sendMessage;
    }
}
