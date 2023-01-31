package ru.TelegramBot.DataBase.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.TelegramBot.DataBase.model.ConfirmedOrder;

public interface ConfirmedOrderRepository extends CrudRepository<ConfirmedOrder, Integer> {
}
