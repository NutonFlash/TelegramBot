package ru.TelegramBot.Orders;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.TelegramBot.DataBase.model.ConfirmedOrder;
import ru.TelegramBot.NavigationButtons.Emojis;

import java.util.ArrayList;
import java.util.List;

public class OrdersList {
    final InlineKeyboardButton previousOrderButton = new InlineKeyboardButton();
    final InlineKeyboardButton nextOrderButton = new InlineKeyboardButton();
    final InlineKeyboardButton currentPageButton = new InlineKeyboardButton();
    List<List<InlineKeyboardButton>> row;
    List<InlineKeyboardButton> row1;
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    int currentPageNumber;

    public OrdersList() {
        previousOrderButton.setText(Emojis.ARROW_BACKWARD.toString());
        previousOrderButton.setCallbackData("previousOrder");
        nextOrderButton.setText(Emojis.ARROW_FORWARD.toString());
        nextOrderButton.setCallbackData("nextOrder");
    }

    public SendMessage sendOrder(SendMessage sendMessage, ArrayList<ConfirmedOrder> userConfirmedOrderList) {
        currentPageNumber = 1;
        sendMessage.setText("Уникальный номер: " + userConfirmedOrderList.get(0).getId() + "\nВремя, когда был совершен заказ: " + userConfirmedOrderList.get(0).getOrderTime() + "\n" + userConfirmedOrderList.get(0).getOrderDetails() + "\n" + userConfirmedOrderList.get(0).getOrderProducts());
        currentPageButton.setText(currentPageNumber + "/" + userConfirmedOrderList.size());
        currentPageButton.setCallbackData("nothing");
        row1 = new ArrayList<>();
        row = new ArrayList<>();
        row1.add(previousOrderButton);
        row1.add(currentPageButton);
        row1.add(nextOrderButton);
        row.add(row1);
        inlineKeyboardMarkup.setKeyboard(row);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    public EditMessageText editOrder(String callbackQuery, ArrayList<ConfirmedOrder> userConfirmedOrderList) {
        if (callbackQuery.equals("previousOrder")) {
            if (currentPageNumber > 1) currentPageNumber--;
            else currentPageNumber = userConfirmedOrderList.size();
        }
        if (callbackQuery.equals("nextOrder")) {
            if (currentPageNumber == userConfirmedOrderList.size())
                currentPageNumber = 1;
            else currentPageNumber++;
        }
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText("Уникальный номер: " + userConfirmedOrderList.get(currentPageNumber - 1).getId() + "\nВремя, когда был совершен заказ: " + userConfirmedOrderList.get(currentPageNumber - 1).getOrderTime() + "\n" + userConfirmedOrderList.get(currentPageNumber - 1).getOrderDetails() + "\n" + userConfirmedOrderList.get(currentPageNumber - 1).getOrderProducts());
        currentPageButton.setText(currentPageNumber + "/" + userConfirmedOrderList.size());
        currentPageButton.setCallbackData("nothing");
        row1 = new ArrayList<>();
        row = new ArrayList<>();
        row1.add(previousOrderButton);
        row1.add(currentPageButton);
        row1.add(nextOrderButton);
        row.add(row1);
        inlineKeyboardMarkup.setKeyboard(row);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        return editMessageText;
    }
}
