package com.simanov.leonSleep;

import com.simanov.LeonSleep;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Map;

public class RequestTodayAll implements Request{

    private DatabaseHandler databaseHandler;


    public RequestTodayAll(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    @Override
    public String getRespond() {
        var commands = databaseHandler.getBy(LocalDate.now());
        String response = "";
        if (!LeonSleep.verifyCommandList(commands)) {
            response = "Поряд команд неверный! Расчет будет ошибочный\n";
        }
        var duration = LeonSleep.sleepTimeAll(commands);
        long hours = duration.toHours();
        long minutes = hours > 0 ? (duration.toMinutes() % hours) : duration.toMinutes();
        response += "Леон сегодня спал " + hours + " часов " + minutes + " минут\n"
                + LeonSleep.getFormattedCommands(commands);
        return response;
    }
}
