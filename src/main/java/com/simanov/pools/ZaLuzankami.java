package com.simanov.pools;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class ZaLuzankami extends Pool {
    public static final String URL_STR = "https://mpsl.sportujemevbrne.cz/rezervace";

    public String getUrlStr(){
        return URL_STR;
    }

    @Override
    protected Map<Integer, Integer> getFreeWays() {
        Document doc = connect(getUrlStr());
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
        return new TreeMap<>(resultMap);
    }
}
