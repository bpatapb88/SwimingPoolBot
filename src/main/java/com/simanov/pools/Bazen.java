package com.simanov.pools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Bazen {
    Map<Integer, Integer> resultMap = new HashMap<>();
    /**
     * @return return Document with full html
     */
    Document connect(String urlString){
        Document doc = null;
        try {
            doc = Jsoup.connect(urlString).data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(3000)
                    .post();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * @return return String with timeslots and number of free ways
     */
    public Map<Integer, Integer> getFreeWays() {
        return Collections.emptyMap();
    }

}
