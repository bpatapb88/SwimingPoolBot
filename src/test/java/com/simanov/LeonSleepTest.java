package com.simanov;

import com.simanov.leonSleep.SleepCommand;
import com.simanov.leonSleep.State;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class LeonSleepTest {

    @Test
    void getSleepTime() {
        LinkedList<SleepCommand> commands = new LinkedList<>();
        commands.add(new SleepCommand(LocalTime.of(12,0), State.UP));
        commands.add(new SleepCommand(LocalTime.of(13,0), State.DOWN));
        commands.add(new SleepCommand(LocalTime.of(14,0), State.UP));
        var duration = LeonSleep.sleepTimeAll(commands);
        assertEquals(13, duration.toHours());
    }
}