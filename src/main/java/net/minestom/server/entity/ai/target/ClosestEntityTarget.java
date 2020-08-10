package net.minestom.server.entity.ai.target;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Target the closest targetable entity (based on the class array)
 */
public class ClosestEntityTarget extends TargetSelector {

    private float range;
    private Class<? extends LivingEntity>[] entitiesTarget;

    public ClosestEntityTarget(EntityCreature entityCreature, float range,
                               Class<? extends LivingEntity>... entitiesTarget) {
        super(entityCreature);
        this.range = range;
        this.entitiesTarget = entitiesTarget;
    }

    @Override
    public Entity findTarget() {
        final Instance instance = getEntityCreature().getInstance();
        final Chunk currentChunk = instance.getChunkAt(entityCreature.getPosition());
        final List<Chunk> chunks = getNeighbours(instance, currentChunk.getChunkX(), currentChunk.getChunkZ());

        Entity entity = null;
        float distance = Float.MAX_VALUE;

        for (Chunk chunk : chunks) {
            final Set<Entity> entities = instance.getChunkEntities(chunk);

            for (Entity ent : entities) {

                // Only target living entities
                if (!(ent instanceof LivingEntity)) {
                    continue;
                }

                // Don't target itself
                if (ent.equals(entityCreature)) {
                    continue;
                }

                // Check if the entity type can be targeted
                final Class<? extends Entity> clazz = ent.getClass();
                boolean correct = false;
                for (Class<? extends LivingEntity> targetClass : entitiesTarget) {
                    if (targetClass.isAssignableFrom(clazz)) {
                        correct = true;
                        break;
                    }
                }

                if (!correct) {
                    continue;
                }

                // Check distance
                final float d = entityCreature.getDistance(ent);
                if ((entity == null || d < distance) && d < range) {
                    entity = ent;
                    distance = d;
                    continue;
                }
            }
        }

        return entity;
    }

    private List<Chunk> getNeighbours(Instance instance, int chunkX, int chunkZ) {
        List<Chunk> chunks = new ArrayList<>();
        // Constants used to loop through the neighbors
        final int[] posX = {1, 0, -1};
        final int[] posZ = {1, 0, -1};

        for (int x : posX) {
            for (int z : posZ) {

                final int targetX = chunkX + x;
                final int targetZ = chunkZ + z;
                final Chunk chunk = instance.getChunk(targetX, targetZ);
                if (ChunkUtils.isLoaded(chunk)) {
                    // Chunk is loaded, add it
                    chunks.add(chunk);
                }

            }
        }
        return chunks;
    }

}
