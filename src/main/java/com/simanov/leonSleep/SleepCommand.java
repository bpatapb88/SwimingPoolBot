package com.simanov.leonSleep;

import java.time.LocalTime;

public record SleepCommand(
        LocalTime time,
        String command
) {

    @Override
    public String toString() {
        return "SleepCommand{" +
                "time=" + time +
                ", command='" + command + '\'' +
                '}';
    }
}
