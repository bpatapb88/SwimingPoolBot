package com.simanov.leonsleep;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.logging.Level;

import static com.simanov.Main.logger;

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

    public boolean cancelLast() {
        var query = String.format("DELETE FROM %1$s WHERE id in (SELECT id FROM %1$s ORDER BY id desc LIMIT 1);",
                TABLE);
        return executeQuery(query) > 0;
    }

    private int executeQuery(String query) {
        Connection connection = getDbConnection();
        int result = 0;
        try (PreparedStatement prSt = connection.prepareStatement(query)) {
            result = prSt.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return result;
    }

    public int save(SleepCommand sleepCommand, LocalDate date) {
        logger.log(Level.INFO, "Save: sleepCommand {}", sleepCommand);
        String query = String.format("INSERT INTO %s (command, time, date) VALUES ('%s', '%s', '%s');",
                TABLE,
                sleepCommand.command(),
                sleepCommand.time(),
                date);
        return executeQuery(query);
    }

    public LinkedList<SleepCommand> getBy(LocalDate date) {
        String query = String.format("SELECT * FROM %s WHERE date='%s';",
                TABLE,
                date.toString());

        Connection connection = getDbConnection();
        LinkedList<SleepCommand> result = new LinkedList<>();
        try (PreparedStatement prSt = connection.prepareStatement(query)) {
            ResultSet resultSet = prSt.executeQuery();
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
