package ru.TelegramBot.Handlers.InputButtonsMessages;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.DataBase.DBService;
import ru.TelegramBot.NavigationButtons.EditSettings.UserAddress;
import ru.TelegramBot.NavigationButtons.Settings;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressSettingsHandler {

    final DBService dbService;

    public AddressSettingsHandler(DBService dbService) {
        this.dbService = dbService;
    }

    public SendMessage addressSettingsHandle(long userID, SendMessage sendMessage, UserDataCache userDataCache) {
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.USERADDRESS_SETTINGS);
        if (userDataCache.getUserProfileData(userID).getAddress() == null)
            sendMessage.setText("Вы можете отправить геолокацию или написать адрес вручную.\nУкажите адрес для доставок:");
        else {
            if (userDataCache.getUserProfileData(userID).getAddress().equals("не указан"))
                sendMessage.setText("Вы можете отправить геолокацию или написать вручную.\nУкажите адрес для доставок:");
            else
                sendMessage.setText("Вы можете отправить геолокацию или написать вручную.\nТекущий адрес для доставок: " + userDataCache.getUserProfileData(userID).getAddress() + "\nНовое значение:");
        }
        sendMessage.setReplyMarkup(new UserAddress().getUserAddressKeyboard());
        return sendMessage;
    }

    public SendMessage saveAddressSettingsHandle(long userID, SendMessage sendMessage, UserDataCache userDataCache, Message message) {
        userDataCache.getUserProfileData(userID).setAddress(message.getText());
        userDataCache.saveUserProfileData(userID, userDataCache.getUserProfileData(userID));
        dbService.getUserProfileRepository().updateUserProfileSetAddressForChatId(userDataCache.getUserProfileData(userID).getAddress(), message.getChatId());
        sendMessage.setText("Данные успешно сохранены!");
        sendMessage.setReplyMarkup(new Settings().getSettingsKeyboard());
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.MAIN_SETTINGS);
        return sendMessage;
    }
}
