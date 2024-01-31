package com.simanov.leonSleep;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class RequestTodayDay extends Request{

    private static final LocalTime dayEnd = LocalTime.of(22,0);

    public RequestTodayDay(DatabaseHandler databaseHandler) {
        super(databaseHandler);
    }

    @Override
    public LocalDate getAimDay() {
        return LocalDate.now();
    }

    @Override
    void formatting(StringBuilder response, SleepingTime night, LinkedHashMap<LocalTime, SleepingTime> sleep, SleepingTime sumTime) {
        response.append("Ночью спал ").append(night).append("\n");
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

        response.append("Сумма снов за день ")
                .append(sumTime.hours()).append(" часов ")
                .append(sumTime.minutes()).append(" минут");
    }

    @Override
    void correction(LinkedList<SleepCommand> commands, LinkedHashMap<LocalTime, SleepingTime> sleep) {
        if (!commands.isEmpty() && commands.getLast().command().equals(State.DOWN)
                && commands.getLast().time().isBefore(dayEnd)
                && LocalTime.now().isBefore(dayEnd)) {
            sleep.put(commands.getLast().time(), null);
        }
    }


}
