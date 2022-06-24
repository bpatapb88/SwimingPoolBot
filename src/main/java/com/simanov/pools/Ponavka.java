package com.simanov.pools;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class Ponavka extends Pool {
    public static final String URL_STR = "https://ponavka.sportujemevbrne.cz/rezervace/";

    public Ponavka(String poolName) {
        super(poolName);
    }

    @Override
    protected Map<Integer, Integer> getFreeWays() {
        Document doc = connect(URL_STR);
        Elements timetable = doc.getElementsByTag("table").get(2).getElementsByTag("td");
        for (Element element:timetable) {
            Element span = element.getElementsByTag("span").get(0);
            int timeFrom = Integer.parseInt(span.attr("data-fullid").split("-")[4]);
            int wayNumber = Integer.parseInt(span.attr("data-fullid").split("-")[3]);
            if(span.attr("style").equals("background-color:#B2D680;") &&
                    wayNumber <= 3 &&
                    timeFrom >= LocalDateTime.now().getHour() &&
                    !span.text().contains("PLAVÁNÍ SENIORŮ")){
                resultMap.put(timeFrom, resultMap.get(timeFrom) != null ? resultMap.get(timeFrom) + 1 : 1);
            }
        }
        return new TreeMap<>(resultMap);
    }

}
