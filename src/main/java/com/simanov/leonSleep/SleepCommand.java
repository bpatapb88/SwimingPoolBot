package com.simanov.leonSleep;

import java.time.LocalTime;

public class SleepCommand {
    private LocalTime time;
    private State command;

    public SleepCommand(LocalTime time, State command) {
        this.time = time;
        this.command = command;
    }

    public LocalTime time() {
        return time;
    }

    public State command() {
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
