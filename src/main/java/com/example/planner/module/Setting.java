package com.example.planner.module;

import java.util.ArrayList;

public class Setting {

    private ArrayList<Section> sections;

    public Setting() {

    }

    public Setting(ArrayList<Section> sections) {
        this.sections = sections;
    }

    public void addSection(Section section) {
        this.sections.add(section);
    }

    public ArrayList<Section> getSections() {
        return this.sections;
    }
}
