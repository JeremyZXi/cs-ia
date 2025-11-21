package com.example.planner.module;

import java.util.ArrayList;

/**
 * A class use to hold all the section data
 * <p>
 * function as a module to hold data
 */
public class Setting {

    /** array contains sections*/
    private ArrayList<Section> sections;

    /** constructor for Jackson*/
    public Setting() {

    }
    /**Constructor that creates an instance of Setting
     * @param sections list of sections
     */
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
