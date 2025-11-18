package com.example.planner.module;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Section {
    private final String name;
    private final ArrayList<String> letterDates;
    private final ArrayList<ArrayList<LocalTime>> timeSpans;
    private final String color;
    private String id;

    public Section(String name, ArrayList<String> letterDates, ArrayList<ArrayList<LocalTime>> timeSpan, String color) {
        this.name = name;
        this.letterDates = letterDates != null ? letterDates : new ArrayList<>();
        this.timeSpans = timeSpan != null ? timeSpan : new ArrayList<>();
        this.color = color;
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public Section() {
        this.id = UUID.randomUUID().toString();
        this.letterDates = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H"));
        this.timeSpans = new ArrayList<>(); // <-- init to avoid NPE
        this.name = "";
        this.color = "#FFFFFF";
    }

    public ArrayList<String> getLetterDates() {
        return letterDates;
    }


    public ArrayList<ArrayList<LocalTime>> getTimeSlots() {
        return timeSpans;
    }

    public ArrayList<ArrayList<LocalTime>> getTimeSpans() {
        return timeSpans;
    }

    public Section(String name, String color) {
        this.name = name;
        this.color = color;
        this.letterDates = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H"));
        this.timeSpans = new ArrayList<>(); // <-- init to avoid NPE
    }

    public void addTimeSlot(String letterDate, ArrayList<LocalTime> time) {
        this.letterDates.add(letterDate);
        this.timeSpans.add(time);
    }

    public String getName() {
        return this.name;
    }

    public String getColor() {
        return this.color;
    }


    /**
     * returns the (start,end) times for the first occurrence of the given letter
     */
    public ArrayList<LocalTime> getTimeSlot(String letterDate) {
        if (letterDate == null) return null;
        for (int i = 0; i < letterDates.size() && i < timeSpans.size(); i++) {
            String L = letterDates.get(i);
            if (letterDate.equalsIgnoreCase(L)) {
                return timeSpans.get(i);
            }
        }
        return null;
    }


}
