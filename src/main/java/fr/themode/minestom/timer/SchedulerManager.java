package fr.themode.minestom.timer;

import fr.themode.minestom.Main;
import fr.themode.minestom.utils.thread.MinestomThread;
import fr.themode.minestom.utils.time.CooldownUtils;
import fr.themode.minestom.utils.time.UpdateOption;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerManager {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static ExecutorService batchesPool = new MinestomThread(Main.THREAD_COUNT_SCHEDULER, "Ms-SchedulerPool");
    private List<Task> tasks = new CopyOnWriteArrayList<>();

    public int addTask(TaskRunnable runnable, UpdateOption updateOption, int maxCallCount) {
        int id = COUNTER.incrementAndGet();
        runnable.setId(id);

        Task task = new Task(runnable, updateOption, maxCallCount);
        this.tasks.add(task);

        return id;
    }

    public int addRepeatingTask(TaskRunnable runnable, UpdateOption updateOption) {
        return addTask(runnable, updateOption, 0);
    }

    public int addDelayedTask(TaskRunnable runnable, UpdateOption updateOption) {
        return addTask(runnable, updateOption, 1);
    }

    public void removeTask(int taskId) {
        synchronized (tasks) {
            this.tasks.removeIf(task -> task.getId() == taskId);
        }
    }

    public void update() {
        long time = System.currentTimeMillis();
        batchesPool.execute(() -> {

            synchronized (tasks) {
                Iterator<Task> iterator = tasks.iterator();
                while (iterator.hasNext()) {
                    Task task = iterator.next();

                    UpdateOption updateOption = task.getUpdateOption();
                    long lastUpdate = task.getLastUpdateTime();
                    boolean hasCooldown = CooldownUtils.hasCooldown(time, lastUpdate, updateOption.getTimeUnit(), updateOption.getValue());
                    if (!hasCooldown) {
                        TaskRunnable runnable = task.getRunnable();
                        int maxCallCount = task.getMaxCallCount();
                        int callCount = runnable.getCallCount() + 1;
                        runnable.setCallCount(callCount);

                        runnable.run();

                        task.refreshLastUpdateTime(time);

                        if (callCount == maxCallCount) {
                            iterator.remove();
                        }
                    }
                }
            }
        });
    }

}
