package com.example.planner.module;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * represents a task in the planner.
 * <p>
 * a task includes fields such as title, description, due date, time span,
 * associated section, priority level, and completion status.
 */
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

    private Section section = new Section();
    private double priority = 1.0;


    /**
     *cCreates a task with full time information, including start and end times
     * @param dueDate     the date the task is due
     * @param start        the time the task begins
     * @param end          the time the task ends
     * @param letterDate   the schedule cycle letter associated with this task
     * @param timeSpan     the duration or number of periods the task spans
     * @param title        the title of the task
     * @param description  a detailed description of the task
     */
    public Task(LocalDate dueDate, LocalTime start, LocalTime end,
                char letterDate, int timeSpan, String title, String description) {
        this.dueDate = dueDate;
        this.start = start;
        this.end = end;
        this.timeSpan = timeSpan;
        this.title = title;
        this.description = description;
        this.letterDate = letterDate;
        this.id = UUID.randomUUID().toString();
    }

    /**
     * creates a task without specifying start and end times
     * useful for tasks that only have a due date and time span
     * @param dueDate     the date the task is due
     * @param letterDate   the schedule cycle letter associated with this task
     * @param timeSpan     the duration or number of periods the task spans
     * @param title        the title of the task
     * @param description  a detailed description of the task
     */
    public Task(LocalDate dueDate, char letterDate, int timeSpan,
                String title, String description) {
        this.dueDate = dueDate;
        this.letterDate = letterDate;
        this.timeSpan = timeSpan;
        this.title = title;
        this.description = description;
        this.id = UUID.randomUUID().toString();
    }



    /**
     * creates a task without specifying start and end times
     * useful for tasks that only have a due date and time span
     * @param dueDate the date the task is due
     * @param letterDate the schedule cycle letter associated with this task
     * @param timeSpan the duration or number of periods the task spans
     * @param title the title of the task
     * @param description a detailed description of the task
     */

    public Task(Section section, LocalDate dueDate, char letterDate,
                int timeSpan, String title, String description) {
        this.section = section;
        this.dueDate = dueDate;
        this.letterDate = letterDate;
        this.timeSpan = timeSpan;
        this.title = title;
        this.description = description;
        this.id = UUID.randomUUID().toString();
    }

    /**
     * creates a task with only a title and description.
     * @param title the title of the task
     * @param description the description of the task
     */
    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.id = UUID.randomUUID().toString();
    }

    /** Default constructor for Jackson */
    public Task() {}


    /**
     * @return the unique identifier of this task
     */
    public String getId() {
        return id;
    }

    /**
     * @return the due date of the task
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * @return the start time of the task
     */
    public LocalTime getStart() {
        return start;
    }

    /**
     * @return the end time of the task
     */
    public LocalTime getEnd() {
        return end;
    }

    /**
     * @return the time span of the task in minutes or periods
     */
    public int getTimeSpan() {
        return timeSpan;
    }

    /**
     * @return the title of the task
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the textual description of the task
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the letter date (e.g., schedule cycle letter)
     */
    public char getLetterDate() {
        return letterDate;
    }

    /**
     * @return the section associated with this task
     */
    public Section getSection() {
        return section;
    }

    /**
     * @return the priority level of this task
     */
    public double getPriority() {
        return priority;
    }

    /**
     * @return whether this task is marked as complete
     */
    public boolean isComplete() {
        return isComplete;
    }



    /**
     * sets the unique identifier of this task.
     * @param id the new ID value
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * sets the due date of the task.
     * @param dueDate the new due date
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * sets the start time of the task.
     * @param start the start time
     */
    public void setStart(LocalTime start) {
        this.start = start;
    }

    /**
     * sets the end time of the task.
     * @param end the end time
     */
    public void setEnd(LocalTime end) {
        this.end = end;
    }

    /**
     * sets the time span of the task.
     * @param timeSpan the new time span value
     */
    public void setTimeSpan(int timeSpan) {
        this.timeSpan = timeSpan;
    }

    /**
     * sets the title of the task.
     * @param title the new task title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * sets the description of the task.
     * @param description the new description text
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * sets the letter date for scheduling.
     * @param letterDate the schedule letter
     */
    public void setLetterDate(char letterDate) {
        this.letterDate = letterDate;
    }

    /**
     * associates this task with a new section.
     * @param section the Section object to assign
     */
    public void setSection(Section section) {
        this.section = section;
    }

    /**
     * sets the priority of this task.
     * @param priority the new priority value
     */
    public void setPriority(double priority) {
        this.priority = priority;
    }

    /**
     * sets whether the task is marked as complete.
     * @param isComplete true if completed, false otherwise
     */
    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }
}
