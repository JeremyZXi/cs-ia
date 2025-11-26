package com.example.planner.ui;

import com.example.planner.module.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

/**
 * a custom card that display task in calendar view
 * <p>
 * used in Calendar
 */
public class TaskCalendarCard extends Button {

    private final Task task;
    private Color accentColor;

    /**
     * constructor creates an instance of TaskCalendarCard
     */
    public TaskCalendarCard(Task task) {
        this(task, null);
    }



    /**
     * constructor with an optional click callback.
     * The callback receives the underlying Task.
     */
    public TaskCalendarCard(Task task, Consumer<Task> onClick) {
        this.task = task;
        this.accentColor = Color.web(task.getSection().getColor());

        // Basic visual setup
        setFocusTraversable(false);
        setPadding(new Insets(2, 4, 2, 4));
        setAlignment(Pos.TOP_LEFT);
        setWrapText(true);                  // long titles wrap
        setMaxWidth(Double.MAX_VALUE);      // fill cell width if needed
        getStyleClass().add("task-calendar-card");

        // only display the title of the task
        String title = (task.getTitle() != null)
                ? task.getTitle()
                : "(no title)";
        setText(shortenString(title,15));

        // Optional click callback
        if (onClick != null) {
            setOnAction(e -> onClick.accept(this.task));
        }

        // Initial color
        applyAccentColor();
    }

    /**
     * change the accent/background color of this card.
     * @param color Color object
     */
    public void setAccentColor(Color color) {
        if (color == null) return;
        this.accentColor = color;
        applyAccentColor();
    }

    /** apply color */
    private void applyAccentColor() {
        String web = toWebColor(accentColor);
        // >>> THIS IS THE PLACE TO TWEAK COLORS / STYLE <<<
        setStyle("-fx-background-color: " + web + ";" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 2 4 2 4;" +
                "-fx-text-fill: -fx-text-inner-color;");
    }

    /**
     * convert hex to webcolor
     * @param c color object
     * @return RGB format
     */
    private String toWebColor(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    /**
     * @return task
     */
    public Task getTask() {
        return task;
    }

    private String shortenString(String text, int len){
        if (text.length() >= len){
            return text.substring(0,len-1) + "...";
        }
        else {
            return text;
        }
    }
}
