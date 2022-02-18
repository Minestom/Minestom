package net.minestom.server.monitoring;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.minestom.server.MinecraftServer.THREAD_NAME_TICK;
import static net.minestom.server.MinecraftServer.THREAD_NAME_TICK_SCHEDULER;

/**
 * Small monitoring tools that can be used to check the current memory usage and Minestom threads CPU usage.
 * <p>
 * Needs to be enabled with {@link #enable(Duration)}. Memory can then be accessed with {@link #getUsedMemory()}
 * and the CPUs usage with {@link #getResultMap()} or {@link #getCpuMonitoringMessage()}.
 * <p>
 * Be aware that this is not the most accurate method, you should use a proper java profiler depending on your needs.
 */
public final class BenchmarkManager {
    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    private static final List<String> THREADS = new ArrayList<>();

    static {
        THREADS.add(THREAD_NAME_TICK_SCHEDULER);
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

    public void enable(@NotNull Duration duration) {
        Check.stateCondition(enabled, "A benchmark is already running, please disable it first.");
        THREAD_MX_BEAN.setThreadContentionMonitoringEnabled(true);
        THREAD_MX_BEAN.setThreadCpuTimeEnabled(true);

        this.time = duration.toMillis();

        final Thread thread = new Thread(null, () -> {
            while (!stop) {
                refreshData();
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
            stop = false;
        }, MinecraftServer.THREAD_NAME_BENCHMARK);
        thread.setDaemon(true);
        thread.start();

        this.enabled = true;
    }

    public void disable() {
        this.stop = true;
        this.enabled = false;
    }

    public void addThreadMonitor(@NotNull String threadName) {
        THREADS.add(threadName);
    }

    /**
     * Gets the heap memory used by the server in bytes.
     *
     * @return the memory used by the server
     */
    public long getUsedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public @NotNull Map<String, ThreadResult> getResultMap() {
        return Collections.unmodifiableMap(resultMap);
    }

    public @NotNull Component getCpuMonitoringMessage() {
        Check.stateCondition(!enabled, "CPU monitoring is only possible when the benchmark manager is enabled.");
        TextComponent.Builder benchmarkMessage = Component.text();
        for (var resultEntry : resultMap.entrySet()) {
            final String name = resultEntry.getKey();
            final ThreadResult result = resultEntry.getValue();

            benchmarkMessage.append(Component.text(name, NamedTextColor.GRAY));
            benchmarkMessage.append(Component.text(": "));
            benchmarkMessage.append(Component.text(MathUtils.round(result.getCpuPercentage(), 2), NamedTextColor.YELLOW));
            benchmarkMessage.append(Component.text("% CPU ", NamedTextColor.YELLOW));
            benchmarkMessage.append(Component.text(MathUtils.round(result.getUserPercentage(), 2), NamedTextColor.RED));
            benchmarkMessage.append(Component.text("% USER ", NamedTextColor.RED));
            benchmarkMessage.append(Component.text(MathUtils.round(result.getBlockedPercentage(), 2), NamedTextColor.LIGHT_PURPLE));
            benchmarkMessage.append(Component.text("% BLOCKED ", NamedTextColor.LIGHT_PURPLE));
            benchmarkMessage.append(Component.text(MathUtils.round(result.getWaitedPercentage(), 2), NamedTextColor.GREEN));
            benchmarkMessage.append(Component.text("% WAITED ", NamedTextColor.GREEN));
            benchmarkMessage.append(Component.newline());
        }
        return benchmarkMessage.build();
    }

    private void refreshData() {
        ThreadInfo[] threadInfo = THREAD_MX_BEAN.getThreadInfo(THREAD_MX_BEAN.getAllThreadIds());
        for (ThreadInfo threadInfo2 : threadInfo) {
            if (threadInfo2 == null) continue; // Can happen if the thread does not exist
            final String name = threadInfo2.getThreadName();
            if (THREADS.stream().noneMatch(name::startsWith)) continue;

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
