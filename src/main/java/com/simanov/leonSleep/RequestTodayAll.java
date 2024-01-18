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
        SleepingTime sleepingTime = new SleepingTime(LeonSleep.sleepTimeAll(commands));
        response += "Леон сегодня спал " + sleepingTime.hours() + " часов " + sleepingTime.minutes() + " минут\n"
                + LeonSleep.getFormattedCommands(commands);
        return response;
    }
}
