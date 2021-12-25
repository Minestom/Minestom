package net.minestom.server.world.generator;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.thread.MinestomThreadPool;
import net.minestom.server.utils.block.SectionBlockCache;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@FunctionalInterface
public interface Generator {
    ExecutorService WORLD_GEN_POOL = new MinestomThreadPool(MinecraftServer.THREAD_COUNT_WORLD_GEN, MinecraftServer.THREAD_NAME_WORLD_GEN);

    List<CompletableFuture<SectionResult>> generateSections(Instance instance, List<Vec> sections);

    record SectionResult(SectionData sectionData, Vec location) {}
    record SectionData(SectionBlockCache blockCache, Palette biomePalette) {}
}
