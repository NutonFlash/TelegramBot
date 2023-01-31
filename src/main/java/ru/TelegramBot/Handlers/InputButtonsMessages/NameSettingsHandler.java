package ru.TelegramBot.Handlers.InputButtonsMessages;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.DataBase.DBService;
import ru.TelegramBot.NavigationButtons.EditSettings.UserName;
import ru.TelegramBot.NavigationButtons.Settings;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class NameSettingsHandler {

    final DBService dbService;

    public NameSettingsHandler(DBService dbService) {
        this.dbService = dbService;
    }

    public SendMessage nameSettingsHandle(long userID, SendMessage sendMessage, UserDataCache userDataCache) {
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.USERNAME_SETTINGS);
        sendMessage.setReplyMarkup(new UserName().getUserNameKeyboard());
        if (userDataCache.getUserProfileData(userID).getName().equals("не указано"))
            sendMessage.setText("Укажите свое имя:");
        else
            sendMessage.setText("Ваше текущее имя: " + userDataCache.getUserProfileData(userID).getName() + "\nНовое значение:");
        return sendMessage;
    }

    public SendMessage saveNameSettingHandle(long userID, SendMessage sendMessage, UserDataCache userDataCache, Message message) {
        if (message.getText().matches("( *[a-zA-ZA-Яa-я]* *){1,3}")) {
            userDataCache.getUserProfileData(userID).setName(message.getText());
            userDataCache.saveUserProfileData(userID, userDataCache.getUserProfileData(userID));
            dbService.getUserProfileRepository().updateUserProfileSetNameForChatID(userDataCache.getUserProfileData(userID).getName(), message.getChatId());
            sendMessage.setText("Данные успешно сохранены!");
            sendMessage.setReplyMarkup(new Settings().getSettingsKeyboard());
            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.MAIN_SETTINGS);
        } else sendMessage.setText("Пожалуйста, укажите свое настоящее имя");
        return sendMessage;
    }
}
