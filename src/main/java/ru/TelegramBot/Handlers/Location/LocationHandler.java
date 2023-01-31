package ru.TelegramBot.Handlers.Location;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.Cashe.MainUpdateInfo;
import ru.TelegramBot.DataBase.DBService;
import ru.TelegramBot.NavigationButtons.Settings;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationHandler {
    final String TOKEN = "c7fe2946-5a7e-417b-99d7-f831e8b7212a";
    final String yandexLink = "https://geocode-maps.yandex.ru/1.x?format=json&lang=ru_RU&kind=house&apikey=" + TOKEN + "&geocode=";
    DBService dbService;
    JsonObject jsonObject;
    JsonArray jsonArray;
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpPost httpPost;
    HttpGet httpGet;
    HttpResponse response;
    String longitude;
    String latitude;
    String jsonResponse;
    ResponseHandler<String> responseHandler;
    String address;

    public LocationHandler(MainUpdateInfo mainUpdateInfo, SendMessage sendMessage, long userID, Message message, DBService dbService) {
        this.dbService = dbService;
        if (message.hasLocation() && (mainUpdateInfo.getUserDataCache().getUsersBotStates().get(userID) == NavigationBotStates.USERADDRESS_SETTINGS)) {
            String address = locationHandle(message.getLocation().getLongitude(), message.getLocation().getLatitude());
            mainUpdateInfo.getUserDataCache().getUserProfileData(userID).setAddress(address);
            dbService.getUserProfileRepository().updateUserProfileSetAddressForChatId(address, message.getChatId());
            sendMessage.setText("Данные успешно сохранены!");
            sendMessage.setReplyMarkup(new Settings().getSettingsKeyboard());
            mainUpdateInfo.getUserDataCache().setUsersCurrentBotState(userID, NavigationBotStates.MAIN_SETTINGS);
        }
    }

    @SneakyThrows
    public String locationHandle(double longitudeLocation, double latitudeLocation) {
        longitude = String.valueOf(longitudeLocation);
        latitude = String.valueOf(latitudeLocation);
        httpPost = new HttpPost(yandexLink + longitude + "," + latitude);
        responseHandler = new LocationResponseHandler();
        response = httpClient.execute(httpPost);
        httpGet = new HttpGet(yandexLink + longitude + "," + latitude);
        jsonResponse = httpClient.execute(httpGet, responseHandler);
        return parseJsonCoordResponse(jsonResponse);
    }

    @SneakyThrows
    public Location locationHandle(String address) {
        String uriFormat = address.replaceAll(" ", "+");
        httpPost = new HttpPost(yandexLink + "Екатеринбург," + uriFormat);
        responseHandler = new LocationResponseHandler();
        response = httpClient.execute(httpPost);
        httpGet = new HttpGet(yandexLink + "Екатеринбург," + uriFormat);
        jsonResponse = httpClient.execute(httpGet, responseHandler);
        return parseJsonAddressResponse(jsonResponse);
    }

    @SneakyThrows
    public String parseJsonCoordResponse(String jsonText) {
        jsonArray = JsonParser.parseString(jsonText).getAsJsonObject().getAsJsonObject("response").getAsJsonObject("GeoObjectCollection").getAsJsonArray("featureMember");
        if (jsonArray.size() != 0) {
            jsonObject = jsonArray.get(0).getAsJsonObject().getAsJsonObject("GeoObject").getAsJsonObject("metaDataProperty").getAsJsonObject("GeocoderMetaData").getAsJsonObject("AddressDetails").getAsJsonObject("Country");
            return jsonObject.getAsJsonPrimitive("AddressLine").getAsString();
        } else return null;
    }

    @SneakyThrows
    public Location parseJsonAddressResponse(String jsonText) {
        JsonArray geoObjectsArray = JsonParser.parseString(jsonText).getAsJsonObject().getAsJsonObject("response").getAsJsonObject("GeoObjectCollection").getAsJsonArray("featureMember");
        Location location = null;
        for (int i = 0; i < geoObjectsArray.size(); i++) {
            if (geoObjectsArray.get(i).getAsJsonObject().getAsJsonObject("GeoObject").getAsJsonObject("metaDataProperty").getAsJsonObject("GeocoderMetaData").getAsJsonPrimitive("kind").getAsString().equals("house")) {
                location = new Location();
                address = geoObjectsArray.get(i).getAsJsonObject().getAsJsonObject("GeoObject").getAsJsonObject("metaDataProperty").getAsJsonObject("GeocoderMetaData").getAsJsonObject("AddressDetails").getAsJsonObject("Country").getAsJsonPrimitive("AddressLine").getAsString();
                String[] coordinates = geoObjectsArray.get(i).getAsJsonObject().getAsJsonObject("GeoObject").getAsJsonObject("Point").getAsJsonPrimitive("pos").getAsString().split(" ");
                location.setLongitude(Double.valueOf(coordinates[0]));
                location.setLatitude(Double.valueOf(coordinates[1]));
                break;
            }
        }
        return location;
    }
}
