package com.example.planner.module;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
public class Task {
    private String id;
    private LocalDate dueDate;
    private LocalTime start;
    private LocalTime end;
    private int timeSpan;
    private String title;
    private String description;
    private char letterDate;

    private boolean isComplete = false;

    private Section section = new Section();;
    private double priority;

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public Task(LocalDate dueDate, LocalTime start, LocalTime end, char letterDate, int timeSpan, String title, String description) {
        this.dueDate = dueDate;
        this.start = start;
        this.end = end;
        this.timeSpan = timeSpan;
        this.title = title;
        this.description = description;
        this.letterDate = letterDate;
        this.id = UUID.randomUUID().toString();
    }

    public Task(LocalDate dueDate,char letterDate,int timeSpan, String title, String description) {
        this.letterDate = letterDate;
        this.dueDate = dueDate;

        this.timeSpan = timeSpan;
        this.title = title;
        this.description = description;
        this.id = UUID.randomUUID().toString();

    }

    public void setLetterDate(char letterDate) {
        this.letterDate = letterDate;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Task(Section section, LocalDate dueDate, char letterDate, int timeSpan, String title, String description) {
        this.letterDate = letterDate;
        this.dueDate = dueDate;
        this.section = section;

        this.timeSpan = timeSpan;
        this.title = title;
        this.description = description;
        this.id = UUID.randomUUID().toString();
    }

    public Task(String title, String description) {

        this.title = title;
        this.description = description;
        this.id = UUID.randomUUID().toString();
    }
    public Task() {
        //default constructor for deserialization
    }

    public String getId() { return id; }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
       this.isComplete = isComplete;
    }

    public void setId(String id) { this.id = id; }

    public LocalDate getDueDate() {
        return dueDate;
    }
    public char getLetterDate(){return letterDate;}

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public int getTimeSpan() {
        return timeSpan;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    public void setTimeSpan(int timeSpan) {
        this.timeSpan = timeSpan;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
