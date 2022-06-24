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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static com.simanov.Main.logger;

public class Bot extends TelegramLongPollingBot {

    private static final String[] POOL_NAMES = new String[] {
            "Za Lužánkami",
            "Kraví Hora",
            "Kohoutovice",
            "Družstevní",
            "Ponávka",
            "TJ TESLA"};
    private static final Pool[] ALL_POOLS = new Pool[] {
            new ZaLuzankami(),
            new KraviHora(),
            new Kohoutovice(),
            new Druzstevni(),
            new Ponavka(),
            new TjTesla()};
    private static final String WELCOME_QUESTION = "Выбери бассейны в Брно:";
    private static final String REMOVE_FROM_CLASS_NAME = "class com.simanov.pools.";
    private static final String END_MESSAGE = "Запустить снова -  /start ";

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage() != null && update.getMessage().isCommand() && update.getMessage().getText().equals("/start")){
            SendPoll poll = new SendPoll();
                poll.setChatId(update.getMessage().getChatId().toString());
                poll.setAllowMultipleAnswers(true);
                poll.setIsAnonymous(false);
                poll.setQuestion(WELCOME_QUESTION);
                poll.setOptions(Arrays.asList(POOL_NAMES));
            try {
                execute(poll);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }else if(update.getPollAnswer() != null){
            logger.log(Level.INFO, "User with id {0} answered poll. Start to collect data... ", update.getPollAnswer().getUser().getId());
            PollAnswer pollAnswer = update.getPollAnswer();
            List<Integer> answers = pollAnswer.getOptionIds();
            StringBuilder resultMessage = new StringBuilder();

            for (Integer bazenId: answers){
                String poolName = ALL_POOLS[bazenId].getClass().toString().replace(REMOVE_FROM_CLASS_NAME, "");
                String freeWays = ALL_POOLS[bazenId].getFreeWaysFormatted();
                resultMessage.append(poolName)
                        .append("\n")
                        .append(freeWays)
                        .append("\n");
                ALL_POOLS[bazenId].clearResultMap();
            }
            resultMessage.append(END_MESSAGE);
            SendMessage response = new SendMessage();
            response.setChatId(update.getPollAnswer().getUser().getId().toString());
            response.setText(resultMessage.toString());
            try {
                execute(response);
                logger.log(Level.INFO, "Response send to {0}", update.getPollAnswer().getUser());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            logger.log(Level.WARNING, "message= {0}", update.getMessage().getText());
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
