package ru.TelegramBot.DataBase;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.TelegramBot.DataBase.repository.ConfirmedOrderRepository;
import ru.TelegramBot.DataBase.repository.ProductRepository;
import ru.TelegramBot.DataBase.repository.UserProfileRepository;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class DBService {

    @Autowired
    ConfirmedOrderRepository confirmedOrderRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserProfileRepository userProfileRepository;

    public DBService() {
    }
}
