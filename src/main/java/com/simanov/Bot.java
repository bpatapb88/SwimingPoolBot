package com.simanov;

import com.google.common.io.Resources;
import com.simanov.pools.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.simanov.Main.logger;

public class Bot extends TelegramLongPollingBot {

    private static final Pool[] ALL_POOLS = new Pool[] {
            new ZaLuzankami("Za Lužánkami"),
            new KraviHora("Kraví Hora"),
            new Kohoutovice("Kohoutovice"),
            new Druzstevni("Družstevní"),
            new Ponavka("Ponávka"),
            new TjTesla("TJ TESLA")};
    private static final String WELCOME_QUESTION = "Выбери бассейны в Брно:";
    private static final String END_MESSAGE = "Запустить снова -  /start ";
    private static final List<String> optionsList = Stream.of(ALL_POOLS).map(Pool::getName).collect(Collectors.toList());

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage() != null && update.getMessage().isCommand() && update.getMessage().getText().equals("/start")){
            sendPoll(update);
        }else if(update.getPollAnswer() != null){
            sendResponse(update.getPollAnswer());
        } else {
            logger.log(Level.WARNING, "message= {0}", update.getMessage());
        }
    }

    private void sendResponse(PollAnswer pollAnswer) {
        String responseString = poolAnswersToRespond(pollAnswer);
        SendMessage response = new SendMessage();
        response.setChatId(pollAnswer.getUser().getId().toString());
        response.setText(responseString);
        try {
            execute(response);
            String message = String.format("Response:\"%s\" %n" +
                    "send to %s",
                    responseString,
                    pollAnswer.getUser().getFirstName());
            logger.log(Level.INFO, message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String poolAnswersToRespond(PollAnswer pollAnswer) {
        logger.log(Level.INFO, "User with id {0} answered poll. Start to collect data... ", pollAnswer.getUser().getId());
        List<Integer> answers = pollAnswer.getOptionIds();
        StringBuilder resultMessage = new StringBuilder();

        for (Integer bazenId: answers){
            String poolName = ALL_POOLS[bazenId].getName();
            String freeWays = ALL_POOLS[bazenId].getFreeWaysFormatted();
            resultMessage.append(poolName)
                    .append("\n")
                    .append(freeWays)
                    .append("\n");
            ALL_POOLS[bazenId].clearResultMap();
        }
        resultMessage.append(END_MESSAGE);
        return resultMessage.toString();
    }

    private void sendPoll(Update update) {
        SendPoll poll = new SendPoll();
        poll.setChatId(update.getMessage().getChatId().toString());
        poll.setAllowMultipleAnswers(true);
        poll.setIsAnonymous(false);
        poll.setQuestion(WELCOME_QUESTION);
        poll.setOptions(optionsList);
        try {
            execute(poll);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method returns the bot's name, which was specified during registration.
     * @return bot name
     */
    @Override
    public String getBotUsername() {
        return "swiming_pools_brno_bot";
    }

    /**
     * This method returns the bot's token for communicating with the Telegram server
     * @return the bot's token
     */
    @Override
    public String getBotToken() {
        String apiKey = "";
        try {
            URL url = Resources.getResource("Config.txt");
            apiKey = Resources.toString(url, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.log(Level.INFO, "Not possible to send Bot Token {0}.", e.toString());
            e.printStackTrace();
        }
        return apiKey;
    }
}
