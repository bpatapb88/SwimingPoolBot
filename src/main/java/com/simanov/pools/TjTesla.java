package com.simanov.pools;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class TjTesla extends Pool {
    public static final String URL_STR = "https://www.tjteslabrno.cz/sportovni-zarizeni/bazen-25m.html";

    public TjTesla(String poolName) {
        super(poolName);
    }

    @Override
    protected Map<Integer, Integer> getFreeWays() {
        Document doc = connect(URL_STR);
        Elements timetable = doc.getElementById("tesla-bazen")
                .getElementsByTag("tr")
                .get(2)
                .getElementsByTag("td");
        timetable.remove(0);
        for (Element element : timetable){
            int timeFrom = 6 + timetable.indexOf(element);
            int freeWays = Integer.parseInt(element.text());
            if(freeWays > 0 && timeFrom >= LocalDateTime.now().getHour()){
                resultMap.put(timeFrom, freeWays);
            }
        }
        return new TreeMap<>(resultMap);
    }
}
