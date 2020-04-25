package net.minestom.server.entity.pathfinding;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class EntityPathFinder {

    private static ExecutorService pathfindingPool = new MinestomThread(MinecraftServer.THREAD_COUNT_ENTITIES_PATHFINDING, MinecraftServer.THREAD_NAME_ENTITIES_PATHFINDING);


    private Entity entity;

    public EntityPathFinder(Entity entity) {
        this.entity = entity;
    }

    public void getPath(Position target, int maxCheck, Consumer<LinkedList<BlockPosition>> consumer) {
        pathfindingPool.execute(() -> {
            Instance instance = entity.getInstance();
            BlockPosition start = entity.getPosition().toBlockPosition();
            BlockPosition end = target.toBlockPosition();

            consumer.accept(AStarPathfinder.getPath(instance, start, end, maxCheck));
        });
    }

}
