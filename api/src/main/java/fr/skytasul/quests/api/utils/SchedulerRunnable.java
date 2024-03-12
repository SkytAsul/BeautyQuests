package fr.skytasul.quests.api.utils;

import fr.euphyllia.energie.model.SchedulerTaskInter;

public class SchedulerRunnable {
    private final SchedulerTaskInter schedulerTaskInter;
    private final Runnable runnable;

    public SchedulerRunnable(SchedulerTaskInter taskInter, Runnable run) {
        this.schedulerTaskInter = taskInter;
        this.runnable = run;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public SchedulerTaskInter getSchedulerTaskInter() {
        return schedulerTaskInter;
    }
}