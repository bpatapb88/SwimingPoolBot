package com.simanov;

import com.google.common.io.Resources;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static com.simanov.Main.logger;

public class Squash extends TelegramLongPollingBot {

    private static final String WELCOME_QUESTION = "Играем в сквош на следующей неделе?";


    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage() != null && update.getMessage().isCommand() && update.getMessage().getText().equals("/start")){
            sendPolls(update);
        } else {
            logger.log(Level.WARNING, "message= {0}", update.getMessage());
        }
    }

    private void sendPolls(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        runSendPoll(Arrays.asList(
                "пн 18:00",
                "пн 18:30",
                "пн 19:00",
                "пн 19:30",
                "вт 18:00",
                "вт 18:30",
                "вт 19:00",
                "вт 19:30",
                "Не приду",
                "Посмотреть"),chatId);
        runSendPoll(Arrays.asList(
                "ср 18:00",
                "ср 18:30",
                "ср 19:00",
                "ср 19:30",
                "чт 18:00",
                "чт 18:30",
                "чт 19:00",
                "чт 19:30",
                "Не приду",
                "Посмотреть"),chatId);

    }

    @Override
    public String getBotUsername() {
        return "squash_brno_bot";
    }

    private void runSendPoll(List<String> options, String chatId){
        SendPoll poll = new SendPoll();
        poll.setChatId(chatId);
        poll.setAllowMultipleAnswers(true);
        poll.setIsAnonymous(false);
        poll.setQuestion(WELCOME_QUESTION);
        poll.setOptions(options);
        try {
            execute(poll);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        String apiKey = "";
        try {
            URL url = Resources.getResource("SquashConf.txt");
            apiKey = Resources.toString(url, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.log(Level.INFO, "Not possible to send Bot Token {0}.", e.toString());
            e.printStackTrace();
        }
        return apiKey;
    }
}
