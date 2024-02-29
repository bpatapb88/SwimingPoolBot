package com.simanov.leonsleep;

import com.simanov.LeonSleep;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.logging.Level;

import static com.simanov.Main.logger;

public abstract class Request {
    DatabaseHandler databaseHandler;

    protected Request(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    public String getRespond(){
        var aimDay = getAimDay();
        var commands = databaseHandler.getBy(aimDay);
        var commandsDayBefore = databaseHandler.getBy(aimDay.minusDays(1));


        var response = new StringBuilder();
        if (!LeonSleep.verifyCommandList(commands)) {
            response.append("Поряд команд неверный! Расчет будет ошибочный\n");
        }

        var night = getNight(commands, commandsDayBefore, aimDay);
        LinkedHashMap<LocalTime, SleepingTime> sleep = getSleepMap(commands);

        correction(commands, sleep);

        var sumTime = new SleepingTime(Duration.ZERO);

        formatting(response, night, sleep, sumTime);

        return response.toString();
    }

    abstract LocalDate getAimDay();

    abstract void correction(
            LinkedList<SleepCommand> commands,
            LinkedHashMap<LocalTime, SleepingTime> sleep
    );

    abstract void formatting(
            StringBuilder response,
            SleepingTime night,
            LinkedHashMap<LocalTime,
            SleepingTime> sleep,
            SleepingTime sumTime
    );

    private static LinkedHashMap<LocalTime, SleepingTime> getSleepMap(LinkedList<SleepCommand> commands) {
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

    private static SleepingTime getNight(
            LinkedList<SleepCommand> commands,
            LinkedList<SleepCommand> commandsDayBefore,
            LocalDate day
    ) {
        var todayUp = commands.stream().filter(x -> x.command().equals(State.UP)).findFirst();
        var dayBefore = day.minusDays(1);
        Collections.reverse(commandsDayBefore);
        var yesterdayDown = commandsDayBefore.stream().filter(x -> x.command().equals(State.DOWN)).findFirst();
        if(todayUp.isEmpty() || yesterdayDown.isEmpty()){
            var warning = String.format("Some list of commands is empty. today.empty %b, yesterdayDown.isEmpty %b",
                    todayUp.isEmpty(),
                    yesterdayDown.isEmpty()
            );
            logger.log(Level.WARNING, warning);
            return new SleepingTime(Duration.ZERO);
        }
        var duration = Duration.between(
                LocalDateTime.of(dayBefore, yesterdayDown.get().time()),
                LocalDateTime.of(day, todayUp.get().time()));
        return new SleepingTime(duration);
    }
}
