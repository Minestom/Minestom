package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

import java.util.List;
import java.util.concurrent.CompletableFuture;

class ChunkTasks {

    public final IntSet changesPositions = new IntOpenHashSet();
    public Boolean[] changedSectionSet;
    public ShortOpenHashSet queuedEdgeChecksSky;
    public ShortOpenHashSet queuedEdgeChecksBlock;
    public List<Runnable> lightTasks;

    public final CompletableFuture<Void> onComplete = new CompletableFuture<>();

}
