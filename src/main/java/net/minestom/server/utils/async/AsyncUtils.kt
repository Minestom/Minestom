package net.minestom.server.utils.async

import java.util.concurrent.CompletableFuture
import java.lang.Void
import net.minestom.server.utils.async.AsyncUtils
import java.lang.Runnable
import net.minestom.server.MinecraftServer
import org.jetbrains.annotations.ApiStatus
import java.lang.Exception

@ApiStatus.Internal
object AsyncUtils {
    @JvmField
    val VOID_FUTURE = CompletableFuture.completedFuture<Void?>(null)
    @JvmStatic
    fun <T> empty(): CompletableFuture<T?> {
        return VOID_FUTURE as CompletableFuture<T?>
    }

    @JvmStatic
    fun runAsync(runnable: Runnable): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            try {
                runnable.run()
            } catch (e: Exception) {
                MinecraftServer.exceptionManager.handleException(e)
            }
        }
    }
}