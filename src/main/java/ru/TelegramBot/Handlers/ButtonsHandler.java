package ru.TelegramBot.Handlers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.MainUpdateInfo;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.DataBase.DBService;
import ru.TelegramBot.Handlers.InputButtonsMessages.*;
import ru.TelegramBot.NavigationButtons.Buttons;
import ru.TelegramBot.NavigationButtons.Emojis;

import javax.validation.constraints.NotNull;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class ButtonsHandler implements Buttons {
    long userID;
    SendMessage sendMessage;
    UserDataCache userDataCache;
    Message message;
    DBService dbService;

    public ButtonsHandler(MainUpdateInfo mainUpdateInfo, DBService dbService) {
        this.userID = mainUpdateInfo.getUserID();
        this.sendMessage = mainUpdateInfo.getSendMessage();
        this.userDataCache = mainUpdateInfo.getUserDataCache();
        this.message = mainUpdateInfo.getMessage();
        this.dbService = dbService;
    }

    @NotNull
    public SendMessage inputMessageHandle(String inputMessage) {
        if (inputMessage != null) {
            if (!inputMessage.equals(back) && !inputMessage.equals(home)) {
                if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.USERNAME_SETTINGS) {
                    sendMessage = new NameSettingsHandler(dbService).saveNameSettingHandle(userID, sendMessage, userDataCache, message);
                } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.USERADDRESS_SETTINGS) {
                    sendMessage = new AddressSettingsHandler(dbService).saveAddressSettingsHandle(userID, sendMessage, userDataCache, message);
                } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.USERPHONE_SETTINGS) {
                    sendMessage = new PhoneNumberSettingsHandler(dbService).savePhoneNumberSettingsHandler(userID, sendMessage, userDataCache, message);
                }
            }
            if (inputMessage.equals(home))
                sendMessage = new HomeHandler().homeHandler(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(catalog))
                sendMessage = new CatalogHandler().catalogHandler(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(basket))
                sendMessage = new BasketHandler().basketHandle(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(settings))
                sendMessage = new MainSettingsHandler().mainSettingsHandle(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(help))
                sendMessage = new HelpHandler().helpHandle(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(about))
                sendMessage = new AboutHandler().aboutHandle(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(userName))
                sendMessage = new NameSettingsHandler(dbService).nameSettingsHandle(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(userAddress))
                sendMessage = new AddressSettingsHandler(dbService).addressSettingsHandle(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(userTelephoneNumber))
                sendMessage = new PhoneNumberSettingsHandler(dbService).phoneNumberSettingsHandler(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(back))
                sendMessage = new BackHandler().backHandle(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(orders))
                sendMessage = new OrdersHandler().ordersHandle(userID, sendMessage, userDataCache);
            else if (inputMessage.equals(pipes))
                sendMessage = new PipesHandler().pipesHandle(userID, userDataCache, sendMessage);
            else if (inputMessage.equals(beer))
                sendMessage = new BeerHandler().beerHandle(userID, userDataCache, sendMessage);
            else if (inputMessage.equals("Адрес из настроек" + Emojis.GEAR) && userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYLOCATION) {
                if (!userDataCache.getUserProfileData(userID).getAddress().equals("не указан"))
                    sendMessage.setText("Есть адрес");
                else
                    sendMessage.setText("Вы еще не указали свой адрес в настройках.\nЧтобы сделать это, пожалуйста, перейдите в настройки.");
            }
        } else if ((userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.USERPHONE_SETTINGS && message.hasContact()))
            sendMessage = new PhoneNumberSettingsHandler(dbService).savePhoneNumberSettingsHandler(userID, sendMessage, userDataCache, message);
        sendMessage.setChatId(String.valueOf(userDataCache.getUserProfileData(userID).getChatID()));
        return sendMessage;
    }
}