package com.example.planner.module;

import java.time.LocalTime;
import java.util.ArrayList;

public class Section {
    private String name;
    private ArrayList<String> letterDates;
    private ArrayList<ArrayList<LocalTime>> timeSpans;
    private String color;

    public Section(String name,ArrayList<String> letterDates, ArrayList<ArrayList<LocalTime>> timeSpan, String color) {
        this.name = name;
        this.letterDates = letterDates;
        this.timeSpans = timeSpan;
        this.color = color;
    }

    public Section(){}

    public Section(String name,String color){
        this.name = name;
        this.color = color;
    }

    public void addTimeSlot(String letterDate, ArrayList<LocalTime> time){
        this.letterDates.add(letterDate);
        this.timeSpans.add(time);
    }
    public String getSectionName(){
        return this.name;
    }

}
