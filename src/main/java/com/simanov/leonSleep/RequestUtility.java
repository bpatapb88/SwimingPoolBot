package com.simanov.leonSleep;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.logging.Level;

import static com.simanov.Main.logger;

public class RequestUtility {

    public static LinkedHashMap<LocalTime, SleepingTime> getSleepMap(LinkedList<SleepCommand> commands) {
        LinkedHashMap<LocalTime, SleepingTime> sleep = new LinkedHashMap<>();
        SleepCommand previous = null;
        for (SleepCommand command : commands) {
            if (previous == null && command.command().equals(State.UP)) {
                previous = command;
                continue;
            }
            if (previous != null) {
                if (command.command().equals(State.UP)) {
                    var duration = Duration.between(previous.time(), command.time());
                    sleep.put(previous.time(), new SleepingTime(duration));
                }
                previous = command;
            }
        }
        return sleep;
    }

    public static SleepingTime getNight(
            LinkedList<SleepCommand> commands,
            LinkedList<SleepCommand> commandsDayBefore,
            LocalDate day
    ) {
        var todayUp = commands.stream().filter(x -> x.command().equals(State.UP)).findFirst();
        var dayBefore = day.minusDays(1);
        Collections.reverse(commandsDayBefore);
        var yesterdayDown = commandsDayBefore.stream().filter(x -> x.command().equals(State.DOWN)).findFirst();
        if(todayUp.isEmpty() || yesterdayDown.isEmpty()){
            logger.log(Level.WARNING, "Some list of commands is empty. today.empty " + todayUp.isEmpty()
                    + ", yesterdayDown.isEmpty " + yesterdayDown.isEmpty());
            return new SleepingTime(Duration.ZERO);
        }
        var duration = Duration.between(
                LocalDateTime.of(dayBefore, yesterdayDown.get().time()),
                LocalDateTime.of(day, todayUp.get().time()));
        return new SleepingTime(duration);
    }
}
