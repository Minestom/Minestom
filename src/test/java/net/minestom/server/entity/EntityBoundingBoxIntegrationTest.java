package net.minestom.server.entity;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityBoundingBoxIntegrationTest {
    @Test
    public void pose(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();

        // Bounding box should be from the registry
        assertEquals(player.getEntityType().registry().boundingBox(), player.getBoundingBox());
        player.setPose(EntityPose.STANDING);
        assertEquals(player.getEntityType().registry().boundingBox(), player.getBoundingBox());

        player.setPose(EntityPose.SLEEPING);
        assertEquals(new BoundingBox(0.2, 0.2, 0.2), player.getBoundingBox());

        player.setPose(EntityPose.SNEAKING);
        assertEquals(new BoundingBox(0.6, 1.5, 0.6), player.getBoundingBox());

        player.setPose(EntityPose.FALL_FLYING);
        assertEquals(new BoundingBox(0.6, 0.6, 0.6), player.getBoundingBox());
    }

    @Test
    public void eyeHeight(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();

        assertEquals(1.62, player.getEyeHeight());

        player.setPose(EntityPose.SLEEPING);
        assertEquals(0.2, player.getEyeHeight());

        player.setPose(EntityPose.SNEAKING);
        assertEquals(1.27, player.getEyeHeight());

        player.setPose(EntityPose.FALL_FLYING);
        assertEquals(0.4, player.getEyeHeight());
    }

    @Test
    public void pickupItem(Env env) {
        final var instance = env.createFlatInstance();
        final var listener = env.listen(PickupItemEvent.class);
        final var spawnPos = new Pos(0, 42, 0);
        final var entity = new LivingEntity(EntityType.ZOMBIE);
        entity.setCanPickupItem(true);
        entity.setInstance(instance, spawnPos).join();

        var time = System.currentTimeMillis();

        dropItem(instance, spawnPos);
        listener.followup();
        entity.update(time += 1_000L);

        dropItem(instance, spawnPos.sub(.5));
        listener.followup();
        entity.update(time += 1_000L);
    }

    private void dropItem(final Instance instance, final Pos position) {
        final var entity = new ItemEntity(ItemStack.of(Material.STONE));
        entity.hasPhysics = false;
        entity.setNoGravity(true);
        entity.setInstance(instance, position).join();
    }
}
