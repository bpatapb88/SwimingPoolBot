package com.simanov.leonSleep;

import com.simanov.LeonSleep;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;


public class RequestYesterdayDay implements Request{

    private DatabaseHandler databaseHandler;

    public RequestYesterdayDay(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;

    }

    @Override
    public String getRespond() {
        var yesterday = LocalDate.now().minusDays(1);
        var commands = databaseHandler.getBy(yesterday);
        var commandsDayBefore = databaseHandler.getBy(yesterday.minusDays(1));
        SleepingTime night = RequestUtility.getNight(commands, commandsDayBefore, yesterday);
        StringBuilder response = new StringBuilder();
        if (!LeonSleep.verifyCommandList(commands)) {
            response.append("Поряд команд неверный! Расчет будет ошибочный\n");
        }

        LinkedHashMap<LocalTime, SleepingTime> sleep = RequestUtility.getSleepMap(commands);


        SleepingTime sleepingTime = new SleepingTime(Duration.ZERO);
        response.append("Спал в ночь с позавчера на вчера " + night + "\n");
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

        response.append("Сумма снов за вчерашний день ")
                .append(sleepingTime.hours()).append(" часов ")
                .append(sleepingTime.minutes()).append(" минут");
        return response.toString();
    }
}
