package com.simanov.leonSleep;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

public class DatabaseHandler {
    Connection dbConnection;
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "5432";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "postgres";
    private static final String DB_NAME = "raspdb";
    private static final String TABLE = "leon_sleep";

    public Connection getDbConnection(){
        String connectionString = "jdbc:postgresql://" + DB_HOST + ":"
                + DB_PORT + "/" + DB_NAME;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(dbConnection == null){
            try {
                dbConnection = DriverManager.getConnection(connectionString, DB_USER, DB_PASS);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return dbConnection;
    }

    private ResultSet executeSelect(String select) {
        Connection connection = getDbConnection();
        try {
            PreparedStatement prSt = connection.prepareStatement(select);
            return prSt.executeQuery();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public void save(SleepCommand sleepCommand) {
        String query = String.format("INSERT INTO %s (command, time, date) VALUES ('%s', '%s', '%s');",
                TABLE,
                sleepCommand.command(),
                sleepCommand.time(),
                LocalDate.now());
        executeSelect(query);
    }

    public LinkedList<SleepCommand> getBy(LocalDate date) {
        String query = String.format("SELECT * FROM %s WHERE date='%s';",
                TABLE,
                date.toString());
        ResultSet resultSet = executeSelect(query);
        LinkedList<SleepCommand> result = new LinkedList<>();
        try {
            while (resultSet != null && resultSet.next()) {
                String command = resultSet.getString("command");
                String time = resultSet.getTime("time").toString();
                int hours = Integer.parseInt(time.split(":")[0]);
                int min = Integer.parseInt(time.split(":")[1]);
                result.add(new SleepCommand(LocalTime.of(hours,min),State.valueOf(command)));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return result;
    }
}