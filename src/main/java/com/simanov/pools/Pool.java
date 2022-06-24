package com.simanov.pools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Pool {
    Map<Integer, Integer> resultMap = new HashMap<>();
    private static final String ONE_LINE_RESULT = "\u231A %s:00 \uD83C\uDFCA %s\n";
    protected String poolName;

    public Pool(String poolName) {
        this.poolName = poolName;
    }

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
     * This method is overridden for every pool
     */
    protected Map<Integer, Integer> getFreeWays() {
        return Collections.emptyMap();
    }

    /**
     * @return return name of pool as String
     */
    public String getName(){
        return poolName;
    }

    public void clearResultMap(){
        resultMap.clear();
    }

    public String getFreeWaysFormatted(){
        Map<Integer, Integer> freeWays = this.getFreeWays();
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : freeWays.entrySet()){
            String str = String.format(ONE_LINE_RESULT, entry.getKey(),entry.getValue());
            result.append(str);
        }
        return result.toString();
    }
}
