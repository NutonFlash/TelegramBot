package ru.TelegramBot.DataBase.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(name = "product_amount")
    Integer productAmount;
    @Column(name = "price")
    Integer price;
    @Column(name = "caption")
    String caption;
    @Column(name = "photo")
    String photo;
    @Column(name = "product_name")
    String productName;
    @Column(name = "configuration_name")
    String configurationName;

    public Product(Integer productAmount, Integer price, String caption, String photo, String productName, String configurationName) {
        this.productAmount = productAmount;
        this.price = price;
        this.caption = caption;
        this.photo = photo;
        this.productName = productName;
        this.configurationName = configurationName;
    }

    public Product() {
    }

    public void increaseProductAmount() {
        this.productAmount++;
    }

    public void decreaseProductAmount() {
        if (productAmount > 1) this.productAmount--;
    }
}
