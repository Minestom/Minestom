package net.minestom.server.collision;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.ServerFlag;
import net.minestom.testing.Env;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class EntityProjectileCollisionIntegrationTest {

    @Test
    void blockShootAndBlockRemoval(Env env) {
        final Instance instance = env.createFlatInstance();
        instance.setWorldBorder(WorldBorder.DEFAULT_BORDER.withDiameter(1000));

        final Entity shooter = new Entity(EntityType.SKELETON);
        shooter.setInstance(instance, new Pos(0, 40, 0)).join();

        final EntityProjectile projectile = new EntityProjectile(shooter, EntityType.ARROW);
        projectile.setInstance(instance, shooter.getPosition().withY(y -> y + shooter.getEyeHeight())).join();

        final Point blockPosition = new Vec(5, 40, 0);
        final Block block = Block.GRASS_BLOCK;
        instance.setBlock(blockPosition, block);
        projectile.shoot(blockPosition, 1, 0);

        final var eventRef = new AtomicReference<ProjectileCollideWithBlockEvent>();
        MinecraftServer.getGlobalEventHandler().addListener(ProjectileCollideWithBlockEvent.class, eventRef::set);

        final long tick = TimeUnit.SERVER_TICK.getDuration().toMillis();
        for (int i = 0; i < ServerFlag.SERVER_TICKS_PER_SECOND; ++i) {
            projectile.tick(i * tick);
        }

        var event = eventRef.get();
        assertNotNull(event);
        assertEquals(blockPosition, new Vec(event.getCollisionPosition().blockX(), event.getCollisionPosition().blockY(), event.getCollisionPosition().blockZ()));
        assertEquals(block, event.getBlock());

        final var eventRef2 = new AtomicReference<ProjectileUncollideEvent>();
        MinecraftServer.getGlobalEventHandler().addListener(ProjectileUncollideEvent.class, eventRef2::set);
        eventRef.set(null);
        instance.setBlock(blockPosition, Block.AIR);

        for (int i = 0; i < ServerFlag.SERVER_TICKS_PER_SECOND; ++i) {
            projectile.tick((ServerFlag.SERVER_TICKS_PER_SECOND + i) * tick);
        }
        event = eventRef.get();
        final var event2 = eventRef2.get();
        assertNotNull(event);
        assertNotNull(event2);
        assertEquals(blockPosition.withY(y -> y - 1), new Vec(event.getCollisionPosition().blockX(), event.getCollisionPosition().blockY(), event.getCollisionPosition().blockZ()));
        eventRef.set(null);
        eventRef2.set(null);
    }

    @Test
    void entityShoot(Env env) {
        final Instance instance = env.createFlatInstance();
        instance.setWorldBorder(WorldBorder.DEFAULT_BORDER.withDiameter(1000));

        final Entity shooter = new Entity(EntityType.SKELETON);
        shooter.setInstance(instance, new Pos(0, 40, 0)).join();

        for (double dx = 1; dx <= 3; dx += .2) {
            singleEntityShoot(instance, shooter, new Vec(dx, 40, 0));
        }
    }

    private void singleEntityShoot(
            Instance instance,
            Entity shooter,
            final Point targetPosition
    ) {
        final EntityProjectile projectile = new EntityProjectile(shooter, EntityType.ARROW);
        projectile.setInstance(instance, shooter.getPosition().withY(y -> y + shooter.getEyeHeight())).join();

        final LivingEntity target = new LivingEntity(EntityType.RABBIT);
        target.setInstance(instance, Pos.fromPoint(targetPosition)).join();
        projectile.shoot(targetPosition, 1, 0);

        final var eventRef = new AtomicReference<ProjectileCollideWithEntityEvent>();
        final var eventNode = EventNode.all("projectile-test");
        eventNode.addListener(ProjectileCollideWithEntityEvent.class, event -> {
            event.getEntity().remove();
            eventRef.set(event);
            MinecraftServer.getGlobalEventHandler().removeChild(eventNode);
        });
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);

        final long tick = TimeUnit.SERVER_TICK.getDuration().toMillis();
        for (int i = 0; i < ServerFlag.SERVER_TICKS_PER_SECOND; ++i) {
            if (!projectile.isRemoved()) {
                projectile.tick(i * tick);
            }
        }

        final var event = eventRef.get();
        assertNotNull(event, "Could not hit entity at " + targetPosition);
        assertSame(target, event.getTarget());
        assertTrue(projectile.getBoundingBox().intersectEntity(event.getCollisionPosition(), target));
        target.remove();
    }

    @Test
    void entitySelfShoot(Env env) {
        final Instance instance = env.createFlatInstance();
        instance.setWorldBorder(WorldBorder.DEFAULT_BORDER.withDiameter(1000));

        final LivingEntity shooter = new LivingEntity(EntityType.SKELETON);
        shooter.setInstance(instance, new Pos(0, 40, 0)).join();

        final EntityProjectile projectile = new EntityProjectile(shooter, EntityType.ARROW);
        projectile.setInstance(instance, shooter.getPosition().withY(y -> y + shooter.getEyeHeight())).join();

        projectile.shoot(new Vec(0, 60, 0), 1, 0);

        final var eventRef = new AtomicReference<ProjectileCollideWithEntityEvent>();
        MinecraftServer.getGlobalEventHandler().addListener(ProjectileCollideWithEntityEvent.class, event -> {
            event.getEntity().remove();
            eventRef.set(event);
        });

        final long tick = TimeUnit.SERVER_TICK.getDuration().toMillis();
        for (int i = 0; i < ServerFlag.SERVER_TICKS_PER_SECOND * 5; ++i) {
            if (!projectile.isRemoved()) {
                projectile.tick(i * tick);
            }
        }

        final var event = eventRef.get();
        assertNotNull(event);
        assertSame(shooter, event.getTarget());
        assertTrue(shooter.getBoundingBox().intersectEntity(shooter.getPosition(), projectile));
        eventRef.set(null);
    }
}
