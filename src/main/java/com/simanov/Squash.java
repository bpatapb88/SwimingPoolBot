package com.simanov;

import com.google.common.io.Resources;
import com.simanov.squashCenters.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static com.simanov.Main.logger;

public class Squash extends TelegramLongPollingBot {

    private Long CHAT_ID = -1001749951981L;
    private Long ADMIN_ID = 173780137L;
    private Long DAY_IN_SECONDS= 86400L;

    private static final String WELCOME_QUESTION = "Играем в сквош на следующей неделе?";
    private static final SquashCenter[] ALL_CENTERS = new SquashCenter[]{
            new FitnessBody("Fitness Body"),
            new Viktoria("Centrum Viktoria")
    };

    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage() == null){
            return;
        }
        Message recivedMessage = update.getMessage();
        if(!Objects.equals(recivedMessage.getChatId(), CHAT_ID) && !Objects.equals(recivedMessage.getChatId(), ADMIN_ID)){
            System.out.println("Wrong Chat id " + recivedMessage.getChatId());
            return;
        }else{
            System.out.println("Correct chat id " + recivedMessage.getChatId());
        }

        if(recivedMessage.isCommand() && recivedMessage.getText().equals("/start")){
            sendPolls(update);
        } else if (recivedMessage.isCommand() && recivedMessage.getText().equals("/free")){
            Calendar calendar = Calendar.getInstance();
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy,MM,dd");
            String fullDate = formatter.format(date);
            String year = fullDate.split(",")[0];
            String month = fullDate.split(",")[1];
            String day = fullDate.split(",")[2];
            calendar.set(Integer.parseInt(year),
                    Integer.parseInt(month) - 1,
                    Integer.parseInt(day));
            int day_new = calendar.get(Calendar.DAY_OF_WEEK);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            Long nextWeekDay = timestamp.getTime() + (9-day_new)*DAY_IN_SECONDS*1000;
            SquashCenter fit = ALL_CENTERS[0];

            String[] days = new String[]{"Понедельник","Вторник","Среда", "Четверг"};
            for(String dayInNextWeek : days){
                String text = dayInNextWeek + " " + new Timestamp(nextWeekDay).toString().split(" ")[0] + "\n" + fit.getPageFormated(nextWeekDay);
                sendMessage(text,recivedMessage.getChatId().toString());
                nextWeekDay+=DAY_IN_SECONDS*1000;
            }
        }
    }

    private void sendMessage(String text, String chatId){
        try{
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            execute(sendMessage);
        }catch (TelegramApiException e){
            System.out.println(e);
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
