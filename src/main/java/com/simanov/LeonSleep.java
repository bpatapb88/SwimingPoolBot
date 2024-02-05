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
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.simanov.Main.logger;

public class LeonSleep extends TelegramLongPollingBot {

    public static final LocalTime DAY_END = LocalTime.of(22,0);
    private static final int INTERVAL_BTW_SLEEP = 180;

    private static final String PAPA_ID = "173780137";
    private static final String MAMA_ID = "103165518";
    private final DatabaseHandler databaseHandler = new DatabaseHandler();
    private List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();

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
        return !id.equals(PAPA_ID) && !id.equals(MAMA_ID);
    }

    private String handleRequest(Update update, Message receivedMessage) {
        var sleepCommand = SleepCommand.toSleepCommand(receivedMessage.getText().toLowerCase());
        if (sleepCommand == null) {
            return "Не понял.. дак он уснул или встал?";
        }
        int result = databaseHandler.save(sleepCommand);
        if (result <= 0) {
            return "не удалось записать";
        }
        notifyPartner(update.getMessage().getChatId().toString(), sleepCommand);
        return sleepCommand.getFormatted();
    }

    private void notifyPartner(String chatId, SleepCommand sleepCommand) {
        var newChatId = chatId.equals(MAMA_ID) ? PAPA_ID : MAMA_ID;
        var who = chatId.equals(MAMA_ID) ? "Мама записала что " : "Папа записала что ";
        var message = who + "Леон " + sleepCommand.getFormatted();
        send(newChatId, message);
        scheduleFeatureNotification(sleepCommand);
    }

    private void scheduleFeatureNotification(SleepCommand sleepCommand) {
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            send(MAMA_ID, "Леону пора спать");
            send(PAPA_ID, "Леону пора спать");
        };
        if(sleepCommand.command().equals(State.UP)
                && sleepCommand.time().isBefore(DAY_END.minus(Duration.of(INTERVAL_BTW_SLEEP, ChronoUnit.MINUTES)))) {
            var minutes = Duration.between(sleepCommand.time(), LocalTime.now()).toMinutes();
            var feature = scheduledExecutor.schedule(
                    task,
                    INTERVAL_BTW_SLEEP - minutes,
                    TimeUnit.MINUTES
            );
            scheduledTasks.add(feature);
        } else if(sleepCommand.command().equals(State.DOWN) && !scheduledTasks.isEmpty())  {
            logger.log(Level.INFO, "Леон уснул вовремя, отменить запланированные нотификации");
            for(ScheduledFuture<?> feature : scheduledTasks) {
                feature.cancel(true);
            }
        }
        scheduledExecutor.shutdown();
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
            case "/day_sleep":
                return new RequestTodayDay(databaseHandler);
            case "/yesterday_sleep":
                return new RequestYesterdayDay(databaseHandler);
            case "/undo":
                return new RequestCancel(databaseHandler);
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

    public static String getFormattedCommands(SleepCommand command) {
        StringBuilder result = new StringBuilder();
        result.append(command.command().label)
            .append(" в ")
            .append(command.time())
            .append("\n");
        if (command.command().equals(State.UP) && command.time().isBefore(DAY_END)) {
            result.append("Следующий сон в ")
                .append(command.time().plusMinutes(INTERVAL_BTW_SLEEP))
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
}
