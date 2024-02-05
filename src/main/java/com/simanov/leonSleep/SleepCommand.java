package com.simanov.leonSleep;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.simanov.Main.logger;

public class SleepCommand {
    private static final String TIME_PATTERN = "\\b([01]?\\d|2[0-3]):[0-5]\\d\\b";
    public static final LocalTime DAY_END = LocalTime.of(22,0);
    private static final int INTERVAL_BTW_SLEEP = 180;

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

    public static SleepCommand toSleepCommand(String text) {
        var localTime = grepTime(text);
        logger.log(Level.INFO, "LocalTime is {}", localTime.toString());
        if (text.contains(State.UP.label)){
            return new SleepCommand(localTime, State.UP);
        } else if(text.contains(State.DOWN.label)) {
            return new SleepCommand(localTime, State.DOWN);
        } else {
            return null;
        }
    }

    public String getFormatted() {
        StringBuilder result = new StringBuilder();
        result.append(this.command().label)
                .append(" в ")
                .append(this.time())
                .append("\n");
        if (this.command().equals(State.UP) && this.time().isBefore(DAY_END)) {
            result.append("Следующий сон в ")
                    .append(this.time().plusMinutes(INTERVAL_BTW_SLEEP))
                    .append("\n");
        }
        return result.toString();
    }

    private static LocalTime grepTime(String text) {
        String time = textToTime(text);
        if (!time.equals("")) {
            var timeArray = time.split(":");
            return LocalTime.of(
                    Integer.parseInt(timeArray[0]),
                    Integer.parseInt(timeArray[1])
            );
        } else {
            return LocalTime.of(
                    LocalTime.now(ZoneId.of("UTC+01:00")).getHour(),
                    LocalTime.now(ZoneId.of("UTC+01:00")).getMinute()
            );
        }
    }

    private static String textToTime(String text) {
        Pattern pattern = Pattern.compile(TIME_PATTERN);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

    @Override
    public String toString() {
        return "SleepCommand{" +
                "time=" + time +
                ", command='" + command + '\'' +
                '}';
    }
}
