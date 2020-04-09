package fr.themode.minestom.entity.pathfinding;

import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.thread.MinestomThread;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class EntityPathFinder {

    private ExecutorService pathfindingPool = new MinestomThread(MinecraftServer.THREAD_COUNT_ENTITIES_PATHFINDING, "Ms-EntitiesPathFinding");


    private Entity entity;

    public EntityPathFinder(Entity entity) {
        this.entity = entity;
    }

    public void getPath(Position target, Consumer<LinkedList<BlockPosition>> consumer) {
        pathfindingPool.execute(() -> {
            LinkedList<BlockPosition> blockPositions = new LinkedList<>();

            JPS jps = new JPS(entity.getInstance(), entity.getPosition(), target);

            boolean first = true;
            for (Position position : jps.getPath()) {
                if (first) {
                    first = false;
                    continue;
                }
                blockPositions.add(position.toBlockPosition());
            }

            consumer.accept(blockPositions);
        });
    }

}
