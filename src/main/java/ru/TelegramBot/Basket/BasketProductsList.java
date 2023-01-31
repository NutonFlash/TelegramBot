package ru.TelegramBot.Basket;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.DataBase.model.Product;
import ru.TelegramBot.NavigationButtons.Emojis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasketProductsList {
    final InlineKeyboardButton cancelButton = new InlineKeyboardButton();
    final InlineKeyboardButton lessAmountButton = new InlineKeyboardButton();
    final InlineKeyboardButton moreAmountButton = new InlineKeyboardButton();
    final InlineKeyboardButton amountButton = new InlineKeyboardButton();
    final InlineKeyboardButton previousProductButton = new InlineKeyboardButton();
    final InlineKeyboardButton nextProductButton = new InlineKeyboardButton();
    final InlineKeyboardButton basketSizeButton = new InlineKeyboardButton();
    final InlineKeyboardButton rightOrderButton = new InlineKeyboardButton();
    final InlineKeyboardButton backToCatalogButton = new InlineKeyboardButton();
    final InlineKeyboardButton selectedProductsButton = new InlineKeyboardButton();
    int currentIndex;
    int productAmount;
    int price;
    String photo;
    String caption;
    long userID;
    UserDataCache userDataCache;
    SendPhoto sendPhoto;
    String productName;
    int totalCost;
    String userAddress;
    String userAddressDetails;
    String phoneNumber;
    String userName;
    String dateOfOrder;
    int deliveryCost;
    String deliveryZone;
    EditMessageMedia editMessageMedia = new EditMessageMedia();
    InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
    InlineKeyboardMarkup inlineKeyboardMarkup;
    List<List<InlineKeyboardButton>> row;
    List<InlineKeyboardButton> row1;
    List<InlineKeyboardButton> row2;
    List<InlineKeyboardButton> row3;
    List<InlineKeyboardButton> row4;
    List<InlineKeyboardButton> row5;
    Iterator<Map.Entry<String, Product>> iterator;
    int productNumber;

    public BasketProductsList(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    public SendPhoto sendBasketList(long chatID, long userID) {
        if (userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() != 0) {
            ArrayList<Product> currentBasket = userDataCache.getUserProfileData(userID).getCurrentUserBasket();
            Product productInfo = currentBasket.get(currentBasket.size() - 1);
            productName = productInfo.getProductName();
            caption = productInfo.getCaption();
            price = productInfo.getPrice();
            productAmount = productInfo.getProductAmount();
            productNumber = 1;
            currentIndex = currentBasket.size() - 1;
            photo = productInfo.getPhoto();
            sendPhoto = new SendPhoto();
            sendPhoto.setPhoto(new InputFile(photo));
            sendPhoto.setCaption(productName + caption + "\nСтоимость: " + price + "х" + productAmount + " = " + price * productAmount + "\u20BD");
            sendPhoto.setChatId(String.valueOf(chatID));
            sendPhoto.setReplyMarkup(getInlineKeyboardMarkup(userID));
        }
        return sendPhoto;
    }

    public EditMessageMedia changeProductPhoto(String callbackQueryData, long userID) {
        if (callbackQueryData.equals("previousProduct") || callbackQueryData.equals("nextProduct")) {
            if (callbackQueryData.equals("previousProduct")) {
                --productNumber;
                if (productNumber == 0) {
                    productNumber = userDataCache.getUserProfileData(userID).getCurrentUserBasket().size();
                }
                --currentIndex;
                if (currentIndex == -1) {
                    currentIndex = userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() - 1;
                }
            } else {
                ++productNumber;
                if (productNumber == userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() + 1) {
                    productNumber = 1;
                }
                ++currentIndex;
                if (currentIndex == userDataCache.getUserProfileData(userID).getCurrentUserBasket().size()) {
                    currentIndex = 0;
                }
            }
        } else if (callbackQueryData.matches("deleteProduct.*")) {
            --productNumber;
            if (productNumber == 0)
                productNumber = 1;
            userDataCache.getUserProfileData(userID).getCurrentUserBasket().remove(currentIndex);
            --currentIndex;
            if (currentIndex == -1)
                currentIndex = 0;
        } else if (callbackQueryData.matches(".*Added")) {
            productNumber = 1;
            currentIndex = userDataCache.getUserProfileData(userID).getCurrentUserBasket().size() - 1;
        } else if (callbackQueryData.matches("deleteFromBasket.*")) {
            String[] splittedCallback = callbackQueryData.split("deleteFromBasket");
            int deleteProductIndex = 0;
            for (Product productInfo : userDataCache.getUserProfileData(userID).getCurrentUserBasket()) {
                ++deleteProductIndex;
                if (productInfo.getProductName().equals(splittedCallback[1]))
                    break;
            }
            --productNumber;
            if (productNumber == 0)
                productNumber = 1;
            --currentIndex;
            if (currentIndex == -1)
                currentIndex = 0;
            userDataCache.getUserProfileData(userID).getCurrentUserBasket().remove(deleteProductIndex - 1);
        }
        Product productInfo = userDataCache.getUserProfileData(userID).getCurrentUserBasket().get(currentIndex);
        productName = productInfo.getProductName();
        caption = productInfo.getCaption();
        price = productInfo.getPrice();
        productAmount = productInfo.getProductAmount();
        inputMediaPhoto.setCaption(productName + "\n" + caption + "\nСтоимость: " + price + "х" + productAmount + " = " + price * productAmount + "\u20BD");
        inputMediaPhoto.setMedia(productInfo.getPhoto());
        editMessageMedia.setMedia(inputMediaPhoto);
        editMessageMedia.setMessageId(userDataCache.getUserProfileData(userID).getMessageID());
        editMessageMedia.setChatId(String.valueOf(userDataCache.getUserProfileData(userID).getChatID()));
        editMessageMedia.setReplyMarkup(getInlineKeyboardMarkup(userID));
        return editMessageMedia;
    }


    public InlineKeyboardMarkup getInlineKeyboardMarkup(long userID) {
        totalCost = 0;
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        row1 = new ArrayList<>();
        row2 = new ArrayList<>();
        row3 = new ArrayList<>();
        row4 = new ArrayList<>();
        row5 = new ArrayList<>();
        row = new ArrayList<>();
        for (Product entry : userDataCache.getUserProfileData(userID).getCurrentUserBasket()) {
            totalCost += entry.getPrice() * entry.getProductAmount();
        }
        cancelButton.setText(Emojis.X.toString());
        cancelButton.setCallbackData("deleteProduct");
        lessAmountButton.setText(Emojis.SMALL_RED_TRIANGLE_DOWN.toString());
        lessAmountButton.setCallbackData("lessAmount");
        moreAmountButton.setText(Emojis.SMALL_RED_TRIANGLE.toString());
        moreAmountButton.setCallbackData("moreAmount");
        amountButton.setText(productAmount + " шт.");
        amountButton.setCallbackData("nothing");
        selectedProductsButton.setText("выбранный товар");
        selectedProductsButton.setCallbackData("nothing");
        previousProductButton.setText(Emojis.ARROW_BACKWARD.toString());
        previousProductButton.setCallbackData("previousProduct");
        nextProductButton.setText(Emojis.ARROW_FORWARD.toString());
        nextProductButton.setCallbackData("nextProduct");
        basketSizeButton.setText(productNumber + "/" + userDataCache.getUserProfileData(userID).getCurrentUserBasket().size());
        basketSizeButton.setCallbackData("nothing");
        rightOrderButton.setText("Заказ на " + totalCost + "\u20BD   Оформить? " + Emojis.WHITE_CHECK_MARK);
        rightOrderButton.setCallbackData("nextToDelivery");
        backToCatalogButton.setText("Продолжить покупки");
        backToCatalogButton.setCallbackData("backToCatalog");
        row1.add(cancelButton);
        row1.add(lessAmountButton);
        row1.add(amountButton);
        row1.add(moreAmountButton);
        row5.add(selectedProductsButton);
        row2.add(previousProductButton);
        row2.add(basketSizeButton);
        row2.add(nextProductButton);
        row3.add(rightOrderButton);
        row4.add(backToCatalogButton);
        row.add(row1);
        row.add(row5);
        row.add(row2);
        row.add(row3);
        row.add(row4);
        inlineKeyboardMarkup.setKeyboard(row);
        return inlineKeyboardMarkup;
    }
}
