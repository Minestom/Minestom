package fr.themode.minestom.timer;

import fr.themode.minestom.Main;
import fr.themode.minestom.utils.thread.MinestomThread;
import fr.themode.minestom.utils.time.CooldownUtils;
import fr.themode.minestom.utils.time.UpdateOption;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerManager {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static ExecutorService batchesPool = new MinestomThread(Main.THREAD_COUNT_SCHEDULER, "Ms-SchedulerPool");
    private List<Task> tasks = new CopyOnWriteArrayList<>();

    public void addRepeatingTask(TaskRunnable runnable, UpdateOption updateOption) {
        runnable.setId(COUNTER.incrementAndGet());

        Task task = new Task(runnable, updateOption);
        this.tasks.add(task);
    }

    public void update() {
        long time = System.currentTimeMillis();
        batchesPool.execute(() -> {
            for (Task task : tasks) {
                UpdateOption updateOption = task.getUpdateOption();
                long lastUpdate = task.getLastUpdateTime();
                boolean hasCooldown = CooldownUtils.hasCooldown(time, lastUpdate, updateOption.getTimeUnit(), updateOption.getValue());
                if (!hasCooldown) {
                    TaskRunnable runnable = task.getRunnable();

                    runnable.run();
                    task.refreshLastUpdateTime(time);
                }
            }
        });
    }

}
