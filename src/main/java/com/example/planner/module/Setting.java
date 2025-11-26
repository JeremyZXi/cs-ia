package com.example.planner.module;

import java.util.ArrayList;

/**
 * A class use to hold all the section data
 * <p>
 * function as a module to hold data
 */

//TODO: some sort of graph
//TODO: allow the user to import and export
public class Setting {

    /** array contains sections*/
    private ArrayList<Section> sections;
    private boolean openNextTime;

    /** constructor for Jackson*/
    public Setting() {

    }
    /**Constructor that creates an instance of Setting
     * @param sections list of sections
     */
    public Setting(ArrayList<Section> sections) {

        this.sections = sections;
        this.openNextTime = true;
    }

    public void addSection(Section section) {
        this.sections.add(section);
    }

    public boolean isOpenNextTime() {
        return openNextTime;
    }

    public void setOpenNextTime(boolean openNextTime) {
        this.openNextTime = openNextTime;
    }

    public ArrayList<Section> getSections() {
        return this.sections;
    }
}
