package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connetion {
    private static final String URL = "jdbc:mysql://localhost:3306/mural_recados";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";


    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
