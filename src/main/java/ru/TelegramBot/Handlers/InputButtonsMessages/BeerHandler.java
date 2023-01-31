package ru.TelegramBot.Handlers.InputButtonsMessages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.NavigationButtons.Emojis;

public class BeerHandler {
    public SendMessage beerHandle(long userID, UserDataCache userDataCache, SendMessage sendMessage) {
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.BEER);
        sendMessage.setText("Пиво" + Emojis.BEER);
        return sendMessage;
    }
}
