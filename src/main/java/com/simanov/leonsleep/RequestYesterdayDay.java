package com.simanov.leonsleep;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;


public class RequestYesterdayDay extends Request{

    public RequestYesterdayDay(DatabaseHandler databaseHandler) {
        super(databaseHandler);
    }

    @Override
    public LocalDate getAimDay() {
        return LocalDate.now().minusDays(1);
    }

    @Override
    void correction(LinkedList<SleepCommand> commands, LinkedHashMap<LocalTime, SleepingTime> sleep) {
        // empty
    }

    @Override
    void formatting(StringBuilder response, SleepingTime night, LinkedHashMap<LocalTime, SleepingTime> sleep, SleepingTime sumTime) {
        response.append("Спал в ночь с позавчера на вчера ").append(night).append("\n");
        response.append("--------------\n");
        response.append(" Уснул | Спал\n");
        response.append("--------------\n");
        for(Map.Entry<LocalTime, SleepingTime> entry : sleep.entrySet()) {
            response.append(" ").append(entry.getKey()).append(" | ");
            if (entry.getValue() == null) {
                response.append("  ?  ");
            } else {
                sumTime = sumTime.plus(entry.getValue());
                response.append(entry.getValue());
            }
            response.append("\n");
        }

        response.append("Сумма снов за вчерашний день ")
                .append(sumTime.hours()).append(" часов ")
                .append(sumTime.minutes()).append(" минут");
    }
}
