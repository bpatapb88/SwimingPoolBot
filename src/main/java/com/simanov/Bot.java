package com.simanov;

import com.google.common.io.Resources;
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
import java.util.Map;
import java.util.logging.Level;

import static com.simanov.Main.logger;

public class Bot extends TelegramLongPollingBot {

    private static final String[] POOLS = new String[] {"Druzstevni", "Za Luzankami", "Kravi Hora"};
    private static final Bazen[] allBazens = new Bazen[] {new Druzstevni(), new ZaLuzankami(), new KraviHora()};
    private static final String WELCOME_QUESTION = "Выбери бассейны в Брно:";
    private static final String REMOVE_FROM_CLASS_NAME = "class com.simanov.";
    private static final String END = "Запустить снова -  /start ";
    private static final String ONE_LINE_RESULT = "\u231A %s:00 \uD83C\uDFCA %s\n";

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
                poll.setOptions(Arrays.asList(POOLS));
            try {
                execute(poll);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }else if(update.getPollAnswer() != null){
            PollAnswer pollAnswer = update.getPollAnswer();
            List<Integer> answers = pollAnswer.getOptionIds();
            StringBuilder resultMessage = new StringBuilder();

            for (Integer bazenId: answers){
                String bazenName = allBazens[bazenId].getClass().toString().replace(REMOVE_FROM_CLASS_NAME, "");
                resultMessage.append(bazenName).append("\n").append(reformatFreeWays(allBazens[bazenId].getFreeWays())).append("\n");
            }
            resultMessage.append(END);
            SendMessage response = new SendMessage();
            logger.log(Level.INFO, "Bot was used by {0}", update.getPollAnswer().getUser());
            response.setChatId(update.getPollAnswer().getUser().getId().toString());
            response.setText(resultMessage.toString());
            try {
                execute(response);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private static String reformatFreeWays(Map<Integer, Integer> input){
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : input.entrySet()){
            String str = String.format(ONE_LINE_RESULT, entry.getKey(),entry.getValue());
            result.append(str);
        }
        return result.toString();
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
