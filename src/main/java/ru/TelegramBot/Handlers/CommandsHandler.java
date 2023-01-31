package ru.TelegramBot.Handlers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.Handlers.InputButtonsMessages.BasketHandler;
import ru.TelegramBot.NavigationButtons.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class CommandsHandler {
    final UserDataCache userDataCache;

    public CommandsHandler(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    public SendMessage commandsHandler(SendMessage sendMessage, Message message) {
        long userID = message.getFrom().getId();
        switch (message.getText()) {
            case "/help": {
                userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.HELP);
                sendMessage.setText("Список команд:\n/start - Главное меню\n/catalog - Каталог\n/basket - Корзина\n/orders - Заказы\n/settings - Настройки\n/help - Помощь\n/about - О нас\n\nЕсли у вас возникли проблемы с доставкой, вы можете написать или позвонить нашему курьеру, по остальным вопросам - пишите @sharmutta");
                sendMessage.setReplyMarkup(new Help().getHelpKeyboard());
                break;
            }
            case "/settings": {
                userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.MAIN_SETTINGS);
                sendMessage.setReplyMarkup(new Settings().getSettingsKeyboard());
                sendMessage.setParseMode("html");
                sendMessage.setText("Текущие настройки:\n<b>Имя</b> \u2014 " + userDataCache.getUserProfileData(userID).getName() + "\n<b>Адрес для доставок</b> \u2014 " + userDataCache.getUserProfileData(userID).getAddress() + "\n<b>Телефон</b> \u2014 " + userDataCache.getUserProfileData(userID).getPhoneNumber() + "\n\nВыберите настройки, которые хотите поменять:");
                break;
            }
            case "/catalog": {
                userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.CATALOG);
                sendMessage.setReplyMarkup(new Catalog().getCatalogKeyboard());
                sendMessage.setText("Что бы вы хотели заказать?");
                break;
            }
            case "/start": {
                if (userDataCache.getUsersCurrentBotState(userID) == null) sendMessage.setText("Добро пожаловать!");
                userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.HOME);
                sendMessage.setReplyMarkup(new Home().getMainMenuKeyboard());
                sendMessage.setText(Emojis.HOUSE + "Главное меню:");
                break;
            }
            case "/about": {
                userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.ABOUT);
                sendMessage.setReplyMarkup(new About().getAboutKeyboard());
                sendMessage.enableHtml(true);
                sendMessage.setText(
                        "Пиво и Дудки занимается доставкой крафтового пива и электронных сигарет в Екатеринбурге.\n\n" +
                                "У нас нет фиксированного меню, поэтому ассортимент пива и электронных сигарет постоянно обновляется" + Emojis.BEERS + Emojis.TRUMPET +
                                "\nНашим личным подарком каждому клиенту являются оригинальные неоновые наклейки, которые идут в подарок вместе с заказом. Они станут отличным дополнением для чехла телефона, компьютера или частью новой интересной и необычной коллекции" + Emojis.CAMERA +
                                "\nМы обожаем делать неожиданные подарки нашим покупателям! Именно поэтому наши постоянные клиенты могут запросто получить товар от очаровательной доставщицы, одетой лишь в бикини, а клиентки - от прекрасного молодого человека с шестью кубиками и набором остроумных шуток. Естественно, с вашего согласия" + Emojis.SMILEY +
                                "Также, вы можете посетить <a href=\"https://www.instagram.com/pivoidudki/\">нашу страничку</a> в инстаграм. Там публикуются все свежие новости по поводу обновления ассортимента и новых крутых акциях!");
                break;
            }
            case "/basket": {
                new BasketHandler().basketHandle(userID, sendMessage, userDataCache);
                break;
            }
            case "/orders": {
                userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.ORDERS);
                sendMessage.enableHtml(true);
                break;
            }
        }
        return sendMessage;
    }
}
