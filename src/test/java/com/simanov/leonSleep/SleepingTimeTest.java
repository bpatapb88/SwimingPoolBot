package com.simanov.leonSleep;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class SleepingTimeTest {

    @Test
    void minutes() {
        Duration duration1 = Duration.between(LocalTime.of(9,30), LocalTime.of(11,27));
        Duration duration2 = Duration.between(LocalTime.of(14,20), LocalTime.of(15,47));
        var result = duration1.plus(duration2);
        var sleepingTime = new SleepingTime(result);

        System.out.println(sleepingTime);

    }
}