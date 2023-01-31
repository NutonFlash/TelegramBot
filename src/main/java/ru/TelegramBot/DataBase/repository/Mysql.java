package ru.TelegramBot.DataBase.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mysql {

    public Mysql() {
        connect();
    }

    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://eu-central.connect.psdb.cloud/telegrambotpid?sslMode=VERIFY_IDENTITY",
                    "zx5rhc0jq6suux3uopkg",
                    "pscale_pw_I8zElVK0m1IdYNfe8mWcZygj7xrpJbT3FuN0rGGhkh3");
        } catch (SQLException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
    }
}
