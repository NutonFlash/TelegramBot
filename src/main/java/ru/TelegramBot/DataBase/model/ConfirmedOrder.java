package ru.TelegramBot.DataBase.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "confirmed_order")
public class ConfirmedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(name = "user_ID")
    Long userID;
    @Column(name = "order_time")
    String orderTime;
    @Column(name = "order_details")
    String orderDetails;
    @Column(name = "order_products")
    String orderProducts;

    public ConfirmedOrder(Long userID, String orderTime, String orderDetails, String orderProducts) {
        this.userID = userID;
        this.orderTime = orderTime;
        this.orderDetails = orderDetails;
        this.orderProducts = orderProducts;
    }

    public ConfirmedOrder() {
    }
}
