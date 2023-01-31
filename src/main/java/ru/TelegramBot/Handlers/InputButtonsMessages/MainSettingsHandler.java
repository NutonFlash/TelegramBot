package ru.TelegramBot.Handlers.InputButtonsMessages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.NavigationButtons.Settings;

public class MainSettingsHandler {
    public SendMessage mainSettingsHandle(long userID, SendMessage sendMessage, UserDataCache userDataCache) {
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.MAIN_SETTINGS);
        sendMessage.setReplyMarkup(new Settings().getSettingsKeyboard());
        sendMessage.setParseMode("html");
        sendMessage.setText("Текущие настройки:\n<b>Имя</b> \u2014 " + userDataCache.getUserProfileData(userID).getName() + "\n<b>Адрес для доставок</b> \u2014 " + userDataCache.getUserProfileData(userID).getAddress() + "\n<b>Телефон</b> \u2014 " + userDataCache.getUserProfileData(userID).getPhoneNumber() + "\n\nВыберите настройки, которые хотите поменять:");
        return sendMessage;
    }
}
