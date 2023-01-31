package ru.TelegramBot.Cashe;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.DataBase.model.UserProfile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDataCache implements DataCache, Serializable {

    final Map<Long, NavigationBotStates> usersBotStates = new HashMap<>();
    final Map<Long, UserProfile> usersProfileData = new HashMap<>();

    @Override
    public void setUsersCurrentBotState(long userId, NavigationBotStates botState) {
        usersBotStates.put(userId, botState);
    }

    @Override
    public NavigationBotStates getUsersCurrentBotState(long userId) {
        return usersBotStates.get(userId);
    }

    @Override
    public UserProfile getUserProfileData(long userId) {
        return usersProfileData.get(userId);
    }

    @Override
    public void saveUserProfileData(long userId, UserProfile userProfileData) {
        usersProfileData.put(userId, userProfileData);
    }
}
