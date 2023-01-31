package ru.TelegramBot.Handlers.InputButtonsMessages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.NavigationButtons.Help;

public class HelpHandler {
    public SendMessage helpHandle(long userID, SendMessage sendMessage, UserDataCache userDataCache) {
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.HELP);
        sendMessage.setText("Список команд:\n/start - Главное меню\n/catalog - Каталог\n/basket - Корзина\n/orders - Заказы\n/settings - Настройки\n/help - Помощь\n/about - О нас\n\nЕсли у вас возникли проблемы с доставкой, напишите @sharmutta");
        sendMessage.setReplyMarkup(new Help().getHelpKeyboard());
        return sendMessage;
    }
}
