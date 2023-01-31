package ru.TelegramBot.Handlers.InputButtonsMessages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.NavigationButtons.About;
import ru.TelegramBot.NavigationButtons.Emojis;

public class AboutHandler {
    public SendMessage aboutHandle(long userID, SendMessage sendMessage, UserDataCache userDataCache) {
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.ABOUT);
        sendMessage.setReplyMarkup(new About().getAboutKeyboard());
        sendMessage.enableHtml(true);
        sendMessage.setText(
                "Пиво и Дудки занимается доставкой крафтового пива и электронных сигарет в Екатеринбурге.\n\n" +
                        "У нас нет фиксированного меню, поэтому ассортимент пива и электронных сигарет постоянно обновляется" + Emojis.BEERS + Emojis.TRUMPET +
                        "\nНашим личным подарком каждому клиенту являются оригинальные неоновые наклейки, которые идут в подарок вместе с заказом. Они станут отличным дополнением для чехла телефона, компьютера или частью новой интересной и необычной коллекции" + Emojis.CAMERA +
                        "\nМы обожаем делать неожиданные подарки нашим покупателям! Именно поэтому наши постоянные клиенты могут запросто получить товар от очаровательной доставщицы, одетой лишь в бикини, а клиентки - от прекрасного молодого человека с шестью кубиками и набором остроумных шуток. Естественно, с вашего согласия" + Emojis.SMILEY +
                        "Также, вы можете посетить <a href=\"https://www.instagram.com/pivoidudki/\">нашу страничку</a> в инстаграм. Там публикуются все свежие новости по поводу обновления ассортимента и новых крутых акциях!");
        return sendMessage;
    }
}
