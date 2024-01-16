package com.simanov;

import com.google.common.io.Resources;
import com.simanov.leonSleep.*;
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

    private Map<LocalDate, LinkedList<SleepCommand>> save = new HashMap<>();
    //TODO notify both parents about request
    private static final String papaId = "173780137";
    private static final String mamaId = "103165518";
    private static final String timePattern = "\\b([01]?\\d|2[0-3]):[0-5]\\d\\b";
    private DatabaseHandler databaseHandler = new DatabaseHandler();

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
        if(update.getMessage() == null || notParents(update)){
            return;
        }
        Message recivedMessage = update.getMessage();
        String response;
        if (recivedMessage.isCommand()) {
            response = handleCommands(update, recivedMessage);
        }else {
            response = handleRequest(update, recivedMessage);
        }
        send(update.getMessage().getChatId().toString(), response);
    }

    private boolean notParents(Update update) {
        var id = update.getMessage().getChatId().toString();
        return !id.equals(papaId) && !id.equals(mamaId);
    }

    private String handleRequest(Update update, Message receivedMessage) {
        var sleepCommand = toSleepCommand(receivedMessage.getText().toLowerCase());
        if (sleepCommand == null) {
            return "Не понял.. дак он уснул или встал?";
        }
        databaseHandler.save(sleepCommand);
//        if(rejectRequest(update, sleepCommand)) {
//            return "Ошибка! последняя запись тоже была \"" + sleepCommand.command().label + "\"";
//        }
        notifyPartner(update.getMessage().getChatId().toString(), sleepCommand);
        return "ok\n" + getFormattedCommands(List.of(sleepCommand));
    }

    private boolean rejectRequest(Update update, SleepCommand sleepCommand) {
        return textToTime(update.getMessage().getText().toLowerCase()).equals("")
                && save.get(LocalDate.now()).getLast().command().equals(sleepCommand.command());
    }

    private void notifyPartner(String chatId, SleepCommand sleepCommand) {
        var newChatId = chatId.equals(mamaId) ? papaId : mamaId;
        var who = chatId.equals(mamaId) ? "Мама записала что " : "Папа записала что ";
        var message = who + "Леон " + sleepCommand.command().label + " в " + sleepCommand.time();
        send(newChatId, message);
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

    private String handleCommands(Update update, Message recivedMessage) {
        Request request = messageToRequest(recivedMessage);
        var response = request == null ? "" : request.getRespond();
        var text = response.equals("") ? "Command not found" : response;
        var chatId = update.getMessage().getChatId().toString();
        var logMessage = String.format("Send message to %s, message %s", chatId, text);
        logger.log(Level.INFO, logMessage);
        return text;
    }

    private Request messageToRequest(Message recivedMessage) {
        switch (recivedMessage.getText()) {
            case "/today" :
                return new RequestTodayAll(databaseHandler);
            case "/vcera":
                return new RequestYesterdayAll(save);
            case "/day_sleep":
                return new RequestTodayDay(databaseHandler);
            case "/yesterday_sleep":
                return new RequestYesterdayDay(save);
            default:
                return null;
        }
    }

    public static boolean verifyCommandList(LinkedList<SleepCommand> commands) {
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

    public static String getFormattedCommands(List<SleepCommand> commands) {
        StringBuilder result = new StringBuilder();
        for (SleepCommand command : commands) {
            result.append(command.command().label)
                    .append(" в ")
                    .append(command.time())
                    .append("\n");
        }
        return result.toString();
    }

    public static Duration sleepTimeAll(LinkedList<SleepCommand> commands) {
        Duration result = Duration.ZERO;
        if(commands.isEmpty()) {
            return Duration.ZERO;
        }
        commands.sort(Comparator.comparing(SleepCommand::time));
        var firstIsUp = commands.getFirst().command().equals(State.UP);
        State state = firstIsUp ? State.UP : State.DOWN;
        LocalTime previous = firstIsUp ? LocalTime.MIN : commands.getFirst().time();

        for(SleepCommand command : commands) {
            if(command.command().equals(state)) {
                if (state.equals(State.UP)) {
                    result = result.plus(Duration.between(previous, command.time()));
                }
                previous = command.time();
                state = state.equals(State.UP) ? State.DOWN : State.UP;
            } else {
                //TODO
            }
        }

        if(commands.getLast().command().equals(State.DOWN)) {
            result = result.plus(Duration.between(commands.get(commands.size()-1).time(), LocalTime.now()));
        }
        return result;
    }

    private SleepCommand toSleepCommand(String text) {
        var localTime = grepTime(text);
        if (text.contains(State.UP.label)){
            return new SleepCommand(localTime, State.UP);
        } else if(text.contains(State.DOWN.label)) {
            return new SleepCommand(localTime, State.DOWN);
        } else {
            return null;
        }
    }

    private LocalTime grepTime(String text) {
        String time = textToTime(text);
        if (!time.equals("")) {
            var timeArray = time.split(":");
            return LocalTime.of(
                    Integer.parseInt(timeArray[0]),
                    Integer.parseInt(timeArray[1])
            );
        } else {
            return LocalTime.of(
                    LocalTime.now().getHour(),
                    LocalTime.now().getMinute()
            );
        }
    }

    private String textToTime(String text) {
        Pattern pattern = Pattern.compile(timePattern);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : "";
    }
}
