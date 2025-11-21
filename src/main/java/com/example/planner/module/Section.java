package com.example.planner.module;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
/**
 * A class hold information about a section
 * <p>
 * name, time, period, color, etc.
 */
public class Section {

    /**name of the section*/
    private final String name;

    /**date in a cycle in which that section present*/
    private final ArrayList<String> letterDates;
    /**time span of the class(start and end)*/
    private final ArrayList<ArrayList<LocalTime>> timeSpans;

    /**color as a tag*/
    private final String color;

    /**UUID of that section*/
    private String id;


    /**
     * Creates a new Section instance
     * @param name the name of the section
     * @param letterDates the list of date which the section take palce
     * @param timeSpan time spans of the section
     * @param color the color tag of the section
     */
    public Section(String name, ArrayList<String> letterDates, ArrayList<ArrayList<LocalTime>> timeSpan, String color) {
        this.name = name;
        this.letterDates = letterDates != null ? letterDates : new ArrayList<>();
        this.timeSpans = timeSpan != null ? timeSpan : new ArrayList<>();
        this.color = color;
        this.id = UUID.randomUUID().toString();
    }
    /**
     * Creates a new Section instance
     * @param name the name of the section
     * @param color the color tag of the section
     */
    public Section(String name, String color) {
        this.name = name;
        this.color = color;
        this.letterDates = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H"));
        this.timeSpans = new ArrayList<>(); // <-- init to avoid NPE
    }

    /**
     * Constructor for Jackson
     */
    public Section() {
        this.id = UUID.randomUUID().toString();
        this.letterDates = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H"));
        this.timeSpans = new ArrayList<>(); // <-- init to avoid NPE
        this.name = "";
        this.color = "#FFFFFF";
    }


    public String getId() {
        return id;
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
     * @return timeSpan
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
