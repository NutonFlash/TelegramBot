package ru.TelegramBot.DataBase.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.TelegramBot.DataBase.model.UserProfile;

public interface UserProfileRepository extends CrudRepository<UserProfile, Integer> {

    @Transactional
    @Modifying
    @Query("update UserProfile u set u.address = ?1 where u.chatID = ?2")
    int updateUserProfileSetAddressForChatId(String address, Long chatID);

    @Transactional
    @Modifying
    @Query("update UserProfile u set u.name = ?1 where u.chatID = ?2")
    int updateUserProfileSetNameForChatID(String name, Long chatID);

    @Transactional
    @Modifying
    @Query("update UserProfile u set u.phoneNumber = ?1 where u.chatID = ?2")
    int updateUserProfileSetPhoneNumberForChatID(String phoneNumber, Long chatID);
}
