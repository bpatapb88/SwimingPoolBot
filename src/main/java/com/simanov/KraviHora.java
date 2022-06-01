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

public class KraviHora implements Bazen{

    private final String URL_STR = "https://www.kravihora-brno.cz/kryta-plavecka-hala";
    Map<Integer, Integer> resultMap;

    @Override
    public Map<Integer, Integer> getFreeWays() {
        resultMap = new HashMap();
        try {
            Document doc = Jsoup.connect(URL_STR).data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(3000)
                    .post();
            Elements timetable = doc.getElementsByTag("table").get(1).getElementsByTag("tr");
            timetable.remove(timetable.last());
            timetable.remove(timetable.last());
            timetable.remove(timetable.first());
            for(Element element : timetable){
                Elements oneWayRow = element.getElementsByTag("td");
                for(Element column : oneWayRow){
                    String time = column.attr("class");
                    if(!time.equals("equip-label") &&
                        column.attr("title").equals("") &&
                            LocalDateTime.now().getHour() <= Integer.parseInt(time.split("-")[1])){
                        int key = Integer.parseInt(time.split("-")[1]);
                        resultMap.put(key, resultMap.get(key) != null ? resultMap.get(key) + 1 : 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TreeMap<>(resultMap);
    }
}
