package com.example.planner.utility;

import com.example.planner.module.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Planning {

    public static void main(String[] args) {
        Map<String, Task> tasks = new HashMap<>();

    }

    public static ArrayList<Task> plan(Map<String, Task> tasks, int availableTime) {

        List<Task> taskList = new ArrayList<>(tasks.values());
        int num = taskList.size();
        double[][] result = new double[num + 1][availableTime + 1];


        // dynamic programming for 0/1 knapsack
        for (int i = 1; i <= num; i++) {
            Task currentTask = taskList.get(i - 1);
            for (int j = 0; j <= availableTime; j++) {
                if (j < currentTask.getTimeSpan()) {
                    result[i][j] = result[i - 1][j];
                } else {
                    result[i][j] = Math.max(
                            result[i - 1][j],
                            result[i - 1][j - currentTask.getTimeSpan()] + currentTask.getPriority()
                    );
                }
            }
        }

        System.out.println("Optimal priority: " + result[num][availableTime]);

        // backtrack to find selected tasks
        ArrayList<Task> selectedTasks = new ArrayList<>();
        int j = availableTime;

        for (int i = num; i > 0; i--) {
            if (result[i][j] != result[i - 1][j]) {
                Task selected = taskList.get(i - 1);
                selectedTasks.add(selected);
                j -= selected.getTimeSpan();
            }
        }
        //TODO: Add sorting based on priority

        return selectedTasks;
    }
}
