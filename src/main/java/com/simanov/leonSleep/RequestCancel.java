package com.simanov.leonSleep;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class RequestCancel extends Request{

    public RequestCancel(DatabaseHandler databaseHandler) {
        super(databaseHandler);
    }

    @Override
    public String getRespond() {
        var result = databaseHandler.cancelLast();
        return result ? "Последняя запись удалена" : "Не удалось удалить";
    }

    @Override
    public LocalDate getAimDay() {
        //empty method
        return null;
    }

    @Override
    void correction(LinkedList<SleepCommand> commands, LinkedHashMap<LocalTime, SleepingTime> sleep) {
        //empty method
    }

    @Override
    void formatting(StringBuilder response, SleepingTime night, LinkedHashMap<LocalTime, SleepingTime> sleep, SleepingTime sumTime) {
        //empty method
    }
}
