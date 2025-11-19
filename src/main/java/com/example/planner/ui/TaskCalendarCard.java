package com.example.planner.ui;

import com.example.planner.module.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class TaskCalendarCard extends Button {

    private final Task task;
    private Color accentColor = Color.web("#e3f2fd"); // default color

    public TaskCalendarCard(Task task) {
        this(task, null);
    }



    /**
     * constructor with an optional click callback.
     * The callback receives the underlying Task.
     */
    public TaskCalendarCard(Task task, Consumer<Task> onClick) {
        this.task = task;

        // Basic visual setup
        setFocusTraversable(false);
        setPadding(new Insets(2, 4, 2, 4));
        setAlignment(Pos.TOP_LEFT);
        setWrapText(true);                  // long titles wrap
        setMaxWidth(Double.MAX_VALUE);      // fill cell width if needed
        getStyleClass().add("task-calendar-card");

        // Only display the title of the task
        String title = (task != null && task.getTitle() != null)
                ? task.getTitle()
                : "(no title)";
        setText(title);

        // Optional click callback
        if (onClick != null) {
            setOnAction(e -> onClick.accept(this.task));
        }

        // Initial color
        applyAccentColor();
    }

    /**
     * Change the accent/background color of this card.
     * This is the main hook for you to theme by priority, list, etc.
     */
    public void setAccentColor(Color color) {
        if (color == null) return;
        this.accentColor = color;
        applyAccentColor();
    }

    /** Where the color is actually applied. Customize this as you like. */
    private void applyAccentColor() {
        String web = toWebColor(accentColor);
        // >>> THIS IS THE PLACE TO TWEAK COLORS / STYLE <<<
        setStyle("-fx-background-color: " + web + ";" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 2 4 2 4;" +
                "-fx-text-fill: -fx-text-inner-color;");
    }

    private String toWebColor(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    public Task getTask() {
        return task;
    }
}
