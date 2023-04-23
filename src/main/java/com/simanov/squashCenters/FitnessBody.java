package com.simanov.squashCenters;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class FitnessBody extends SquashCenter {

    private static final String URL_STR = "https://88.208.115.20:28080/timeline/day?tc%5B0%5D.s=true&_tc%5B0%5D.s=on&check_all=on&_csrf=e5cf4101-d2dd-4b60-a713-a469225d92c6&criteriaTimestamp=";
    //&criteriaTimestamp=1668542837287


    private Long DAY_IN_SECONDS= 86400L;
    private static final Map<String,String> timeColMap = new HashMap<String, String>() {{
        put("time_col_1","07:00-08:00");
        put("time_col_2","08:00-09:00");
        put("time_col_3","09:00-10:00");
        put("time_col_4","10:00-11:00");
        put("time_col_5","11:00-12:00");
        put("time_col_6","12:00-13:00");
        put("time_col_7","13:00-14:00");
        put("time_col_8","14:00-15:00");
        put("time_col_9","15:00-16:00");
        put("time_col_10","16:00-17:00");
        put("time_col_11","17:00-18:00");
        put("time_col_12","18:00-19:00");
        put("time_col_13","19:00-20:00");
        put("time_col_14","20:00-21:00");
        put("time_col_15","21:00-22:00");}};

    public FitnessBody(String centerName) {
        super(centerName);
    }

    @Override
    protected String getPage(Long nextMonday) {
        List<String> timeslots = new ArrayList<>();
        timeslots.add("17:00-18:00");
        timeslots.add("17:30-18:30");
        timeslots.add("18:00-19:00");
        timeslots.add("18:30-19:30");
        timeslots.add("19:00-20:00");
        timeslots.add("19:30-20:30");
        Collections.sort(timeslots);
        List<Map<String,String>> oneDay = getOneDay(URL_STR + nextMonday);
        StringBuilder result = new StringBuilder();
        for (String timeSlot : timeslots){
            int kortsFree = getNumberFreeKorts(oneDay,timeSlot);
            if(kortsFree > 0){
                result.append("\u231A: ")
                        .append(timeSlot)
                        .append(" \uD83C\uDFBE: ")
                        .append(kortsFree)
                        .append("\n");
            }

        }
        return result.toString();
    }

    private int getNumberFreeKorts(List<Map<String, String>> oneDay, String timeSlot) {
        int result = 0;
        for (Map<String, String> kurt : oneDay){
            if(timeSlot.split("-")[0].split(":")[1].equals("30")){
                String startHours = timeSlot.split("-")[0].split(":")[0];
                int startPlus = Integer.parseInt(startHours) + 1;
                String endLeft = startPlus > 9 ? startPlus + "" : "0" + startPlus;
                String leftTime = startHours + ":00-" + endLeft + ":00";
                String rightTime = endLeft + ":00-" + ((startPlus+1) > 9 ? (startPlus+1) : "0" + (startPlus+1)) + ":00";
                if(kurt.get(leftTime) != null &&
                        kurt.get(rightTime) != null &&
                        (kurt.get(leftTime).equals("right") || kurt.get(leftTime).equals("full")) &&
                        (kurt.get(rightTime).equals("left") || kurt.get(rightTime).equals("full"))){
                    result++;
                }

            } else if(kurt.get(timeSlot) != null && kurt.get(timeSlot).equals("full")){
                result++;
            }
        }
        return result;
    }

    private List<Map<String,String>> getOneDay(String url){
        List<Map<String,String>> result = new ArrayList<>();
        Document doc = connect(url);
        Elements timetable = doc.getElementsByTag("table").get(1).getElementsByAttributeValue("data-parent-row-id","activity_hall_4");
        //Map<String, String> mapTimeStatus =
        int counter = 1;
        for(Element cort : timetable){
            Map<String,String> kurt = new HashMap<>();
            for(Element timeBlock: cort.getElementsByAttributeValueContaining("data-time-id","time_col")){
                String state = timeBlock.attr("class");
                String timeId = timeBlock.attr("data-time-id");
                if(state.equals("zoom can_book semi_full_3")){
                    String timeStart = timeBlock.getElementsByAttributeValue("class"," can_book").get(0).getElementsByAttributeValue("class","time start").text();
                    kurt.put(timeColMap.get(timeId),timeStart.split(":")[1].equals("00") ? "left" : "right");
                }else if (state.equals("zoom can_book")){
                    kurt.put(timeColMap.get(timeId),"full");
                }else if(state.contains("can_book")){
                    kurt.put(timeColMap.get(timeId),state.contains("left") ? "right" : "left");
                }
            }
            result.add(kurt);
            counter++;
        }
        return result;
    }
}
