package com.simanov;

import com.google.common.io.Resources;
import com.simanov.leonSleep.SleepCommand;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.simanov.Main.logger;

public class LeonSleep extends TelegramLongPollingBot {

    private static final String UP = "встал";
    private static final String DOWN = "уснул";
    private Map<LocalDate, LinkedList<SleepCommand>> save = new HashMap<>();
    //TODO notify both parents about request
    private static final String papaId = "";
    private static final String mamaId = "";

    @Override
    public String getBotUsername() {
        return "leon_sleep_bot";
    }

    @Override
    public String getBotToken() {
        String apiKey = "";
        try {
            URL url = Resources.getResource("LeonConf.txt");
            apiKey = Resources.toString(url, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.log(Level.INFO, "Not possible to send Bot Token {0}.", e.toString());
            e.printStackTrace();
        }
        return apiKey;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage() == null){
            return;
        }
        Message recivedMessage = update.getMessage();

        if (recivedMessage.isCommand()) {
            handleCommands(update, recivedMessage);
            return;
        }
        handleRequest(update, recivedMessage);
    }

    private void handleRequest(Update update, Message recivedMessage) {
        var text = recivedMessage.getText().toLowerCase();
        var sleepCommand = toSleepCommand(text);
        var responseMessage = "ok";
        if (sleepCommand != null) {
            if(save.containsKey(LocalDate.now())) {
                save.get(LocalDate.now()).add(sleepCommand);
            } else {
                save.put(LocalDate.now(), new LinkedList<>(List.of(sleepCommand)));
            }
            var logMessage = String.format("Registered sleepCommand: %s. ChatId %s",
                    sleepCommand,
                    update.getMessage().getChatId()
            );
            logger.log(Level.INFO, logMessage);
        } else {
            responseMessage = "Не понял.. дак он уснул или встал?";
        }
        send(update.getMessage().getChatId().toString(), responseMessage);
    }

    private void send(String chatId, String text) {
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setText(text);
        try {
            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCommands(Update update, Message recivedMessage) {
        String firstPart = "Леон ";
        LinkedList<SleepCommand> commands;
        switch (recivedMessage.getText()) {
            case "/today" :
                commands = save.get(LocalDate.now());
                firstPart += "спал сегодня ";
                break;
            case "/vcera":
                commands = save.get(LocalDate.now().minusDays(1));
                firstPart += "спал вчера ";
                break;
            default:
                return;
        }
        boolean orderGood = verifyCommandList(commands);
        var duration = getSleepTime(commands);

        firstPart = orderGood ? firstPart : "Проверте порядок команд!\n" + firstPart;
        var text = firstPart
                + duration.toHoursPart() + " часов "
                + duration.toMinutesPart() + " минут \n"
                + getFormattedCommands(commands);
        var chatId = update.getMessage().getChatId().toString();
        var logMessage = String.format("Send message to %s, message %s", chatId, text);
        logger.log(Level.INFO, logMessage);
        send(chatId, text);
    }

    private boolean verifyCommandList(LinkedList<SleepCommand> commands) {
        if (commands.isEmpty() || commands.size() == 1) {
            return true;
        }
        commands.sort(Comparator.comparing(SleepCommand::time));
        for (int i = 1 ; i < commands.size(); i ++) {
            if (commands.get(i).command().equals(commands.get(i-1).command())) {
                var logMessage = String.format(
                        "Warning! order UP/DOWN is broken. Command 1 %s, Command 2 %s, All %s",
                        commands.get(i-1),
                        commands.get(i),
                        commands
                );
                logger.log(Level.WARNING , logMessage);
                return false;
            }
        }
        return true;
     }

    private String getFormattedCommands(LinkedList<SleepCommand> commands) {
        StringBuilder result = new StringBuilder();
        for (SleepCommand command : commands) {
            result.append(command.command() + " в " + command.time() + "\n");
        }
        return result.toString();
    }

    private Duration getSleepTime(LinkedList<SleepCommand> commands) {
        Duration result = Duration.ZERO;
        if(commands.isEmpty()) {
            return result;
        }
        commands.sort(Comparator.comparing(SleepCommand::time));
        var firstIsUp = commands.get(0).command().equals(UP);
        String state = firstIsUp ? UP : DOWN;
        LocalTime previous = firstIsUp ? LocalTime.MIN : commands.get(0).time();

        for(SleepCommand command : commands) {
            if(command.command().equals(state)) {
                if (state.equals(UP)) {
                    result = result.plus(Duration.between(previous, command.time()));
                }
                previous = command.time();
                state = state.equals(UP) ? DOWN : UP;
            } else {
                //TODO
            }
        }

        if(commands.get(commands.size()-1).command().equals(DOWN)) {
            result = result.plus(Duration.between(commands.get(commands.size()-1).time(), LocalTime.now()));
        }
        return result;
    }

    private SleepCommand toSleepCommand(String text) {
        var localTime = grepTime(text);
        System.out.println("getRequest: time " + localTime);

        if (text.contains(UP)){
            return new SleepCommand(localTime, UP);
        } else if(text.contains(DOWN)) {
            return new SleepCommand(localTime, DOWN);
        } else {
            return null;
        }
    }

    private LocalTime grepTime(String text) {
        LocalTime result;
        Pattern pattern = Pattern.compile("\\b([01]?\\d|2[0-3]):[0-5]\\d\\b");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            var time = matcher.group().split(":");
            result = LocalTime.of(
                    Integer.parseInt(time[0]),
                    Integer.parseInt(time[1])
            );
        } else {
            result = LocalTime.of(
                    LocalTime.now().getHour(),
                    LocalTime.now().getMinute()
            );
        }
        return result;
    }
}
