package com.simanov;

import com.simanov.leonSleep.DatabaseHandler;
import com.simanov.leonSleep.SleepCommand;
import com.simanov.leonSleep.State;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Main {

    static Logger logger;
    static {
        try(FileInputStream ins = new FileInputStream("log.config")){
            LogManager.getLogManager().readConfiguration(ins);
            logger = Logger.getLogger(Main.class.getName());
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            // botsApi.registerBot(new Bot());
            // botsApi.registerBot(new Squash());
            botsApi.registerBot(new LeonSleep());

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
