package ru.TelegramBot.AppConfig;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.TelegramBot.Handlers.Location.LocationResponseHandler;

import java.io.IOException;

@Service
@Slf4j
@Getter
@Setter
public class Pinger {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet;
    ResponseHandler<String> responseHandler;
    @Value("${telegrambot.webHookPath}")
    String url;

    @Scheduled(fixedRateString = "${pinger.period}")
    public void pingMe() {
        try {
            responseHandler = new LocationResponseHandler();
            httpGet = new HttpGet("https://telegram-bot-pid.fly.dev");
            log.info("Response message from GET request: {}", httpClient.execute(httpGet, responseHandler) + "OK, response code 200");
        } catch (IOException e) {
            log.error("Ping FAILED");
            e.printStackTrace();
        }
    }
}
