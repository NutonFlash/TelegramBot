package ru.TelegramBot.Handlers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.MainUpdateInfo;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.DataBase.DBService;
import ru.TelegramBot.DataBase.model.Product;
import ru.TelegramBot.NavigationButtons.Emojis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CatalogCallbackQueryHandler {

    final Iterator<Product> productList;
    final long userID;
    final UserDataCache userDataCache;
    final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    final InlineKeyboardButton buyButton = new InlineKeyboardButton();
    final InlineKeyboardButton basketDecorateButton = new InlineKeyboardButton();
    final InlineKeyboardButton basketDeleteButton = new InlineKeyboardButton();
    final InlineKeyboardButton basketButton = new InlineKeyboardButton();
    List<List<InlineKeyboardButton>> row;
    List<InlineKeyboardButton> row1;
    List<InlineKeyboardButton> row2;
    EditMessageReplyMarkup messageReplyMarkup;

    final DBService dbService;

    public CatalogCallbackQueryHandler(MainUpdateInfo mainUpdateInfo, Iterator<Product> productList, DBService dbService) {
        this.userDataCache = mainUpdateInfo.getUserDataCache();
        this.userID = mainUpdateInfo.getUserID();
        this.productList = productList;
        this.dbService = dbService;
    }

    public EditMessageReplyMarkup catalogRequestHandle(String callbackData) {
        if (callbackData.matches(".*_.*")) {
            addToBasketHandle(buyButtonHandle(callbackData, false) + "Added", false);
        } else if (callbackData.matches(".*Added")) {
            addToBasketHandle(callbackData, true);
        } else if (callbackData.matches("deleteFromBasket.*")) {
            deleteFromBasket(callbackData, true);
        } else if (callbackData.matches("forEditingReplyMarkup.*")) {
            deleteFromBasket(callbackData, false);
        }
        return messageReplyMarkup;
    }

    private String buyButtonHandle(String callbackData, boolean forAddingToBasket) {
        String[] splittedCallbackData;
        if (forAddingToBasket) splittedCallbackData = callbackData.split("Added", 2);
        else splittedCallbackData = callbackData.split("_", 2);
        Product productFromDB = dbService.getProductRepository().findProductByConfigurationName(splittedCallbackData[0]);
        productFromDB.setProductAmount(1);
        boolean productAdded = false;
        for (Product product : userDataCache.getUserProfileData(userID).getCurrentUserBasket()) {
            if (product.getProductName().equals(productFromDB.getProductName())) {
                productAdded = true;
                break;
            }
        }
        if (!productAdded)
            userDataCache.getUserProfileData(userID).getCurrentUserBasket().add(productFromDB);
        if (!forAddingToBasket) userDataCache.setUsersCurrentBotState(userID, NavigationBotStates.BASKET);
        return splittedCallbackData[0];
    }

    private void addToBasketHandle(String callbackData, boolean forAddingButton) {
        String[] splittedCallbackData = callbackData.split("Added", 2);
        row1 = new ArrayList<>();
        row2 = new ArrayList<>();
        row = new ArrayList<>();
        Product product = dbService.getProductRepository().findProductByConfigurationName(splittedCallbackData[0]);
        buyButton.setText("Купить \u2014 " + product.getPrice() + "\u20BD");
        buyButton.setCallbackData(product.getConfigurationName() + "_" + product.getPrice());
        basketDecorateButton.setText("В корзине " + Emojis.WHITE_CHECK_MARK);
        basketDecorateButton.setCallbackData("nothing");
        basketDeleteButton.setText("Удалить?" + Emojis.X);
        basketDeleteButton.setCallbackData("deleteFromBasket" + product.getConfigurationName());
        row1.add(buyButton);
        row2.add(basketDecorateButton);
        row2.add(basketDeleteButton);
        addingButtons(row, row1, row2);
        buyButtonHandle(callbackData, true);
        if (forAddingButton)
            userDataCache.getUserProfileData(userID).getCurrentBasketButtonState().replace(product.getProductName(), "added");
    }

    public void deleteFromBasket(String callbackData, boolean forCallbackHandler) {
        String[] splittedCallbackData;
        if (forCallbackHandler) splittedCallbackData = callbackData.split("deleteFromBasket", 2);
        else splittedCallbackData = callbackData.split("forEditingReplyMarkup", 2);
        row1 = new ArrayList<>();
        row2 = new ArrayList<>();
        row = new ArrayList<>();
        Product product = dbService.getProductRepository().findProductByConfigurationName(splittedCallbackData[1]);
        buyButton.setText("Купить \u2014 " + product.getPrice() + "\u20BD");
        buyButton.setCallbackData(product.getConfigurationName() + "_" + product.getPrice());
        basketButton.setText("Добавить в корзину " + Emojis.SHOPPING_TROLLEY);
        basketButton.setCallbackData(product.getConfigurationName() + "Added");
        row1.add(buyButton);
        row2.add(basketButton);
        addingButtons(row, row1, row2);
        userDataCache.getUserProfileData(userID).getCurrentBasketButtonState().put(product.getProductName(), "notAdded");
    }

    private void addingButtons(List<List<InlineKeyboardButton>> row, List<InlineKeyboardButton> row1, List<InlineKeyboardButton> row2) {
        row.add(row1);
        row.add(row2);
        inlineKeyboardMarkup.setKeyboard(row);
        messageReplyMarkup = new EditMessageReplyMarkup();
        messageReplyMarkup.setChatId(String.valueOf(userDataCache.getUserProfileData(userID).getChatID()));
        messageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);
    }
}
