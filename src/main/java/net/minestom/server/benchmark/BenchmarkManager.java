package net.minestom.server.benchmark;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.utils.validate.Check;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.minestom.server.MinecraftServer.*;

/**
 * Small monitoring tools that can be used to check the current memory usage and Minestom threads CPU usage.
 * <p>
 * Needs to be enabled with {@link #enable(UpdateOption)}. Memory can then be accessed with {@link #getUsedMemory()}
 * and the CPUs usage with {@link #getResultMap()} or {@link #getCpuMonitoringMessage()}.
 */
public final class BenchmarkManager {

    public static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    private static final List<String> THREADS = new ArrayList<>();

    static {
        THREAD_MX_BEAN.setThreadContentionMonitoringEnabled(true);
        THREAD_MX_BEAN.setThreadCpuTimeEnabled(true);

        THREADS.add(THREAD_NAME_MAIN_UPDATE);
        THREADS.add(THREAD_NAME_PACKET_WRITER);
        THREADS.add(THREAD_NAME_BLOCK_BATCH);
        THREADS.add(THREAD_NAME_SCHEDULER);
        THREADS.add(THREAD_NAME_TICK);
    }

    private final Long2LongMap lastCpuTimeMap = new Long2LongOpenHashMap();
    private final Long2LongMap lastUserTimeMap = new Long2LongOpenHashMap();
    private final Long2LongMap lastWaitedMap = new Long2LongOpenHashMap();
    private final Long2LongMap lastBlockedMap = new Long2LongOpenHashMap();

    private final Map<String, ThreadResult> resultMap = new ConcurrentHashMap<>();

    private boolean enabled = false;
    private volatile boolean stop = false;

    private long time;

    public void enable(UpdateOption updateOption) {
        Check.stateCondition(enabled, "A benchmark is already running, please disable it first.");

        time = updateOption.getTimeUnit().toMilliseconds(updateOption.getValue());

        final Thread thread = new Thread(null, () -> {

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

        thread.start();

        this.enabled = true;
    }

    public void disable() {
        this.stop = true;
        this.enabled = false;
    }

    public void addThreadMonitor(String threadName) {
        THREADS.add(threadName);
    }

    /**
     * Gets the memory used by the server in bytes.
     *
     * @return the memory used by the server
     */
    public long getUsedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public Map<String, ThreadResult> getResultMap() {
        return Collections.unmodifiableMap(resultMap);
    }

    public String getCpuMonitoringMessage() {
        String benchmarkMessage = "";
        for (Map.Entry<String, ThreadResult> resultEntry : resultMap.entrySet()) {
            final String name = resultEntry.getKey();
            final ThreadResult result = resultEntry.getValue();

            benchmarkMessage += ChatColor.GRAY + name;
            benchmarkMessage += ": ";
            benchmarkMessage += ChatColor.YELLOW.toString() + MathUtils.round(result.getCpuPercentage(), 2) + "% CPU ";
            benchmarkMessage += ChatColor.RED.toString() + MathUtils.round(result.getUserPercentage(), 2) + "% USER ";
            benchmarkMessage += ChatColor.PINK.toString() + MathUtils.round(result.getBlockedPercentage(), 2) + "% BLOCKED ";
            benchmarkMessage += ChatColor.BRIGHT_GREEN.toString() + MathUtils.round(result.getWaitedPercentage(), 2) + "% WAITED ";
            benchmarkMessage += "\n";
        }
        return benchmarkMessage;
    }

    private void refreshData() {
        ThreadInfo[] threadInfo = THREAD_MX_BEAN.getThreadInfo(THREAD_MX_BEAN.getAllThreadIds());
        for (ThreadInfo threadInfo2 : threadInfo) {
            final String name = threadInfo2.getThreadName();
            boolean shouldBenchmark = false;
            for (String thread : THREADS) {
                if (name.startsWith(thread)) {
                    shouldBenchmark = true;
                    break;
                }
            }
            if (!shouldBenchmark)
                continue;

            final long id = threadInfo2.getThreadId();

            final long lastCpuTime = lastCpuTimeMap.getOrDefault(id, 0L);
            final long lastUserTime = lastUserTimeMap.getOrDefault(id, 0L);
            final long lastWaitedTime = lastWaitedMap.getOrDefault(id, 0L);
            final long lastBlockedTime = lastBlockedMap.getOrDefault(id, 0L);

            final long blockedTime = threadInfo2.getBlockedTime();
            final long waitedTime = threadInfo2.getWaitedTime();
            final long cpuTime = THREAD_MX_BEAN.getThreadCpuTime(id);
            final long userTime = THREAD_MX_BEAN.getThreadUserTime(id);

            lastCpuTimeMap.put(id, cpuTime);
            lastUserTimeMap.put(id, userTime);
            lastWaitedMap.put(id, waitedTime);
            lastBlockedMap.put(id, blockedTime);

            final double totalCpuTime = (double) (cpuTime - lastCpuTime) / 1000000D;
            final double totalUserTime = (double) (userTime - lastUserTime) / 1000000D;
            final long totalBlocked = blockedTime - lastBlockedTime;
            final long totalWaited = waitedTime - lastWaitedTime;

            final double cpuPercentage = totalCpuTime / (double) time * 100L;
            final double userPercentage = totalUserTime / (double) time * 100L;
            final double waitedPercentage = totalWaited / (double) time * 100L;
            final double blockedPercentage = totalBlocked / (double) time * 100L;

            ThreadResult threadResult = new ThreadResult(cpuPercentage, userPercentage, waitedPercentage, blockedPercentage);
            resultMap.put(name, threadResult);
        }
    }
}
