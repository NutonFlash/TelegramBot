package ru.TelegramBot.Cashe;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@Component
public class MainUpdateInfo {
    long userID;
    UserDataCache userDataCache;
    SendMessage sendMessage;
    Message message;

    public MainUpdateInfo() {
    }
}
