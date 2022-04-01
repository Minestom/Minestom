package net.minestom.server.monitoring

import net.minestom.server.utils.validate.Check.stateCondition
import net.minestom.server.MinecraftServer.Companion.exceptionManager
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.minestom.server.monitoring.ThreadResult
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.monitoring.BenchmarkManager
import java.lang.Runnable
import java.lang.InterruptedException
import net.minestom.server.MinecraftServer
import net.kyori.adventure.text.format.NamedTextColor

class TickMonitor(val tickTime: Double, val acquisitionTime: Double)