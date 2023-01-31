package ru.TelegramBot.Handlers.InputButtonsMessages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.NavigationButtons.Catalog;
import ru.TelegramBot.NavigationButtons.Home;
import ru.TelegramBot.NavigationButtons.OrderSettings.DeliveryLocation;
import ru.TelegramBot.NavigationButtons.OrderSettings.DeliveryUserName;
import ru.TelegramBot.NavigationButtons.Settings;

import static ru.TelegramBot.BotLogic.HQDTelegramBot.usersBasketProductsList;
import static ru.TelegramBot.NavigationButtons.Buttons.home;

public class BackHandler {
    public SendMessage backHandle(long userID, SendMessage sendMessage, UserDataCache userDataCache) {
        if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.USERADDRESS_SETTINGS || userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.USERPHONE_SETTINGS || userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.USERNAME_SETTINGS) {
            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.MAIN_SETTINGS);
            sendMessage.setReplyMarkup(new Settings().getSettingsKeyboard());
            sendMessage.setText("Выберите настройки, которые хотите поменять:");
        } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.PIPES || userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.BEER) {
            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.CATALOG);
            sendMessage.setReplyMarkup(new Catalog().getCatalogKeyboard());
            sendMessage.setText("Что бы вы хотели заказать?");
        } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYLOCATION)
            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.BASKET);
        else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERNAME) {
            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYLOCATION);
            sendMessage.setReplyMarkup(new DeliveryLocation().getDeliveryLocationKeyboard(usersBasketProductsList.get(userID)));
        } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERPHONE) {
            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYUSERNAME);
            sendMessage.setReplyMarkup(new DeliveryUserName().getDeliveryUserNameKeyboard(usersBasketProductsList.get(userID)));
        } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYCONFIRM)
            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYUSERPHONE);
        else {
            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.HOME);
            sendMessage.setReplyMarkup(new Home().getMainMenuKeyboard());
            sendMessage.setText(home + ":");
        }
        return sendMessage;
    }
}
