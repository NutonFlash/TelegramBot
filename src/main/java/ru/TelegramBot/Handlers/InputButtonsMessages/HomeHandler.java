package ru.TelegramBot.Handlers.InputButtonsMessages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.NavigationButtons.Emojis;
import ru.TelegramBot.NavigationButtons.Home;

public class HomeHandler {
    public SendMessage homeHandler(long userID, SendMessage sendMessage, UserDataCache userDataCache) {
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.HOME);
        sendMessage.setReplyMarkup(new Home().getMainMenuKeyboard());
        sendMessage.setText(Emojis.HOUSE + "Главное меню:");
        return sendMessage;
    }
}
