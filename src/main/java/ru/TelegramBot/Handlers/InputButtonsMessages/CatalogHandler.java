package ru.TelegramBot.Handlers.InputButtonsMessages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.NavigationButtons.Catalog;

public class CatalogHandler {
    public SendMessage catalogHandler(long userID, SendMessage sendMessage, UserDataCache userDataCache) {
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.CATALOG);
        sendMessage.setChatId(String.valueOf(userDataCache.getUserProfileData(userID).getChatID()));
        sendMessage.setReplyMarkup(new Catalog().getCatalogKeyboard());
        sendMessage.setText("Что бы вы хотели заказать?");
        return sendMessage;
    }
}
