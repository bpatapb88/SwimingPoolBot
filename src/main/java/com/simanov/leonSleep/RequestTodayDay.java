package com.simanov.leonSleep;

import com.simanov.LeonSleep;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;

import static com.simanov.Main.logger;

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
        var commandsDayBefore = databaseHandler.getBy(LocalDate.now().minusDays(1));
        SleepingTime night = RequestUtility.getNight(commands, commandsDayBefore, LocalDate.now());
        StringBuilder response = new StringBuilder();
        if (!LeonSleep.verifyCommandList(commands)) {
            response.append("Поряд команд неверный! Расчет будет ошибочный\n");
        }

        LinkedHashMap<LocalTime, SleepingTime> sleep = RequestUtility.getSleepMap(commands);

        if (!commands.isEmpty() && commands.getLast().command().equals(State.DOWN)
                && commands.getLast().time().isBefore(dayEnd)
                && LocalTime.now().isBefore(dayEnd)) {
            sleep.put(commands.getLast().time(), null);
        }

        SleepingTime sleepingTime = new SleepingTime(Duration.ZERO);
        response.append("Ночью спал " + night + "\n");
        response.append("--------------\n");
        response.append(" Уснул | Спал\n");
        response.append("--------------\n");
        for(Map.Entry<LocalTime, SleepingTime> entry : sleep.entrySet()) {
            response.append(" ").append(entry.getKey()).append(" | ");
            if (entry.getValue() == null) {
                response.append("  ?  ");
            } else {
                sleepingTime = sleepingTime.plus(entry.getValue());
                response.append(entry.getValue());
            }
            response.append("\n");
        }

        response.append("Сумма снов за день ")
                .append(sleepingTime.hours()).append(" часов ")
                .append(sleepingTime.minutes()).append(" минут");
        return response.toString();
    }


}
