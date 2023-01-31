package ru.TelegramBot.DataBase.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.TelegramBot.DataBase.model.Product;

public interface ProductRepository extends CrudRepository<Product, Integer> {

    @Query("select p from Product p where p.configurationName = ?1")
    Product findProductByConfigurationName(String configurationName);

    @Query("select p from Product p where p.productName = ?1")
    Product findProductByProductName(String productName);

    @Transactional
    @Modifying
    @Query("update Product p set p.productAmount = ?1 where p.productName = ?2")
    int updateProductSetProductAmountForProductName(Integer productAmount, String productName);

    @Transactional
    @Modifying
    @Query("update Product p set p.productAmount = ?1 where p.configurationName = ?2")
    int updateProductSetProductAmountForConfigurationName(Integer productAmount, String configurationName);
}
