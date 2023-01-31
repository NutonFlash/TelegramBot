package ru.TelegramBot.DeliveryZones;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import ru.TelegramBot.Basket.BasketProductsList;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.UserDataCache;
import ru.TelegramBot.Handlers.Location.LocationHandler;
import ru.TelegramBot.NavigationButtons.OrderSettings.DeliveryUserName;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import static java.awt.geom.Path2D.WIND_NON_ZERO;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryZonesHandler {
    final Path2D greenZone;
    final Path2D purpleZone;

    public DeliveryZonesHandler() {
        greenZone = new Path2D.Double();
        double[] greenZoneXPoint = {60.560908, 60.565116, 60.575211, 60.578580, 60.582141, 60.600395, 60.601711, 60.614920, 60.630397, 60.640466, 60.626405, 60.628034, 60.632316, 60.642274, 60.639030, 60.636203, 60.633618, 60.629455, 60.621332, 60.611439, 60.607566, 60.603875, 60.598198, 60.585823, 60.580804, 60.561668, 60.561325, 60.562716, 60.561958};
        double[] greenZoneYPoint = {56.846506, 56.84775, 56.85607, 56.85626, 56.85556, 56.85718, 56.85618, 56.85741, 56.84540, 56.81817, 56.817214, 56.816138, 56.804461, 56.799819, 56.792429, 56.789571, 56.788257, 56.787364, 56.787999, 56.790438, 56.790953, 56.790953, 56.790217, 56.825266, 56.824879, 56.825460, 56.828357, 56.842054, 56.845291};
        greenZone.moveTo(greenZoneXPoint[0], greenZoneYPoint[0]);
        greenZone.setWindingRule(WIND_NON_ZERO);
        for (int i = 1; i < greenZoneYPoint.length; i++) {
            greenZone.lineTo(greenZoneXPoint[i], greenZoneYPoint[i]);
        }
        greenZone.closePath();
        purpleZone = new Path2D.Double();
        double[] purpleZoneXPoint = {60.551919, 60.550160, 60.549691, 60.550032, 60.549559, 60.561666, 60.611544, 60.613792, 60.634318, 60.652087, 60.674850, 60.673190, 60.668933, 60.668820, 60.680914, 60.688381, 60.691144, 60.692803, 60.694774, 60.697076, 60.703042, 60.709004, 60.713612, 60.711915, 60.709775, 60.709314, 0.711371, 60.704026, 60.702141, 60.695013, 60.669348, 60.655662, 60.652517, 60.642023, 0.640163, 60.655453, 60.648007, 60.628628, 60.628220, 60.614723, 60.595232, 60.584759, 60.527791, 60.530223, 60.478871, 60.485311, 60.491577, 60.491577, 60.517506, 60.515418, 60.528056, 60.540995, 60.553051};
        double[] purpleZoneYPoint = {56.842276, 56.856088, 56.859674, 56.863380, 56.871271, 56.883528, 56.887529, 56.887117, 56.894095, 56.878639, 56.865519, 56.853541, 56.848185, 56.841980, 56.842129, 56.843200, 56.843013, 56.843184, 56.843056, 56.843600, 56.844881, 56.845066, 56.842979, 56.834961, 56.834850, 56.829851, 56.823391, 56.821057, 56.816894, 56.812900, 56.818007, 56.812691, 56.806501, 56.799895, 56.795336, 56.781427, 56.780407, 56.769871, 56.767183, 56.768325, 56.769317, 56.753776, 56.768319, 56.772667, 56.797419, 56.813098, 56.822659, 56.822659, 56.837320, 56.841701, 56.836970, 56.840375, 56.841933};
        purpleZone.moveTo(purpleZoneXPoint[0], purpleZoneYPoint[0]);
        for (int i = 1; i < purpleZoneYPoint.length; i++) {
            purpleZone.lineTo(purpleZoneXPoint[i], purpleZoneYPoint[i]);
        }
        purpleZone.closePath();
    }

    public SendMessage deliveryLocationHandle(Location location, long chatID, BasketProductsList basket, UserDataCache userDataCache, LocationHandler locationHandler) {
        Point2D point = new Point2D.Double(location.getLongitude(), location.getLatitude());
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chatID));
        int totalCost = basket.getTotalCost();
        if (greenZone.contains(point)) {
            sendMessage.setReplyMarkup(new DeliveryUserName().getDeliveryUserNameKeyboard(basket));
            sendMessage.setText("Ваш адрес для доставки в <b>зелёной зоне</b>!\n\nУкажите свое имя:\nСейчас: " + basket.getUserName());
            userDataCache.setUsersCurrentBotState(basket.getUserID(), NavigationBotStates.DELIVERYUSERNAME);
            basket.setUserAddress(locationHandler.getAddress());
            if (totalCost < 1000)
                basket.setDeliveryCost(100);
            else if (totalCost < 1500)
                basket.setDeliveryCost(50);
            else basket.setDeliveryCost(0);
            basket.setTotalCost(totalCost + basket.getDeliveryCost());
            basket.setDeliveryZone("green");
        } else if (purpleZone.contains(point)) {
            sendMessage.setText("Ваш адрес для доставки в <b>фиолетовой зоне</b>!\n\nУкажите свое имя:\nСейчас: " + basket.getUserName());
            userDataCache.setUsersCurrentBotState(basket.getUserID(), NavigationBotStates.DELIVERYUSERNAME);
            sendMessage.setReplyMarkup(new DeliveryUserName().getDeliveryUserNameKeyboard(basket));
            basket.setUserAddress(locationHandler.getAddress());
            if (totalCost < 1000)
                basket.setDeliveryCost(200);
            else if (totalCost < 1500)
                basket.setDeliveryCost(50);
            else basket.setDeliveryCost(0);
            basket.setTotalCost(totalCost + basket.getDeliveryCost());
            basket.setDeliveryZone("purple");
        } else
            sendMessage.setText("К сожалению, место, которое вы указали, не входит в зону доставки. Доступные зоны указаны на картинке выше.");
        return sendMessage;
    }
}