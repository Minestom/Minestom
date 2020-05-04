package net.minestom.server.benchmark;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.time.UpdateOption;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;

import static net.minestom.server.MinecraftServer.*;

public class BenchmarkManager {

    public static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static List<String> threads = Arrays.asList(THREAD_NAME_MAIN_UPDATE, THREAD_NAME_PACKET_WRITER,
            THREAD_NAME_BLOCK_BATCH, THREAD_NAME_BLOCK_UPDATE, THREAD_NAME_ENTITIES, THREAD_NAME_ENTITIES_PATHFINDING,
            THREAD_NAME_PLAYERS_ENTITIES, THREAD_NAME_SCHEDULER);

    static {
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        threadMXBean.setThreadCpuTimeEnabled(true);
    }

    private Map<Long, Long> lastCpuTimeMap = new HashMap<>();
    private Map<Long, Long> lastUserTimeMap = new HashMap<>();
    private Map<Long, Long> lastBlockedMap = new HashMap<>();

    private Map<String, ThreadResult> resultMap = new HashMap<>();

    private boolean enabled = false;
    private volatile boolean stop = false;

    private UpdateOption updateOption;
    private Thread thread;

    private long time;

    public void enable(UpdateOption updateOption) {
        if (enabled)
            throw new IllegalStateException("A benchmark is already running, please disable it first.");

        this.updateOption = updateOption;
        time = updateOption.getTimeUnit().toMilliseconds(updateOption.getValue());

        this.thread = new Thread(null, () -> {

            while (!stop) {
                refreshData();

                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            stop = false;

        }, MinecraftServer.THREAD_NAME_BENCHMARK, 0L);

        this.thread.start();

        this.enabled = true;
    }

    public void disable() {
        this.stop = true;
        this.enabled = false;
    }

    public void addThreadMonitor(String threadName) {
        threads.add(threadName);
    }

    public long getUsedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public Map<String, ThreadResult> getResultMap() {
        return Collections.unmodifiableMap(resultMap);
    }

    private void refreshData() {
        ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds());
        for (ThreadInfo threadInfo2 : threadInfo) {
            String name = threadInfo2.getThreadName();
            boolean shouldBenchmark = false;
            for (String thread : threads) {
                if (name.startsWith(thread)) {
                    shouldBenchmark = true;
                    break;
                }
            }
            if (!shouldBenchmark)
                continue;

            long id = threadInfo2.getThreadId();

            long lastCpuTime = lastCpuTimeMap.getOrDefault(id, 0L);
            long lastUserTime = lastUserTimeMap.getOrDefault(id, 0L);
            long lastBlockedTime = lastBlockedMap.getOrDefault(id, 0L);

            long blockedTime = threadInfo2.getBlockedTime();
            //long waitedTime = threadInfo2.getWaitedTime();
            long cpuTime = threadMXBean.getThreadCpuTime(id);
            long userTime = threadMXBean.getThreadUserTime(id);

            lastCpuTimeMap.put(id, cpuTime);
            lastUserTimeMap.put(id, userTime);
            lastBlockedMap.put(id, blockedTime);

            double totalCpuTime = (double) (cpuTime - lastCpuTime) / 1000000D;
            double totalUserTime = (double) (userTime - lastUserTime) / 1000000D;
            long totalBlocked = blockedTime - lastBlockedTime;

            double cpuPercentage = totalCpuTime / (double) time * 100L;
            double userPercentage = totalUserTime / (double) time * 100L;
            double blockedPercentage = totalBlocked / (double) time * 100L;

            ThreadResult threadResult = new ThreadResult(cpuPercentage, userPercentage, blockedPercentage);
            resultMap.put(name, threadResult);
        }
    }
}
