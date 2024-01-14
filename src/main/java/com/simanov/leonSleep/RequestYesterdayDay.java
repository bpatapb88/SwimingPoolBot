package com.simanov.leonSleep;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Map;


public class RequestYesterdayDay implements Request{

    private Map<LocalDate, LinkedList<SleepCommand>> save;

    public RequestYesterdayDay(Map<LocalDate, LinkedList<SleepCommand>> save) {
        this.save = save;
    }

    @Override
    public String getRespond() {
        return null;
    }
}
