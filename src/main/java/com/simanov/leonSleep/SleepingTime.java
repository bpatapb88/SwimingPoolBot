package com.simanov.leonSleep;

import java.time.Duration;

public class SleepingTime {
    private final Duration duration;

    public SleepingTime(Duration duration) {
        this.duration = duration;
    }

    public long hours() {
        return duration.toHours();
    }

    public long minutes() {
        return hours() > 0 ? (duration.toMinutes() % hours()) : duration.toMinutes();
    }
}
