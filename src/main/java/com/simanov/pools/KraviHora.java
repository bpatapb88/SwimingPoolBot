package com.simanov.pools;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class KraviHora extends Pool {

    private static final String URL_STR = "https://www.kravihora-brno.cz/kryta-plavecka-hala";


    @Override
    protected Map<Integer, Integer> getFreeWays() {
        Document doc = connect(URL_STR);
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
        return new TreeMap<>(resultMap);
    }
}
