package ru.TelegramBot.BotLogic;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.TelegramBot.Basket.BasketProductsList;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.DataBase.DBService;
import ru.TelegramBot.DataBase.model.ConfirmedOrder;
import ru.TelegramBot.DataBase.model.Product;
import ru.TelegramBot.DeliveryZones.DeliveryZonesHandler;
import ru.TelegramBot.Handlers.BasketCallbackQueryHandler;
import ru.TelegramBot.Handlers.CatalogCallbackQueryHandler;
import ru.TelegramBot.Handlers.InputButtonsMessages.CatalogHandler;
import ru.TelegramBot.Handlers.InputButtonsMessages.HomeHandler;
import ru.TelegramBot.Handlers.Location.LocationHandler;
import ru.TelegramBot.NavigationButtons.Basket;
import ru.TelegramBot.NavigationButtons.Emojis;
import ru.TelegramBot.NavigationButtons.Home;
import ru.TelegramBot.NavigationButtons.OrderSettings.DeliveryConfirm;
import ru.TelegramBot.NavigationButtons.OrderSettings.DeliveryLocation;
import ru.TelegramBot.NavigationButtons.OrderSettings.DeliveryPhoneNumber;
import ru.TelegramBot.NavigationButtons.OrderSettings.DeliveryUserName;
import ru.TelegramBot.NavigationButtons.Orders;
import ru.TelegramBot.Orders.OrdersList;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class HQDTelegramBot extends TelegramWebhookBot {

    @Autowired
    DBService dbService;

    public static HashMap<Long, BasketProductsList> usersBasketProductsList = new HashMap<>();
    public static ArrayList<String> configurationNames = new ArrayList<>();
    final HashMap<String, String> title_configurationName_Map = new HashMap<>();
    public HashMap<Long, OrdersList> usersOrdersMap = new HashMap<>();
    int uniqueUpdateID = 0;
    DeliveryZonesHandler deliveryZonesHandler;
    String botPath;
    String botUserName;
    String botToken;
    SendMessage sendMessage;
    TelegramFacade telegramFacade;
    long userID;
    long chatID;
    UserDataCache userDataCache;
    HashMap<Long, HashMap<String, Integer>> productsMessageIDMap = new HashMap<>();
    HashMap<String, Integer> addingProductAmountIDMapCourier = new HashMap<>();
    HashMap<String, Integer> addingProductAmountIDMapSanya = new HashMap<>();
    HashMap<Long, Integer> ordersMessageIDMap = new HashMap<>();
    EditMessageReplyMarkup messageReplyMarkup;
    EditMessageMedia editMessageMedia;
    int locationMessageID;
    String callbackQueryData;
    JsonObject lastUsersCoordinates;
    Iterator<Product> products;
    String addingState = "";

    public HQDTelegramBot(TelegramFacade telegramFacade) {
        super(new DefaultBotOptions());
        this.telegramFacade = telegramFacade;
        deliveryZonesHandler = new DeliveryZonesHandler();
    }

    @Override
    public String getBotUsername() {
        return this.botUserName;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @SneakyThrows
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        products = dbService.getProductRepository().findAll().iterator();
        while (products.hasNext()) {
            Product product = products.next();
            if (!configurationNames.contains(product.getConfigurationName())) {
                configurationNames.add(product.getConfigurationName());
                title_configurationName_Map.put(product.getProductName(), product.getConfigurationName());
            }
        }
        if (uniqueUpdateID != update.getUpdateId()) {
            sendMessage = telegramFacade.handleUpdate(update, products);
            userID = telegramFacade.getUserID();
            userDataCache = telegramFacade.getUserDataCache();
            chatID = userDataCache.getUserProfileData(userID).getChatID();
            productsMessageIDMap.computeIfAbsent(userID, k -> new HashMap<>());
            usersOrdersMap.computeIfAbsent(userID, k -> new OrdersList());
            BasketProductsList basketProductsList = new BasketProductsList(userDataCache);
            if (usersBasketProductsList.get(userID) == null) {
                basketProductsList.setUserAddress("не указан");
                basketProductsList.setUserName("не указано");
                basketProductsList.setPhoneNumber("не указан");
                basketProductsList.setUserID(userID);
                basketProductsList.setUserAddressDetails("не указаны");
                usersBasketProductsList.put(userID, basketProductsList);
            }
            if (telegramFacade.getUserDataCache().getUsersCurrentBotState(userID) == NavigationBotStates.PIPES) {
                executePipesList(chatID);
            } else if (telegramFacade.getUserDataCache().getUsersCurrentBotState(userID) == NavigationBotStates.BEER) {
                executeBeerList(chatID);
            }
            if (update.hasCallbackQuery()) {
                callbackQueryData = update.getCallbackQuery().getData();
                if (callbackQueryData.matches(".*Added|deleteFromBasket.*")) {
                    if (callbackQueryData.matches(".*Added")) {
                        messageReplyMarkup = telegramFacade.getMessageReplyMarkup();
                        changingButtonMarkup(callbackQueryData);
                        try {
                            execute(messageReplyMarkup);
                        } catch (TelegramApiException exception) {
                            exception.printStackTrace();
                        }
                    }
                    if (userDataCache.getUserProfileData(userID).getMessageID() != 0) {
                        if (callbackQueryData.matches(".*Added")) {
                            editMessageMedia = usersBasketProductsList.get(userID).changeProductPhoto(callbackQueryData, userID);
                            try {
                                execute(editMessageMedia);
                            } catch (TelegramApiException exception) {
                                exception.printStackTrace();
                            }
                        } else {
                            if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() > 1) {
                                String[] splittedCallback = callbackQueryData.split("deleteFromBasket");
                                for (Map.Entry<String, String> entry : getTitle_configurationName_Map().entrySet()) {
                                    if (entry.getValue().equals(splittedCallback[1])) {
                                        editMessageMedia = usersBasketProductsList.get(userID).changeProductPhoto("deleteFromBasket" + entry.getKey(), userID);
                                        break;
                                    }
                                }
                                messageReplyMarkup = telegramFacade.getMessageReplyMarkup();
                                changingButtonMarkup(callbackQueryData);
                                try {
                                    execute(editMessageMedia);
                                    execute(messageReplyMarkup);
                                } catch (TelegramApiException exception) {
                                    exception.printStackTrace();
                                }
                            } else {
                                DeleteMessage deleteMessage = new DeleteMessage();
                                deleteMessage.setChatId(String.valueOf(chatID));
                                deleteMessage.setMessageId(userDataCache.getUserProfileData(userID).getMessageID());
                                execute(deleteMessage);
                                deleteMessage.setMessageId(userDataCache.getUserProfileData(userID).getMessageID() - 1);
                                deleteMessage.setChatId(String.valueOf(chatID));
                                execute(deleteMessage);
                                userDataCache.getUserProfileData(userID).setMessageID(0);
                                userDataCache.getUserProfileData(userID).getCurrentUserBasket().clear();
                                messageReplyMarkup = telegramFacade.getMessageReplyMarkup();
                                changingButtonMarkup(callbackQueryData);
                                try {
                                    execute(messageReplyMarkup);
                                } catch (TelegramApiException exception) {
                                    exception.printStackTrace();
                                }
                                sendMessage.setChatId(String.valueOf(chatID));
                                userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.HOME);
                                sendMessage.setReplyMarkup(new Home().getMainMenuKeyboard());
                                sendMessage.setText(Emojis.HOUSE + "Главное меню:");
                            }
                        }
                    } else if (callbackQueryData.matches("deleteFromBasket.*")) {
                        messageReplyMarkup = telegramFacade.getMessageReplyMarkup();
                        changingButtonMarkup(callbackQueryData);
                        try {
                            execute(messageReplyMarkup);
                        } catch (TelegramApiException exception) {
                            exception.printStackTrace();
                        }
                        String[] splittedCallback = callbackQueryData.split("deleteFromBasket");
                        int deleteProductIndex = 0;
                        for (Product productInfo : userDataCache.getUserProfileData(userID).getCurrentUserBasket()) {
                            ++deleteProductIndex;
                            String productName = "";
                            for (Map.Entry<String, String> entry : getTitle_configurationName_Map().entrySet()) {
                                if (entry.getValue().equals(splittedCallback[1])) {
                                    productName = entry.getKey();
                                    break;
                                }
                                if (productInfo.equals(productName))
                                    break;
                            }
                        }
                        userDataCache.getUserProfileData(userID).getCurrentUserBasket().remove(deleteProductIndex - 1);
                    }
                } else if (callbackQueryData.matches("change.*")) {
                    String configurationName = callbackQueryData.split("change")[1];
                    Product product = dbService.getProductRepository().findProductByConfigurationName(configurationName);
                    addingState = configurationName;
                    sendMessage.setText("Укажите, какое количество товара \"" + product.getProductName() + "\" сейчас у вас имеется на руках");
                    sendMessage.setChatId(String.valueOf(1858217051));
                } else if (callbackQueryData.matches(".*_.*")) {
                    if (userDataCache.getUserProfileData(userID).getMessageID() != 0) {
                        DeleteMessage deleteMessage = new DeleteMessage();
                        deleteMessage.setChatId(String.valueOf(chatID));
                        deleteMessage.setMessageId(userDataCache.getUserProfileData(userID).getMessageID());
                        execute(deleteMessage);
                        deleteMessage.setMessageId(userDataCache.getUserProfileData(userID).getMessageID() - 1);
                        deleteMessage.setChatId(String.valueOf(chatID));
                        execute(deleteMessage);
                        userDataCache.getUserProfileData(userID).setMessageID(0);
                    }
                    String[] splittedCallback = callbackQueryData.split("_");
                    for (Map.Entry<String, String> entry : getTitle_configurationName_Map().entrySet()) {
                        if (entry.getValue().equals(splittedCallback[0])) {
                            splittedCallback[1] = entry.getKey();
                            break;
                        }
                    }
                    if (userDataCache.getUserProfileData(userID).getCurrentBasketButtonState().get(splittedCallback[1]).equals("notAdded")) {
                        messageReplyMarkup = telegramFacade.getMessageReplyMarkup();
                        messageReplyMarkup.setChatId(String.valueOf(chatID));
                        messageReplyMarkup.setMessageId(productsMessageIDMap.get(userID).get(splittedCallback[0]));
                        try {
                            execute(messageReplyMarkup);
                        } catch (TelegramApiException exception) {
                            exception.printStackTrace();
                        }
                    }
                    userDataCache.getUserProfileData(userID).getCurrentBasketButtonState().replace(splittedCallback[1], "added");
                    execute(new BasketCallbackQueryHandler().sendBasketList(chatID));
                    userDataCache.getUserProfileData(userID).setMessageID(execute(usersBasketProductsList.get(userID).sendBasketList(chatID, userID)).getMessageId());
                } else {
                    switch (callbackQueryData) {
                        case "previousProduct":
                        case "nextProduct": {
                            if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() > 1) {
                                editMessageMedia = usersBasketProductsList.get(userID).changeProductPhoto(callbackQueryData, userID);
                                try {
                                    execute(editMessageMedia);
                                } catch (TelegramApiException exception) {
                                    exception.printStackTrace();
                                }
                            }
                            break;
                        }
                        case "lessAmount":
                        case "moreAmount": {
                            if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() > 0) {
                                String[] splittedCaption = update.getCallbackQuery().getMessage().getCaption().split("\\n", 2);
                                Product productFromDB = dbService.getProductRepository().findProductByProductName(splittedCaption[0]);
                                int productIndex = 0;
                                for (Product product : userDataCache.getUserProfileData(userID).getCurrentUserBasket()) {
                                    productIndex++;
                                    if (product.getProductName().equals(splittedCaption[0]))
                                        break;
                                }
                                if (callbackQueryData.equals("moreAmount")) {
                                    if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().get(productIndex - 1).getProductAmount() > 0) {
                                        if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().get(productIndex - 1).getProductAmount() < productFromDB.getProductAmount()) {
                                            userDataCache.getUserProfileData(userID).getCurrentUserBasket().get(productIndex - 1).increaseProductAmount();
                                            editMessageMedia = usersBasketProductsList.get(userID).changeProductPhoto(callbackQueryData, userID);
                                            try {
                                                execute(editMessageMedia);
                                            } catch (TelegramApiException exception) {
                                                exception.printStackTrace();
                                            }
                                        } else {
                                            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
                                            answerCallbackQuery.setText("В наличии только " + productFromDB.getProductAmount() + Emojis.PENSIVE);
                                            answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
                                            execute(answerCallbackQuery);
                                        }
                                    }
                                } else {
                                    if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().get(productIndex - 1).getProductAmount() > 1) {
                                        userDataCache.getUserProfileData(userID).getCurrentUserBasket().get(productIndex - 1).decreaseProductAmount();
                                        editMessageMedia = usersBasketProductsList.get(userID).changeProductPhoto(callbackQueryData, userID);
                                        try {
                                            execute(editMessageMedia);
                                        } catch (TelegramApiException exception) {
                                            exception.printStackTrace();
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        case "deleteProduct": {
                            String[] splittedCaption = update.getCallbackQuery().getMessage().getCaption().split("\\n", 2);
                            if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() > 1) {
                                editMessageMedia = usersBasketProductsList.get(userID).changeProductPhoto(callbackQueryData + splittedCaption[0], userID);
                                try {
                                    execute(editMessageMedia);
                                } catch (TelegramApiException exception) {
                                    exception.printStackTrace();
                                }
                            } else if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() == 1) {
                                DeleteMessage deleteMessage = new DeleteMessage();
                                deleteMessage.setMessageId(userDataCache.getUserProfileData(userID).getMessageID());
                                deleteMessage.setChatId(String.valueOf(chatID));
                                execute(deleteMessage);
                                deleteMessage.setMessageId(userDataCache.getUserProfileData(userID).getMessageID() - 1);
                                deleteMessage.setChatId(String.valueOf(chatID));
                                execute(deleteMessage);
                                userDataCache.getUserProfileData(userID).setMessageID(0);
                                userDataCache.getUserProfileData(userID).setCurrentUserBasket(new ArrayList<>());
                                sendMessage = new HomeHandler().homeHandler(userID, sendMessage, userDataCache);
                                sendMessage.setChatId(String.valueOf(chatID));
                            }
                            messageReplyMarkup = new CatalogCallbackQueryHandler(getTelegramFacade().getMainUpdateInfo(), products, dbService).catalogRequestHandle("forEditingReplyMarkup" + getTitle_configurationName_Map().get(splittedCaption[0]));
                            messageReplyMarkup.setMessageId(productsMessageIDMap.get(userID).get(getTitle_configurationName_Map().get(splittedCaption[0])));
                            messageReplyMarkup.setChatId(String.valueOf(chatID));
                            try {
                                execute(messageReplyMarkup);
                            } catch (TelegramApiException exception) {
                                exception.printStackTrace();
                            }
                            break;
                        }
                        case "nextToDelivery": {
                            if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() > 0) {
                                sendDeliveryLocationDetails();
                            }
                            break;
                        }
                        case "backToCatalog": {
                            sendMessage = new CatalogHandler().catalogHandler(userID, sendMessage, userDataCache);
                            break;
                        }
                        case "previousOrder":
                        case "nextOrder": {
                            EditMessageText editMessageText = usersOrdersMap.get(userID).editOrder(callbackQueryData, userDataCache.getUserProfileData(userID).getConfirmedOrderList());
                            editMessageText.setChatId(String.valueOf(chatID));
                            editMessageText.setMessageId(ordersMessageIDMap.get(userID));
                            editMessageText.enableHtml(true);
                            try {
                                execute(editMessageText);
                            } catch (TelegramApiException exception) {
                                exception.printStackTrace();
                            }
                            break;
                        }

                    }
                }
            } else if (update.hasMessage()) {
                if (update.getMessage().hasPhoto() && userID == 1027947243) {
                    PhotoSize photoSize;
                    ListIterator<PhotoSize> listIterator = update.getMessage().getPhoto().listIterator();
                    while (listIterator.hasNext()) {
                        photoSize = listIterator.next();
                        log.info("Photo ID - {}, size of the photo - width: {}px, height: {}px, file size - {}", photoSize.getFileId(), photoSize.getWidth(), photoSize.getHeight(), photoSize.getFileSize());
                    }
                }
                if (update.getMessage().hasText())
                    if (update.getMessage().getText().equals("xsdfhasdfg32423CGHF"))
                        log.info("ID курьера: {}", userID);
                if (update.getMessage().hasContact() && userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERPHONE) {
                    usersBasketProductsList.get(userID).setPhoneNumber(update.getMessage().getContact().getPhoneNumber());
                    userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYCONFIRM);
                    sendMessage.setText("Данные успешно сохранены!");
                    execute(sendMessage);
                    deliveryConfirmMessage(usersBasketProductsList.get(userID));
                }
                if (update.getMessage().hasText()) {
                    if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYLOCATION && update.getMessage().getText().equals(Emojis.ARROW_LEFT + "Назад"))
                        sendDeliveryLocationDetails();
                    else if (update.getMessage().getText().equals(Emojis.WHITE_CHECK_MARK + "Верно") && userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYLOCATION) {
                        BasketProductsList basket = usersBasketProductsList.get(userID);
                        int totalCost = basket.getTotalCost();
                        if (basket.getDeliveryZone().equals("green")) {
                            if (totalCost < 1000)
                                basket.setDeliveryCost(100);
                            else if (totalCost < 1500)
                                basket.setDeliveryCost(50);
                            else basket.setDeliveryCost(0);
                        } else if (basket.getDeliveryZone().equals("purple")) {
                            if (totalCost < 1000)
                                basket.setDeliveryCost(200);
                            else if (totalCost < 1500)
                                basket.setDeliveryCost(150);
                            else basket.setDeliveryCost(0);
                        }
                        basket.setTotalCost(totalCost + basket.getDeliveryCost());
                        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYUSERNAME);
                        sendMessage.setReplyMarkup(new DeliveryUserName().getDeliveryUserNameKeyboard(usersBasketProductsList.get(userID)));
                        sendMessage.setText("Укажите свое имя:\nСейчас: " + usersBasketProductsList.get(userID).getUserName());
                    } else if (update.getMessage().getText().equals(Emojis.WHITE_CHECK_MARK + "Верно") && userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERNAME) {
                        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYUSERPHONE);
                        sendMessage.setReplyMarkup(new DeliveryPhoneNumber().getDeliveryPhoneNumberKeyboard(usersBasketProductsList.get(userID)));
                        sendMessage.setText("Укажите свой номер телефона:\nСейчас: " + usersBasketProductsList.get(userID).getPhoneNumber());
                    } else if (update.getMessage().getText().equals(Emojis.WHITE_CHECK_MARK + "Верно") && userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERPHONE) {
                        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYCONFIRM);
                        deliveryConfirmMessage(usersBasketProductsList.get(userID));
                    } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYCONFIRM && update.getMessage().getText().equals("Изменить")) {
                        sendDeliveryLocationDetails();
                    } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERNAME && update.getMessage().getText().equals(Emojis.ARROW_LEFT + "Назад"))
                        sendMessage.setText("Укажите свое имя:\nСейчас: " + usersBasketProductsList.get(userID).getUserName());
                    else if (update.getMessage().getText().equals(Emojis.ARROW_LEFT + "Назад") && userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERNAME) {
                        BasketProductsList basket = usersBasketProductsList.get(userID);
                        int totalCost = basket.getTotalCost();
                        if (basket.getDeliveryZone().equals("green")) {
                            if (totalCost < 1000)
                                basket.setDeliveryCost(100);
                            else if (totalCost < 1500)
                                basket.setDeliveryCost(50);
                            else basket.setDeliveryCost(0);
                        } else if (basket.getDeliveryZone().equals("purple")) {
                            if (totalCost < 1000)
                                basket.setDeliveryCost(200);
                            else if (totalCost < 1500)
                                basket.setDeliveryCost(150);
                            else basket.setDeliveryCost(0);
                        }
                        basket.setTotalCost(totalCost + basket.getDeliveryCost());
                        sendDeliveryLocationDetails();
                    } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERNAME) {
                        if (update.getMessage().getText().equals("Имя из настроек" + Emojis.GEAR)) {
                            if (!userDataCache.getUserProfileData(userID).getName().equals("не указано")) {
                                sendMessage.setText("Данные успешно сохранены!");
                                execute(sendMessage);
                                usersBasketProductsList.get(userID).setUserName(userDataCache.getUserProfileData(userID).getName());
                                userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYUSERPHONE);
                                sendMessage.setText("Укажите свой номер телефона:\nСейчас: " + usersBasketProductsList.get(userID).getPhoneNumber());
                                sendMessage.setReplyMarkup(new DeliveryPhoneNumber().getDeliveryPhoneNumberKeyboard(usersBasketProductsList.get(userID)));
                            } else
                                sendMessage.setText("Вы еще не указали своё имя в настройках.\nЧтобы сделать это, пожалуйста, перейдите в настройки.");
                        } else if (update.getMessage().getText().matches("( *[a-zA-ZA-Яa-я]* *){1,3}")) {
                            sendMessage.setText("Данные успешно сохранены!");
                            execute(sendMessage);
                            usersBasketProductsList.get(userID).setUserName(update.getMessage().getText());
                            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYUSERPHONE);
                            sendMessage.setText("Укажите свой номер телефона:\nСейчас: " + usersBasketProductsList.get(userID).getPhoneNumber());
                            sendMessage.setReplyMarkup(new DeliveryPhoneNumber().getDeliveryPhoneNumberKeyboard(usersBasketProductsList.get(userID)));
                        } else sendMessage.setText("Пожалуйста, укажите свое настоящее имя");
                    } else if (update.getMessage().getText().equals("Номер из настроек" + Emojis.GEAR) && userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERPHONE) {
                        if (!userDataCache.getUserProfileData(userID).getPhoneNumber().equals("не указан")) {
                            sendMessage.setText("Данные успешно сохранены!");
                            execute(sendMessage);
                            usersBasketProductsList.get(userID).setPhoneNumber(userDataCache.getUserProfileData(userID).getPhoneNumber());
                            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYCONFIRM);
                            deliveryConfirmMessage(usersBasketProductsList.get(userID));
                        } else
                            sendMessage.setText("Вы еще не указали свой номер телефона в настройках.\nЧтобы сделать это, пожалуйста, перейдите в настройки.");
                    } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERPHONE && update.getMessage().getText().equals(Emojis.ARROW_LEFT + "Назад")) {
                        sendMessage.setReplyMarkup(new DeliveryPhoneNumber().getDeliveryPhoneNumberKeyboard(usersBasketProductsList.get(userID)));
                        sendMessage.setText("Укажите свой номер телефона:\nСейчас: " + usersBasketProductsList.get(userID).getPhoneNumber());
                    } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYUSERPHONE) {
                        if (update.getMessage().getText().matches("(\\+?)\\d{11}")) {
                            sendMessage.setText("Данные успешно сохранены!");
                            execute(sendMessage);
                            usersBasketProductsList.get(userID).setPhoneNumber(update.getMessage().getText());
                            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYCONFIRM);
                            deliveryConfirmMessage(usersBasketProductsList.get(userID));
                        } else sendMessage.setText("Пожалуйста, укажите свой настоящий телефон");
                    } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.DELIVERYCONFIRM && update.getMessage().getText().equals(Emojis.WHITE_CHECK_MARK + "Подтвердить и отправить")) {
                        products = dbService.getProductRepository().findAll().iterator();
                        boolean isAnyCollision = false;
                        for (Product productInfo : userDataCache.getUserProfileData(userID).getCurrentUserBasket()) {
                            while (products.hasNext()) {
                                Product product = products.next();
                                if (productInfo.getProductName().equals(product.getProductName())) {
                                    if (productInfo.getProductAmount() > product.getProductAmount()) {
                                        if (product.getProductAmount() != 0)
                                            sendMessage.setText("Кто-то быстрее вас заказал \"" + productInfo.getProductName() + "\"\nСейчас в наличии есть " + product.getProductAmount());
                                        else
                                            sendMessage.setText("Кто-то быстрее вас заказал \"" + productInfo.getProductName() + "\"\nК сожалению, данного товара сейчас нет в наличии");
                                        execute(sendMessage);
                                        isAnyCollision = true;
                                    }
                                }
                            }
                        }
                        if (!isAnyCollision) {
                            ConfirmedOrder confirmedOrder = new ConfirmedOrder();
                            BasketProductsList basket = usersBasketProductsList.get(userID);
                            confirmedOrder.setUserID(userID);
                            String listOfProducts = "\nЗаказанные товары:\n";
                            for (Product productInfo : userDataCache.getUserProfileData(userID).getCurrentUserBasket()) {
                                listOfProducts += (productInfo.getProductName() + "\nКол-во: " + productInfo.getProductAmount() + "\nЦена: " + productInfo.getPrice() + "\u20BD\n");
                            }
                            Date date = new Date(update.getMessage().getDate() * 1000L);
                            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            formatDate.setTimeZone(TimeZone.getTimeZone("GMT+5"));
                            confirmedOrder.setOrderTime(formatDate.format(date));
                            confirmedOrder.setOrderProducts(listOfProducts + "\n<b>Итоговая сумма:</b> " + basket.getTotalCost() + "\u20BD");
                            String deliveryCost;
                            if (basket.getDeliveryCost() == 0)
                                deliveryCost = "бесплатно";
                            else deliveryCost = basket.getDeliveryCost() + "\u20BD";
                            confirmedOrder.setOrderDetails("Адрес доставки: " + basket.getUserAddress() + ", " + basket.getUserAddressDetails() + "\nСтоимость доставки: " + deliveryCost + "\nПокупатель: " + basket.getUserName() + "\nТелефон: " + basket.getPhoneNumber());
                            userDataCache.getUserProfileData(userID).getConfirmedOrderList().add(0, confirmedOrder);
                            dbService.getConfirmedOrderRepository().save(confirmedOrder);
                            for (Map.Entry<String, String> entry : userDataCache.getUserProfileData(userID).getCurrentBasketButtonState().entrySet()) {
                                entry.setValue("notAdded");
                            }
                            //Отправка Сане
//                            sendMessage.enableHtml(true);
//                            sendMessage.setText("<b>У вас новый заказ!</b>\n\nУникальный номер: " + confirmedOrder.getId() + "\nВремя, когда был совершен заказ: " + confirmedOrder.getOrderTime() + "\n" + confirmedOrder.getOrderDetails() + "\n" + confirmedOrder.getOrderProducts());
//                            sendMessage.setChatId(String.valueOf(1858217051));
//                            sendMessage.enableNotification();
//                            execute(sendMessage);
                            //Отправка Тохе
//                            sendMessage.enableHtml(true);
//                            sendMessage.setText("<b>У вас новый заказ!</b>\n\nУникальный номер: " + confirmedOrder.getId() + "\nВремя, когда был совершен заказ: " + confirmedOrder.getOrderTime() + "\n" + confirmedOrder.getOrderDetails() + "\n" + confirmedOrder.getOrderProducts());
//                            sendMessage.setChatId(String.valueOf(1074886968));
//                            sendMessage.enableNotification();
//                            execute(sendMessage);
//                            sendMessage.setText("Если покупатель поменял свой заказ и купил другое количество товара или другие товары, то вам нужно поменять их текущее значение с помощью команды /add");
//                            execute(sendMessage);
                            //Отправка мне
                            sendMessage.enableHtml(true);
                            sendMessage.setText("<b>У вас новый заказ!</b>\n\nУникальный номер: " + confirmedOrder.getId() + "\nВремя, когда был совершен заказ: " + confirmedOrder.getOrderTime() + "\n" + confirmedOrder.getOrderDetails() + "\n" + confirmedOrder.getOrderProducts());
                            sendMessage.setChatId(String.valueOf(1027947243));
                            sendMessage.enableNotification();
                            execute(sendMessage);
                            sendMessage.setText("Если покупатель поменял свой заказ и купил другое количество товара или другие товары, то вам нужно поменять их текущее значение с помощью команды /add");
                            execute(sendMessage);
                            while (products.hasNext()) {
                                Product product = products.next();
                                for (Product productInfo : userDataCache.getUserProfileData(userID).getCurrentUserBasket()) {
                                    if (productInfo.getProductName().equals(product.getProductName())) {
                                        int newProductAmount = product.getProductAmount() - productInfo.getProductAmount();
                                        dbService.getProductRepository().updateProductSetProductAmountForProductName(newProductAmount, product.getProductName());
                                    }
                                }
                            }
                            userDataCache.getUserProfileData(userID).getCurrentUserBasket().clear();
                            usersBasketProductsList.remove(userID);
                            userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.HOME);
                            sendMessage.setChatId(String.valueOf(chatID));
                            sendMessage.enableHtml(true);
                            sendMessage.setText("Поздравляем, вы успешно оформили заказ" + Emojis.RELAXED + "\nНаш человек свяжется с вами в ближайшее время и уточнит время доставки.\nСпасибо, что выбрали Пиво и Дудки!");
                            sendMessage.setReplyMarkup(new Home().getMainMenuKeyboard());
                        } else
                            sendMessage.setText("Пожалуйста, поменяйте количество товара в корзине и попробуйте еще раз!");
                    } else if (((update.getMessage().getText().equals(Emojis.SHOPPING_TROLLEY + "Корзина") || update.getMessage().getText().equals("/basket")) || update.getMessage().getText().equals(Emojis.ARROW_LEFT + "Назад")) && userDataCache.getUsersBotStates().get(userID) == NavigationBotStates.BASKET) {
                        if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() != 0) {
                            if (userDataCache.getUserProfileData(userID).getMessageID() != 0) {
                                DeleteMessage deleteMessage = new DeleteMessage();
                                deleteMessage.setChatId(String.valueOf(chatID));
                                deleteMessage.setMessageId(userDataCache.getUserProfileData(userID).getMessageID());
                                execute(deleteMessage);
                                deleteMessage.setChatId(String.valueOf(chatID));
                                deleteMessage.setMessageId(userDataCache.getUserProfileData(userID).getMessageID() - 1);
                                execute(deleteMessage);
                            }
                            execute(new BasketCallbackQueryHandler().sendBasketList(chatID));
                            userDataCache.getUserProfileData(userID).setMessageID(execute(usersBasketProductsList.get(userID).sendBasketList(chatID, userID)).getMessageId());
                        } else {
                            sendMessage = new SendMessage();
                            sendMessage.enableMarkdown(true);
                            sendMessage.setChatId(String.valueOf(chatID));
                            sendMessage.setReplyMarkup(new Basket().getBasketKeyboard());
                            sendMessage.setText("К сожалению, вы еще ничего не добавили в корзину. Воспользуйтесь командой /catalog , чтобы посмотреть товары нашего магазина!");
                        }
                    } else if (update.getMessage().getText().equals("/add") && (chatID == 1858217051)) {
                        if (addingProductAmountIDMapSanya.size() > 0) {
                            DeleteMessage deleteMessage = new DeleteMessage();
                            for (Map.Entry<String, Integer> entry : addingProductAmountIDMapSanya.entrySet()) {
                                deleteMessage.setMessageId(entry.getValue());
                                deleteMessage.setChatId(String.valueOf(1858217051));
                                execute(deleteMessage);
                            }
                            addingProductAmountIDMapSanya.clear();
                        }
                        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.ADDINGPRODUCTAMOUNT);
                        products = dbService.getProductRepository().findAll().iterator();
                        InlineKeyboardMarkup inlineKeyboardMarkup;
                        InlineKeyboardButton changingButton;
                        List<List<InlineKeyboardButton>> row;
                        List<InlineKeyboardButton> row1;
                        sendMessage.enableHtml(true);
                        sendMessage.setText("<b>Список доступных товаров:</b>");
                        sendMessage.setChatId(String.valueOf(1858217051));
                        execute(sendMessage);
                        while (products.hasNext()) {
                            Product product = products.next();
                            inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            changingButton = new InlineKeyboardButton();
                            row = new ArrayList<>();
                            row1 = new ArrayList<>();
                            sendMessage.setText("Товар: " + product.getProductName() + "\nТекущее количество: " + product.getProductAmount());
                            changingButton.setText("Изменить");
                            changingButton.setCallbackData("change" + product.getConfigurationName());
                            row1.add(changingButton);
                            row.add(row1);
                            inlineKeyboardMarkup.setKeyboard(row);
                            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                            addingProductAmountIDMapSanya.put(product.getProductName(), execute(sendMessage).getMessageId());
                        }
                        sendMessage = null;
                    } else if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.ADDINGPRODUCTAMOUNT && update.getMessage().hasText()) {
                        if (userID == 1858217051) {
                            String newProductAmount = update.getMessage().getText();
                            sendMessage.setChatId(String.valueOf(1858217051));
                            if (newProductAmount.matches("\\d{1,2}")) {
                                Product product = dbService.getProductRepository().findProductByConfigurationName(addingState);
                                if (Integer.parseInt(newProductAmount) != product.getProductAmount()) {
                                    EditMessageText editMessageText = new EditMessageText();
                                    editMessageText.setText("Товар: " + product.getProductName() + "\nТекущее количество: " + newProductAmount);
                                    editMessageText.setMessageId(addingProductAmountIDMapSanya.get(product.getProductName()));
                                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                    InlineKeyboardButton changingButton = new InlineKeyboardButton();
                                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                                    List<List<InlineKeyboardButton>> row = new ArrayList<>();
                                    changingButton.setText("Изменить");
                                    changingButton.setCallbackData("change" + addingState);
                                    row1.add(changingButton);
                                    row.add(row1);
                                    inlineKeyboardMarkup.setKeyboard(row);
                                    editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                                    editMessageText.setChatId(String.valueOf(1858217051));
                                    execute(editMessageText);
                                }
                                dbService.getProductRepository().updateProductSetProductAmountForConfigurationName(Integer.parseInt(newProductAmount), addingState);
                                sendMessage.setText("Данные успешно сохранены!");
                            } else sendMessage.setText("Некорректный ввод. Укажите только число!");
                        }
                    }
                }
                if (((update.getMessage().hasLocation()) || update.getMessage().hasText()) && userDataCache.getUsersBotStates().get(userID) == NavigationBotStates.DELIVERYLOCATION) {
                    LocationHandler locationHandler = new LocationHandler(telegramFacade.getMainUpdateInfo(), sendMessage, userID, update.getMessage(), dbService);
                    if (update.getMessage().hasText()) {
                        if (!update.getMessage().getText().equals(Emojis.ARROW_LEFT + "Назад") && !update.getMessage().getText().equals("Изменить")) {
                            Location location;
                            if (sendMessage.getText() == null) sendMessage.setText("");
                            if (sendMessage.getText().equals("Есть адрес")) {
                                sendMessage.setText("");
                                location = locationHandler.locationHandle(userDataCache.getUserProfileData(userID).getAddress());
                            } else
                                location = locationHandler.locationHandle(update.getMessage().getText());
                            if (location != null) {
                                sendMessage.setText("Данные успешно сохранены!");
                                execute(sendMessage);
                                execute(deliveryZonesHandler.deliveryLocationHandle(location, chatID, usersBasketProductsList.get(userID), userDataCache, locationHandler));
                                sendMessage = null;
                            } else {
                                if (sendMessage.getText().equals("Вы еще не указали свой адрес в настройках.\nЧтобы сделать это, пожалуйста, перейдите в настройки."))
                                    sendMessage.setChatId(String.valueOf(chatID));
                                else
                                    sendMessage.setText("Адрес указан неправильно!\n(должна быть указана улица и номер дома)\nПопробуйте еще раз");
                                sendMessage.setChatId(String.valueOf(chatID));
                            }
                        }
                    } else {
                        Location deliveryCoordinates = update.getMessage().getLocation();
                        locationHandler.setAddress(locationHandler.locationHandle(deliveryCoordinates.getLongitude(), deliveryCoordinates.getLatitude()));
                        sendMessage.setText("Данные успешно сохранены!");
                        execute(sendMessage);
                        execute(deliveryZonesHandler.deliveryLocationHandle(deliveryCoordinates, chatID, usersBasketProductsList.get(userID), userDataCache, locationHandler));
                        sendMessage = null;
                    }
                }
                if (userDataCache.getUsersCurrentBotState(userID) == NavigationBotStates.ORDERS && (update.getMessage().getText().equals(Emojis.PACKAGE + "Заказы") || update.getMessage().getText().equals("/orders"))) {
                    if (ordersMessageIDMap.get(userID) != null) {
                        DeleteMessage deleteMessage = new DeleteMessage();
                        deleteMessage.setChatId(String.valueOf(chatID));
                        deleteMessage.setMessageId(ordersMessageIDMap.get(userID));
                        execute(deleteMessage);
                        deleteMessage.setMessageId(ordersMessageIDMap.get(userID) - 1);
                        execute(deleteMessage);
                        ordersMessageIDMap.remove(userID);
                    }
                    if (userDataCache.getUserProfileData(userID).getConfirmedOrderList().size() > 0) {
                        sendMessage.enableHtml(true);
                        sendMessage.setText("<b>История заказов:</b>");
                        sendMessage.setReplyMarkup(new Orders().getOrdersKeyboard());
                        execute(sendMessage);
                        sendMessage = usersOrdersMap.get(userID).sendOrder(sendMessage, userDataCache.getUserProfileData(userID).getConfirmedOrderList());
                        sendMessage.enableHtml(true);
                        ordersMessageIDMap.put(userID, execute(sendMessage).getMessageId());
                        sendMessage = null;
                    } else {
                        sendMessage.setText("К сожалению, вы еще ничего не заказывали" + Emojis.PENSIVE);
                    }
                }
            }
        }
        uniqueUpdateID = update.getUpdateId();
        return sendMessage;
    }

    private void sendDeliveryLocationDetails() throws TelegramApiException {
        execute(new BasketCallbackQueryHandler().nextToDeliveryMessage(chatID));
        execute(new BasketCallbackQueryHandler().nextToDeliveryPhoto(chatID));
        sendMessage.setText("Отправьте свой адрес для доставки и стоимость определится автоматически.");
        sendMessage.setChatId(String.valueOf(chatID));
        sendMessage.setReplyMarkup(new DeliveryLocation().getDeliveryLocationKeyboard(usersBasketProductsList.get(userID)));
        execute(sendMessage);
        sendMessage.setText("Укажите адрес доставки (улица и номер дома):\nСейчас: " + usersBasketProductsList.get(userID).getUserAddress());
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.DELIVERYLOCATION);
    }

    @SneakyThrows
    public void executePipesList(long chatID) {
        if (productsMessageIDMap.get(userID) != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            HashMap<String, Integer> userMap;
            for (Map.Entry<Long, HashMap<String, Integer>> entry : productsMessageIDMap.entrySet()) {
                if (entry.getKey() == userID) {
                    userMap = entry.getValue();
                    for (Map.Entry<String, Integer> userEntry : userMap.entrySet()) {
                        if (!userEntry.getKey().matches("(.*)Beer")) {
                            deleteMessage.setMessageId(userEntry.getValue());
                            deleteMessage.setChatId(String.valueOf(chatID));
                            execute(deleteMessage);
                        }
                    }
                }
            }
        }
        sendMessage.setChatId(String.valueOf(chatID));
        execute(sendMessage);
        products = dbService.getProductRepository().findAll().iterator();
        while (products.hasNext()) {
            Product product = products.next();
            userDataCache.getUserProfileData(userID).getCurrentBasketButtonState().putIfAbsent(product.getProductName(), "notAdded");
            if (product.getProductAmount() > 0 && !product.getConfigurationName().matches("(.*)Beer")) {
                makeSendingProductPhoto(chatID, product);
            }
        }
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.CATALOG);
        sendMessage = null;
    }

    @SneakyThrows
    public void executeBeerList(long chatID) {
        if (productsMessageIDMap.get(userID) != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            HashMap<String, Integer> userMap;
            for (Map.Entry<Long, HashMap<String, Integer>> entry : productsMessageIDMap.entrySet()) {
                if (entry.getKey() == userID) {
                    userMap = entry.getValue();
                    for (Map.Entry<String, Integer> userEntry : userMap.entrySet()) {
                        if (userEntry.getKey().matches("(.*)Beer")) {
                            deleteMessage.setMessageId(userEntry.getValue());
                            deleteMessage.setChatId(String.valueOf(chatID));
                            execute(deleteMessage);
                        }
                    }
                }
            }
        }
        sendMessage.setChatId(String.valueOf(chatID));
        execute(sendMessage);
        products = dbService.getProductRepository().findAll().iterator();
        while (products.hasNext()) {
            Product product = products.next();
            userDataCache.getUserProfileData(userID).getCurrentBasketButtonState().putIfAbsent(product.getProductName(), "notAdded");
            if (product.getProductAmount() > 0 && product.getConfigurationName().matches("(.*)Beer")) {
                makeSendingProductPhoto(chatID, product);
            }
        }
        userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.CATALOG);
        sendMessage = null;
    }

    private void makeSendingProductPhoto(long chatID, Product product) throws TelegramApiException {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> row = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton buyButton = new InlineKeyboardButton();
        InlineKeyboardButton basketButton = new InlineKeyboardButton();
        InlineKeyboardButton basketDeleteButton = new InlineKeyboardButton();
        InlineKeyboardButton basketDecorateButton = new InlineKeyboardButton();
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(product.getPhoto()));
        sendPhoto.setChatId(String.valueOf(chatID));
        sendPhoto.setCaption(product.getProductName() + product.getCaption());
        buyButton.setText("Купить \u2014 " + product.getPrice() + "\u20BD");
        buyButton.setCallbackData(product.getConfigurationName() + "_" + product.getPrice());
        row1.add(buyButton);
        if (userDataCache.getUserProfileData(userID).getCurrentBasketButtonState().get(product.getProductName()).equals("notAdded")) {
            basketButton.setText("Добавить в корзину " + Emojis.SHOPPING_TROLLEY);
            basketButton.setCallbackData(product.getConfigurationName() + "Added");
            row2.add(basketButton);
            row.add(row1);
            row.add(row2);
        } else if (userDataCache.getUserProfileData(userID).getCurrentBasketButtonState().get(product.getProductName()).equals("added")) {
            basketDeleteButton.setText("Удалить?" + Emojis.X);
            basketDeleteButton.setCallbackData("deleteFromBasket" + product.getConfigurationName());
            basketDecorateButton.setText("В корзине" + Emojis.WHITE_CHECK_MARK);
            basketDecorateButton.setCallbackData("nothing");
            row2.add(basketDecorateButton);
            row2.add(basketDeleteButton);
            row.add(row1);
            row.add(row2);
        }
        inlineKeyboardMarkup.setKeyboard(row);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        productsMessageIDMap.get(userID).put(product.getConfigurationName(), execute(sendPhoto).getMessageId());
    }

    @SneakyThrows
    public void changingButtonMarkup(String callbackQueryData) {
        String[] splittedCallbackData;
        if (callbackQueryData.matches("(.*)Added")) {
            splittedCallbackData = callbackQueryData.split("Added", 2);
            messageReplyMarkup.setMessageId(productsMessageIDMap.get(userID).get(splittedCallbackData[0]));
        } else if (callbackQueryData.matches("deleteFromBasket(.*)")) {
            splittedCallbackData = callbackQueryData.split("deleteFromBasket", 2);
            messageReplyMarkup.setMessageId(productsMessageIDMap.get(userID).get(splittedCallbackData[1]));
        }
    }

    @SneakyThrows
    public void deliveryConfirmMessage(BasketProductsList basket) {
        sendMessage.enableHtml(true);
        sendMessage.setText("<b>Информация о заказе:</b>");
        execute(sendMessage);
        String listOfProducts = "";
        String deliveryCost;
        for (Product productInfo : userDataCache.getUserProfileData(userID).getCurrentUserBasket()) {
            Product product = productInfo;
            listOfProducts += (product.getProductName() + "\nКол-во: " + product.getProductAmount() + "\nЦена: " + product.getPrice() + "\u20BD\n\n");
        }
        if (basket.getDeliveryCost() == 0)
            deliveryCost = "бесплатно";
        else deliveryCost = basket.getDeliveryCost() + "\u20BD";
        sendMessage.setText("Адрес доставки: " + basket.getUserAddress() + ", " + basket.getUserAddressDetails() + "\nСтоимость доставки: " + deliveryCost + "\nПокупатель: " + basket.getUserName() + "\nТелефон: " + basket.getPhoneNumber() + "\n\nЗаказанные товары:\n" + listOfProducts + "<b>Итоговая сумма:</b> " + basket.getTotalCost() + "\u20BD" + "\n\nНажмите \"" + Emojis.WHITE_CHECK_MARK + "Подтвердить и отправить\", если все верно. Пожалуйста, будьте внимательны, после подтверждения заказа его больше нельзя будет изменить!");
        sendMessage.setReplyMarkup(new DeliveryConfirm().getDeliveryConfirmKeyboard());
    }

    @Override
    public String getBotPath() {
        return this.botPath;
    }
}
