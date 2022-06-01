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

public class Bot extends TelegramLongPollingBot {

    private static String[] POOLS = new String[] {"Druzstevni", "Za Luzankami", "Kravi Hora"};

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
                poll.setQuestion("Выбери бассейны в Брно:");
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
            Bazen[] allBazens = new Bazen[] {new Druzstevni(), new ZaLuzankami(), new KraviHora()};
            for (Integer bazenId: answers){
                String bazenName = allBazens[bazenId].getClass().toString().replace("class com.simanov.", "");
                resultMessage.append(bazenName).append("\n").append(reformatFreeWays(allBazens[bazenId].getFreeWays())).append("\n");
            }

            resultMessage.append("Запустить снова -  /start ");
            SendMessage response = new SendMessage();
            System.out.println("Bot was used by " + update.getPollAnswer().getUser());
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
            result.append("\u231A " + entry.getKey() + ":00 \uD83C\uDFCA " + entry.getValue() + "\n");
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
            System.out.println("Not possible to send Bot Token");
            e.printStackTrace();
        }
        return apiKey;
    }
}
