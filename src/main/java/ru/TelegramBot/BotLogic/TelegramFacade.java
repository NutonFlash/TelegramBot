package ru.TelegramBot.BotLogic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.TelegramBot.Cashe.MainUpdateInfo;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.DataBase.DBService;
import ru.TelegramBot.DataBase.model.ConfirmedOrder;
import ru.TelegramBot.DataBase.model.Product;
import ru.TelegramBot.DataBase.model.UserProfile;
import ru.TelegramBot.Handlers.ButtonsHandler;
import ru.TelegramBot.Handlers.CatalogCallbackQueryHandler;
import ru.TelegramBot.Handlers.CommandsHandler;
import ru.TelegramBot.Handlers.Location.LocationHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Getter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramFacade {

    @Autowired
    DBService dbService;

    final UserDataCache userDataCache = new UserDataCache();
    final MainUpdateInfo mainUpdateInfo = new MainUpdateInfo();
    Location courierLocation = new Location();
    UserProfile userProfileData;
    long userID;
    Message getMessage;
    SendMessage sendMessage;
    long chatId;
    EditMessageReplyMarkup messageReplyMarkup;

    public TelegramFacade() {
        courierLocation.setLongitude(60.600717);
        courierLocation.setLatitude(56.826910);
    }

    @SneakyThrows
    public SendMessage handleUpdate(Update update, Iterator<Product> productList) {
        sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        if (update.hasMessage()) {
            getMessage = update.getMessage();
            userID = getMessage.getFrom().getId();
            chatId = getMessage.getChatId();
            mainUpdateInfo.setMessage(getMessage);
            mainUpdateInfo.setUserDataCache(userDataCache);
            mainUpdateInfo.setUserID(userID);
            if (userDataCache.getUsersProfileData().size() == 0) {
                for (UserProfile userProfile : dbService.getUserProfileRepository().findAll()) {
                    userProfileData = userProfile;
                    userDataCache.saveUserProfileData(userProfileData.getChatID(), userProfileData);
                }
                Iterator<ConfirmedOrder> orderList = dbService.getConfirmedOrderRepository().findAll().iterator();
                for (Map.Entry<Long, UserProfile> entry : userDataCache.getUsersProfileData().entrySet()) {
                    long userID = entry.getKey();
                    while (orderList.hasNext()) {
                        ConfirmedOrder currentOrder = orderList.next();
                        if (currentOrder.getUserID() == userID) {
                            userDataCache.getUserProfileData(userID).getConfirmedOrderList().add(currentOrder);
                        }
                    }
                }
            }
            sendMessage.setChatId(String.valueOf(getMessage.getChatId()));
            mainUpdateInfo.setSendMessage(sendMessage);
            if (userDataCache.getUserProfileData(userID) == null) {
                userProfileData = new UserProfile();
                userProfileData.setChatID(chatId);
                userProfileData.setName("не указано");
                userProfileData.setAddress("не указан");
                userProfileData.setPhoneNumber("не указан");
                userProfileData.setTelegram(getMessage.getFrom().getUserName());
                HashMap<String, String> currentBasketButtonState = new HashMap<>();
                Iterator<Product> userProfileDataList = dbService.getProductRepository().findAll().iterator();
                while (userProfileDataList.hasNext()) {
                    currentBasketButtonState.put(userProfileDataList.next().getProductName(), "notAdded");
                }
                userProfileData.setCurrentBasketButtonState(currentBasketButtonState);
                userDataCache.saveUserProfileData(userID, userProfileData);
                dbService.getUserProfileRepository().save(userProfileData);
            } else if (userDataCache.getUserProfileData(userID).getCurrentBasketButtonState().size() == 0) {
                HashMap<String, String> currentBasketButtonState = new HashMap<>();
                Iterator<Product> userProfileDataList = dbService.getProductRepository().findAll().iterator();
                while (userProfileDataList.hasNext()) {
                    currentBasketButtonState.put(userProfileDataList.next().getProductName(), "notAdded");
                }
                userDataCache.getUserProfileData(userID).setCurrentBasketButtonState(currentBasketButtonState);
            }
            if (getMessage.hasEntities() && getMessage.hasText() && !getMessage.getEntities().get(0).getType().equals("phone_number")) {
                sendMessage = new CommandsHandler(userDataCache).commandsHandler(sendMessage, getMessage);
            } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.USERADDRESS_SETTINGS && getMessage.hasLocation()) {
                new LocationHandler(mainUpdateInfo, sendMessage, userID, getMessage, dbService);
            } else sendMessage = new ButtonsHandler(mainUpdateInfo, dbService).inputMessageHandle(getMessage.getText());
        } else if (update.hasEditedMessage()) {
            userID = update.getEditedMessage().getFrom().getId();
            if (update.getEditedMessage().hasLocation() && (userID == 1858217051))
                courierLocation = update.getEditedMessage().getLocation();
        } else if (update.hasCallbackQuery()) {
            userID = update.getCallbackQuery().getFrom().getId();
            mainUpdateInfo.setUserID(userID);
            messageReplyMarkup = new CatalogCallbackQueryHandler(mainUpdateInfo, productList, dbService).catalogRequestHandle(update.getCallbackQuery().getData());
        }
        return sendMessage;
    }
}

