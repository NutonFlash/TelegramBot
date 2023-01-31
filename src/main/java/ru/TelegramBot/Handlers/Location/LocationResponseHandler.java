package ru.TelegramBot.Handlers.Location;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class LocationResponseHandler implements ResponseHandler {
    @Override
    public String handleResponse(HttpResponse httpResponse) throws IOException {
        return EntityUtils.toString(httpResponse.getEntity());
    }
}
