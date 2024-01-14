package com.simanov.leonSleep;

import com.simanov.LeonSleep;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Map;

public class RequestTodayAll implements Request{

    private Map<LocalDate, LinkedList<SleepCommand>> save;

    public RequestTodayAll(Map<LocalDate, LinkedList<SleepCommand>> save) {
        this.save = save;
    }

    @Override
    public String getRespond() {
        var commands = save.get(LocalDate.now());
        String response = "";
        if (!LeonSleep.verifyCommandList(commands)) {
            response = "Поряд команд неверный! Расчет будет ошибочный\n";
        }
        var duration = LeonSleep.sleepTimeAll(commands);
        response += "Леон сегодня спал " + duration.toHours() + " часов " + duration.toMinutes() + " минут\n"
                + commands;
        return response;
    }
}
