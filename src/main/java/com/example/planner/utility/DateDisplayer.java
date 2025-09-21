package com.example.planner.utility;

import com.example.planner.ui.CustomDatePicker;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateDisplayer extends StringConverter<LocalDate> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final CustomDatePicker picker;

    public DateDisplayer(CustomDatePicker picker) {
        this.picker = picker;
    }

    @Override
    public String toString(LocalDate date) {
        if (date == null) return "";
        char letter = picker.getLetterForDate(date);

        return formatter.format(date) + " (" + letter + " Day)";

    }

    @Override
    public LocalDate fromString(String string) {
        try {
            return LocalDate.parse(string, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}
