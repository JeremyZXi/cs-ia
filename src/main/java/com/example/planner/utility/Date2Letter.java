package com.example.planner.utility;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.time.LocalDate;
import java.util.List;

public class Date2Letter {
    private  static final List<String[]> data = readCSV("data/calendar.csv");


    public static String letterDate(LocalDate d) {
        String letter = "0";
        for (String[] row : data) {
            if (row[0].equals(d.toString())) {
                letter = String.valueOf(row[2].charAt(0));
            }
        }
        return letter;
    }
    private static List<String[]> readCSV(String file) {
        List<String[]> allData = null;
        try {
            FileReader filereader = new FileReader(file);
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withSkipLines(1)
                    .build();
            allData = csvReader.readAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return allData;
    }
}
