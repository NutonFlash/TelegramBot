package ru.TelegramBot.Cashe;

import ru.TelegramBot.BotLogic.NavigationBotStates;
import ru.TelegramBot.DataBase.model.UserProfile;

public interface DataCache {

    void setUsersCurrentBotState(long userId, NavigationBotStates botState);

    NavigationBotStates getUsersCurrentBotState(long userId);

    UserProfile getUserProfileData(long userId);

    void saveUserProfileData(long userId, UserProfile userProfileData);
}
