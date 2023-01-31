package ru.TelegramBot.DataBase.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(name = "chat_ID")
    Long chatID;
    @Column(name = "address")
    String address;
    @Column(name = "name")
    String name;
    @Column(name = "phone_number")
    String phoneNumber;
    @Column(name = "telegram")
    String telegram;
    @Transient
    ArrayList<Product> currentUserBasket = new ArrayList<>();
    @Transient
    int messageID;
    @Transient
    HashMap<String, String> currentBasketButtonState = new HashMap<>();
    @Transient
    ArrayList<ConfirmedOrder> confirmedOrderList = new ArrayList<>();

    public UserProfile(Long chatID, String address, String name, String phoneNumber, String telegram) {
        this.chatID = chatID;
        this.address = address;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.telegram = telegram;
    }

    public UserProfile() {
    }


}
