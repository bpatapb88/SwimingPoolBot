package com.simanov;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class Main {

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

//        Druzstevni druzstevni = new Druzstevni();
//        System.out.println(reformatFreeWays(druzstevni.getFreeWays()));

//
//        ZaLuzankami zaLuzankami = new ZaLuzankami();
//        System.out.println(zaLuzankami.getFreeWays());
//
//        KraviHora kraviHora = new KraviHora();
//        System.out.println(kraviHora.getFreeWays());
    }


}
