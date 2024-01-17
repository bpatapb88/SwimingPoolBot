package com.simanov.leonSleep;

import com.simanov.LeonSleep;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Map;

public class RequestTodayDay implements Request{

    private DatabaseHandler databaseHandler;
    private static final LocalTime dayStart = LocalTime.of(6,0);
    private static final LocalTime dayEnd = LocalTime.of(22,0);

    public RequestTodayDay(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    @Override
    public String getRespond() {
        var commands = databaseHandler.getBy(LocalDate.now());
        String response = "";
        if (!LeonSleep.verifyCommandList(commands)) {
            response = "Поряд команд неверный! Расчет будет ошибочный\n";
        }
        SleepCommand previous = null;
        Duration duration = Duration.ZERO;
        for (SleepCommand command : commands) {
            if(command.time().isBefore(dayStart)) {
                continue;
            }
            if (previous == null && command.command().equals(State.UP)) {
                previous = command;
                continue;
            }
            if (previous != null) {
                if (command.command().equals(State.UP)) {
                    duration = duration.plus(Duration.between(previous.time(), command.time()));
                }
                previous = command;
            }
        }

        if (commands.getLast().command().equals(State.DOWN)
                && commands.getLast().time().isBefore(dayEnd)
                && LocalTime.now().isBefore(dayEnd)) {
            duration = duration.plus(Duration.between(commands.getLast().time(), LocalTime.now()));
        }
        response += "Леон сегодня спал " + duration.toHours() + " часов " + duration.toMinutes() + " минут\n"
                + LeonSleep.getFormattedCommands(commands);
        return response;
    }
}
