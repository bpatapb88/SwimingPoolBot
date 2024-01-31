package com.simanov.leonSleep;

import java.time.Duration;

public class SleepingTime {
    private final Duration duration;
    private static final int MINUTES_IN_HOUR = 60;

    public SleepingTime(Duration duration) {
        this.duration = duration;
    }

    public long hours() {
        return duration.toHours();
    }

    public long minutes() {
        return hours() > 0 ? (duration.toMinutes() % MINUTES_IN_HOUR) : duration.toMinutes();
    }

    public SleepingTime plus(SleepingTime add) {
        return new SleepingTime(this.duration.plus(add.duration));
    }

    @Override
    public String toString() {
        return hours() + " часов " + minutes() + " минут";
    }
}
