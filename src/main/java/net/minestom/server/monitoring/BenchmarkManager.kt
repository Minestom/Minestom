package net.minestom.server.monitoring

import it.unimi.dsi.fastutil.longs.Long2LongMap
import net.minestom.server.utils.validate.Check.stateCondition
import net.minestom.server.MinecraftServer.Companion.exceptionManager
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.kyori.adventure.text.Component
import net.minestom.server.monitoring.ThreadResult
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.monitoring.BenchmarkManager
import java.lang.Runnable
import java.lang.InterruptedException
import net.minestom.server.MinecraftServer
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.utils.MathUtils
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.time.Duration
import java.util.*

/**
 * Small monitoring tools that can be used to check the current memory usage and Minestom threads CPU usage.
 *
 *
 * Needs to be enabled with [.enable]. Memory can then be accessed with [.getUsedMemory]
 * and the CPUs usage with [.getResultMap] or [.getCpuMonitoringMessage].
 *
 *
 * Be aware that this is not the most accurate method, you should use a proper java profiler depending on your needs.
 */
class BenchmarkManager {
    private val lastCpuTimeMap: Long2LongMap = Long2LongOpenHashMap()
    private val lastUserTimeMap: Long2LongMap = Long2LongOpenHashMap()
    private val lastWaitedMap: Long2LongMap = Long2LongOpenHashMap()
    private val lastBlockedMap: Long2LongMap = Long2LongOpenHashMap()
    private val resultMap: MutableMap<String, ThreadResult> = ConcurrentHashMap()
    private var enabled = false

    @Volatile
    private var stop = false
    private var time: Long = 0
    fun enable(duration: Duration) {
        stateCondition(enabled, "A benchmark is already running, please disable it first.")
        try {
            THREAD_MX_BEAN.isThreadContentionMonitoringEnabled = true
            THREAD_MX_BEAN.isThreadCpuTimeEnabled = true
        } catch (e: Throwable) {
            // Likely unsupported by the JVM (e.g. Substrate VM)
            LOGGER.warn("Could not enable thread monitoring", e)
            return
        }
        time = duration.toMillis()
        val thread = Thread(null, {
            while (!stop) {
                refreshData()
                try {
                    Thread.sleep(time)
                } catch (e: InterruptedException) {
                    exceptionManager.handleException(e)
                }
            }
            stop = false
        }, MinecraftServer.THREAD_NAME_BENCHMARK)
        thread.isDaemon = true
        thread.start()
        enabled = true
    }

    fun disable() {
        stop = true
        enabled = false
    }

    fun addThreadMonitor(threadName: String) {
        THREADS.add(threadName)
    }

    /**
     * Gets the heap memory used by the server in bytes.
     *
     * @return the memory used by the server
     */
    val usedMemory: Long
        get() = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

    fun getResultMap(): Map<String, ThreadResult> {
        return Collections.unmodifiableMap(resultMap)
    }

    val cpuMonitoringMessage: Component
        get() {
            if (!enabled) return Component.text("CPU monitoring is disabled")
            val benchmarkMessage = Component.text()
            for ((name, result) in resultMap) {
                benchmarkMessage.append(Component.text(name, NamedTextColor.GRAY))
                benchmarkMessage.append(Component.text(": "))
                benchmarkMessage.append(Component.text(MathUtils.round(result.cpuPercentage, 2), NamedTextColor.YELLOW))
                benchmarkMessage.append(Component.text("% CPU ", NamedTextColor.YELLOW))
                benchmarkMessage.append(Component.text(MathUtils.round(result.userPercentage, 2), NamedTextColor.RED))
                benchmarkMessage.append(Component.text("% USER ", NamedTextColor.RED))
                benchmarkMessage.append(
                    Component.text(
                        MathUtils.round(result.blockedPercentage, 2),
                        NamedTextColor.LIGHT_PURPLE
                    )
                )
                benchmarkMessage.append(Component.text("% BLOCKED ", NamedTextColor.LIGHT_PURPLE))
                benchmarkMessage.append(
                    Component.text(
                        MathUtils.round(result.waitedPercentage, 2),
                        NamedTextColor.GREEN
                    )
                )
                benchmarkMessage.append(Component.text("% WAITED ", NamedTextColor.GREEN))
                benchmarkMessage.append(Component.newline())
            }
            return benchmarkMessage.build()
        }

    private fun refreshData() {
        val threadInfo = THREAD_MX_BEAN.getThreadInfo(THREAD_MX_BEAN.allThreadIds)
        for (threadInfo2 in threadInfo) {
            if (threadInfo2 == null) continue  // Can happen if the thread does not exist
            val name = threadInfo2.threadName
            if (THREADS.stream().noneMatch { prefix: String? ->
                    name.startsWith(
                        prefix!!
                    )
                }) continue
            val id = threadInfo2.threadId
            val lastCpuTime = lastCpuTimeMap.getOrDefault(id, 0L)
            val lastUserTime = lastUserTimeMap.getOrDefault(id, 0L)
            val lastWaitedTime = lastWaitedMap.getOrDefault(id, 0L)
            val lastBlockedTime = lastBlockedMap.getOrDefault(id, 0L)
            val blockedTime = threadInfo2.blockedTime
            val waitedTime = threadInfo2.waitedTime
            val cpuTime = THREAD_MX_BEAN.getThreadCpuTime(id)
            val userTime = THREAD_MX_BEAN.getThreadUserTime(id)
            lastCpuTimeMap[id] = cpuTime
            lastUserTimeMap[id] = userTime
            lastWaitedMap[id] = waitedTime
            lastBlockedMap[id] = blockedTime
            val totalCpuTime = (cpuTime - lastCpuTime).toDouble() / 1000000.0
            val totalUserTime = (userTime - lastUserTime).toDouble() / 1000000.0
            val totalBlocked = blockedTime - lastBlockedTime
            val totalWaited = waitedTime - lastWaitedTime
            val cpuPercentage = totalCpuTime / time.toDouble() * 100L
            val userPercentage = totalUserTime / time.toDouble() * 100L
            val waitedPercentage = totalWaited / time.toDouble() * 100L
            val blockedPercentage = totalBlocked / time.toDouble() * 100L
            val threadResult = ThreadResult(cpuPercentage, userPercentage, waitedPercentage, blockedPercentage)
            resultMap[name] = threadResult
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BenchmarkManager::class.java)
        private val THREAD_MX_BEAN = ManagementFactory.getThreadMXBean()
        private val THREADS: MutableList<String> = ArrayList()

        init {
            THREADS.add(MinecraftServer.THREAD_NAME_TICK_SCHEDULER)
            THREADS.add(MinecraftServer.THREAD_NAME_TICK)
        }
    }
}