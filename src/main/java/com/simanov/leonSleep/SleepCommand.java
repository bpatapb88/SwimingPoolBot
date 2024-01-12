package com.simanov.leonSleep;

import java.time.LocalTime;

public class SleepCommand {
    private LocalTime time;
    private String command;

    public SleepCommand(LocalTime time, String command) {
        this.time = time;
        this.command = command;
    }

    public LocalTime time() {
        return time;
    }

    public String command() {
        return command;
    }

    @Override
    public String toString() {
        return "SleepCommand{" +
                "time=" + time +
                ", command='" + command + '\'' +
                '}';
    }
}
