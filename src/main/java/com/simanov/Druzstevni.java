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

public class Druzstevni implements Bazen{
    private final String URL_STR = "https://www.druzstevni.cz/bazeny/rozvrh-hodin/";
    private static final int COUNT_OF_WAYS_IN_POOL = 6;
    private Map<Integer, Integer> resultMap;

    public Druzstevni() {

    }

    @Override
    public Map<Integer, Integer> getFreeWays(){
        resultMap = new HashMap();
        try {
            Document doc = Jsoup.connect(URL_STR).data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(3000)
                    .post();
            // parsing start
            Elements timetable = doc.getElementById("timetable-1").children().get(0).children();
            timetable.remove(0);
            timetable.remove(0);
            timetable.remove(0);
            for(Element element : timetable){
                Elements timeSlotPerWay = element.getElementsByTag("td");
                for(Element timeSlot : timeSlotPerWay){
                    appendResultMap(resultMap, timeSlot);
                }
                if(element.getElementsByTag("tr").get(0).attr("class").equals("title-day")){
                    break;
                }
            }

            //parsing end
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TreeMap<>(resultMap);
    }

    private void appendResultMap(Map<Integer, Integer> resultMap, Element element) {
        if (element.attr("title").equals("Otevřeno pro veřejnost.") &&
                Integer.parseInt(element.attr("data-x")) <= COUNT_OF_WAYS_IN_POOL &&
                !element.attr("data-q").equals("1") &&
                checkDate(element.attr("data-time"))){

            int key = dataTimeToInt(element.attr("data-time"));
            resultMap.put(key, resultMap.get(key) != null ? resultMap.get(key) + 1 : 1);
        }

    }

    private boolean checkDate(String attr) {
        int fromTime = Integer.parseInt(attr.split(",")[0]);
        int hours = fromTime / 100;
        int minutes = fromTime % 100;
        int nowHours = LocalDateTime.now().getHour();
        return minutes == 0 && nowHours <= hours;
    }

    private int dataTimeToInt(String attr){
        int fromTime = Integer.parseInt(attr.split(",")[0]);
        return fromTime / 100;
    }
}
