package io.github.mortuusars.exposure.util;

import java.util.*;

public class ScheduledTasks {
    private static class Client {
        private static final Set<Task> tasks = new TreeSet<>(Comparator.comparingInt(Task::getDelayTicks));

        public static void tick() {
            List<Task> tasksToRemove = new ArrayList<>();

            for (Task task : tasks) {
                task.tick();

                if (task.getDelayTicks() <= 0) {
                    task.getTask().run();
                    tasksToRemove.add(task);
                }
            }

            for (Task task : tasksToRemove) {
                tasks.remove(task);
            }
        }
    }

    private static class Server {
        private static final Set<Task> tasks = new TreeSet<>(Comparator.comparingInt(Task::getDelayTicks));

        public static void tick() {
            List<Task> tasksToRemove = new ArrayList<>();

            for (Task task : tasks) {
                task.tick();

                if (task.getDelayTicks() <= 0) {
                    task.getTask().run();
                    tasksToRemove.add(task);
                }
            }

            for (Task task : tasksToRemove) {
                tasks.remove(task);
            }
        }
    }

    public static void scheduleServer(Task task) {
        Server.tasks.add(task);
    }

    public static void scheduleClient(Task task) {
        Client.tasks.add(task);
    }

    public static void tickServer() {
        Server.tick();
    }

    public static void tickClient() {
        Client.tick();
    }

    public static class Task {
        private int delayTicks;
        private final Runnable task;
        public Task(int delayTicks, Runnable task) {
            this.delayTicks = delayTicks;
            this.task = task;
        }

        public int getDelayTicks() {
            return delayTicks;
        }

        public Runnable getTask() {
            return task;
        }

        public void tick() {
            delayTicks--;
        }
    }
}
