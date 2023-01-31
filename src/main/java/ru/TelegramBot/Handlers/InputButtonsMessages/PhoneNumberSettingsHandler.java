package ru.TelegramBot.Handlers.InputButtonsMessages;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.DataBase.DBService;
import ru.TelegramBot.NavigationButtons.EditSettings.UserPhone;
import ru.TelegramBot.NavigationButtons.Settings;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhoneNumberSettingsHandler {

    final DBService dbService;

    public PhoneNumberSettingsHandler(DBService dbService) {
        this.dbService = dbService;
    }

    public SendMessage phoneNumberSettingsHandler(long userID, SendMessage sendMessage, UserDataCache userDataCache) {
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.USERPHONE_SETTINGS);
        if (userDataCache.getUserProfileData(userID).getPhoneNumber().equals("не указан"))
            sendMessage.setText("Укажите свой номер:");
        else
            sendMessage.setText("Ваш текущий номер: " + userDataCache.getUserProfileData(userID).getPhoneNumber() + "\nНовое значение:");
        sendMessage.setReplyMarkup(new UserPhone().getUserPhoneKeyboard());
        return sendMessage;
    }

    public SendMessage savePhoneNumberSettingsHandler(long userID, SendMessage sendMessage, UserDataCache userDataCache, Message message) {
        if (message.hasText()) {
            if (message.getText().matches("(\\+?)\\d{11}")) {
                userDataCache.getUserProfileData(userID).setPhoneNumber(message.getText());
                dbService.getUserProfileRepository().updateUserProfileSetPhoneNumberForChatID(userDataCache.getUserProfileData(userID).getPhoneNumber(), message.getChatId());
                sendMessage.setText("Данные успешно сохранены!");
                sendMessage.setReplyMarkup(new Settings().getSettingsKeyboard());
                userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.MAIN_SETTINGS);
            } else sendMessage.setText("Пожалуйста, укажите свой настоящий телефон");
        } else if (message.hasContact()) {
            userDataCache.getUserProfileData(userID).setPhoneNumber(message.getContact().getPhoneNumber());
            dbService.getUserProfileRepository().updateUserProfileSetPhoneNumberForChatID(userDataCache.getUserProfileData(userID).getPhoneNumber(), message.getChatId());
            sendMessage.setText("Данные успешно сохранены!");
            sendMessage.setReplyMarkup(new Settings().getSettingsKeyboard());
            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.MAIN_SETTINGS);
        }
        return sendMessage;
    }
}
