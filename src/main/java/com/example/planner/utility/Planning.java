package com.example.planner.utility;

import com.example.planner.module.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * class use to employ dynamic programming to optimize task allocation
 * <p>
 *     turn problem into 0-1 allocation problem
 *     objective function: max priority
 *     subject to: time contrains
 * </p>
 */
public class Planning {

    public static void main(String[] args) {
        Map<String, Task> tasks = new HashMap<>();

    }

    /**
     *
     * @param tasks list of task to optimize
     * @param availableTime available time
     * @return optimized list of task
     */
    public static ArrayList<Task> plan(Map<String, Task> tasks, int availableTime) {

        List<Task> taskList = new ArrayList<>(tasks.values());
        int num = taskList.size();
        double[][] result = new double[num + 1][availableTime + 1];


        // dynamic programming for 0/1 knapsack
        for (int i = 1; i <= num; i++) {
            Task currentTask = taskList.get(i - 1);
            for (int j = 0; j <= availableTime; j++) {
                // see if select
                if (j < currentTask.getTimeSpan()) {
                    result[i][j] = result[i - 1][j]; // drop the current task because they can't fit into the time slot
                } else {
                    // time available for the current task, see if the task is prioritized or not to determine whether pick or note
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


        return sort(selectedTasks);
    }

    /**
     * sort the task based on priority
     * @param taskList list of task to sort
     * @return sorted task descending
     */
    private static ArrayList<Task> sort(ArrayList<Task> taskList) {


        boolean swapped = true;
        int n = taskList.size();

        while (swapped) {
            swapped = false;

            for (int i = 0; i < n - 1; i++) {
                Task current = taskList.get(i);
                Task next = taskList.get(i + 1);

                // big one goes first
                if (current.getPriority() < next.getPriority()) {
                    taskList.set(i, next);
                    taskList.set(i + 1, current);
                    swapped = true;
                }
            }
        }

        return new ArrayList<>(taskList);
    }

}
