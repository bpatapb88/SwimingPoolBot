package com.simanov;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ZaLuzankami implements Bazen{
    private final String URL_STR = "https://mpsl.sportujemevbrne.cz/rezervace";
    private Map<Integer, Integer> resultMap;

    @Override
    public Map<Integer, Integer> getFreeWays() {
        resultMap = new HashMap<>();
        try {
            Document doc = Jsoup.connect(URL_STR).data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(3000)
                    .post();
            Elements timetable = doc.getElementsByTag("table").get(2).getElementsByTag("td");
            for (Element element:timetable) {
                Element span = element.getElementsByTag("span").get(0);
                int timeFrom = Integer.parseInt(span.attr("data-fullid").split("-")[4]);
                int wayNumber = Integer.parseInt(span.attr("data-fullid").split("-")[3]);
                if(span.attr("style").equals("background-color:#B2D680;") &&
                        wayNumber < 9 &&
                        timeFrom >= LocalDateTime.now().getHour()){
                    resultMap.put(timeFrom, resultMap.get(timeFrom) != null ? resultMap.get(timeFrom) + 1 : 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TreeMap<>(resultMap);
    }
}
