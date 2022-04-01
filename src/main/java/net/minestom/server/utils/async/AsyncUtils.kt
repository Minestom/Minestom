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
    val VOID_FUTURE = CompletableFuture.completedFuture<Void?>(null)
    fun <T> empty(): CompletableFuture<T?> {
        return VOID_FUTURE as CompletableFuture<T?>
    }

    fun runAsync(runnable: Runnable): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            try {
                runnable.run()
            } catch (e: Exception) {
                MinecraftServer.getExceptionManager().handleException(e)
            }
        }
    }
}