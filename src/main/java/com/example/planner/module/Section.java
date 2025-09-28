package com.example.planner.module;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.UUID;

public class Section {
    private String name;
    private ArrayList<String> letterDates;
    private ArrayList<ArrayList<LocalTime>> timeSpans;
    private String color;
    private String id;

    public Section(String name,ArrayList<String> letterDates, ArrayList<ArrayList<LocalTime>> timeSpan, String color) {
        this.name = name;
        this.letterDates = letterDates;
        this.timeSpans = timeSpan;
        this.color = color;
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public Section(){
        this.id = UUID.randomUUID().toString();
    }

    public ArrayList<String> getLetterDates() {
        return letterDates;
    }

    public ArrayList<ArrayList<LocalTime>> getTimeSpans() {
        return timeSpans;
    }

    public Section(String name, String color){
        this.name = name;
        this.color = color;
    }

    public void addTimeSlot(String letterDate, ArrayList<LocalTime> time){
        this.letterDates.add(letterDate);
        this.timeSpans.add(time);
    }


    public String getName() {
        return this.name;
    }

    public String getColor() {
        return this.color;
    }
}
