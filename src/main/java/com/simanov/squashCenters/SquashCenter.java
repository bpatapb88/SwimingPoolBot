package com.simanov.squashCenters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class SquashCenter {

    protected String centerName;

    public SquashCenter(String centerName) {
        this.centerName = centerName;
    }

    Document connect(String urlString){
        Document doc = null;
        try {
            System.out.println("connect() " + urlString);
            doc = Jsoup.connect(urlString)
                    .userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.110 Safari/537.36")
                    .timeout(0)
                    .followRedirects(true)
                    .execute()
                    .parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    public String getCenterName() {
        return centerName;
    }

    protected String getPage(Long nextMonday){
        return "";
    }

    public String getPageFormated(Long nextMonday){
        return this.getPage(nextMonday);
    }

}
