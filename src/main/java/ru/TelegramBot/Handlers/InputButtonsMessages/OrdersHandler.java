package ru.TelegramBot.Handlers.InputButtonsMessages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;


public class OrdersHandler {
    public SendMessage ordersHandle(long userID, SendMessage sendMessage, UserDataCache userDataCache) {
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.ORDERS);
        sendMessage.enableHtml(true);
        return sendMessage;
    }
}
