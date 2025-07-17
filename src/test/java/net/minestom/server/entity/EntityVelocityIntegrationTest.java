package net.minestom.server.entity;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityVelocityIntegrationTest {
    @Test
    public void gravity(Env env) {
        var instance = env.createFlatInstance();
        loadChunks(instance);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        env.tick(); // Ensure velocity downwards is present

        testMovement(env, entity, new Vec(0.0, 42.0, 0.0),
                new Vec(0.0, 41.92159999847412, 0.0),
                new Vec(0.0, 41.76636799395752, 0.0),
                new Vec(0.0, 41.53584062504456, 0.0),
                new Vec(0.0, 41.231523797587016, 0.0),
                new Vec(0.0, 40.85489329934836, 0.0),
                new Vec(0.0, 40.40739540236494, 0.0),
                new Vec(0.0, 40.0, 0.0));
    }

    @Test
    public void singleKnockback(Env env) {
        var instance = env.createFlatInstance();
        loadChunks(instance);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 0)).join();
        env.tick();
        env.tick(); // Ensures the entity is onGround
        entity.takeKnockback(0.4f, 0, -1);

        testMovement(env, entity, new Vec(0.0, 40.0, 0.0),
                new Vec(0.0, 40.360800005197525, 0.4000000059604645),
                new Vec(0.0, 40.63598401564693, 0.6184000345826153),
                new Vec(0.0, 40.827264349610196, 0.8171440663565412),
                new Vec(0.0, 40.9363190790167, 0.9980011404830835),
                new Vec(0.0, 40.96479271438924, 1.1625810826814025),
                new Vec(0.0, 40.914296876071546, 1.3123488343981535),
                new Vec(0.0, 40.7864109520312, 1.4486374923882126),
                new Vec(0.0, 40.58268274250654, 1.5726601747334787),
                new Vec(0.0, 40.304629091760695, 1.685520818920295),
                new Vec(0.0, 40.0, 1.7882240080901861),
                new Vec(0.0, 40.0, 1.8816839129282854),
                new Vec(0.0, 40.0, 1.9327130268970532),
                new Vec(0.0, 40.0, 1.9605749263602332),
                new Vec(0.0, 40.0, 1.9757875252341128),
                new Vec(0.0, 40.0, 1.9840936051840241),
                new Vec(0.0, 40.0, 1.9886287253634418),
                new Vec(0.0, 40.0, 1.9886287253634418));
    }

    @Test
    public void doubleKnockback(Env env) {
        var instance = env.createFlatInstance();
        loadChunks(instance);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 0)).join();
        env.tick();
        env.tick(); // Ensures the entity is onGround
        entity.takeKnockback(0.4f, 0, -1);
        entity.takeKnockback(0.5f, 0, -1);

        assertTrue(entity.hasVelocity());

        testMovement(env, entity, new Vec(0.0, 40.0, 0.0),
                new Vec(0.0, 40.4, 0.7000000029802322),
                new Vec(0.0, 40.71360000610351, 1.0822000490009787),
                new Vec(0.0, 40.94252801654052, 1.4300021009034531),
                new Vec(0.0, 41.088477469609366, 1.7465019772561767),
                new Vec(0.0, 41.153107934874726, 2.0345168730376946),
                new Vec(0.0, 41.138045790541625, 2.2966104357523673),
                new Vec(0.0, 41.04488488728202, 2.5351155846963964),
                new Vec(0.0, 40.87518719878482, 2.7521552764905097),
                new Vec(0.0, 40.630483459294965, 2.949661401715245),
                new Vec(0.0, 40.312273788401676, 3.1293919808495585),
                new Vec(0.0, 40.0, 3.292946812575406),
                new Vec(0.0, 40.0, 3.441781713735323),
                new Vec(0.0, 40.0, 3.523045579207649),
                new Vec(0.0, 40.0, 3.56741565490924),
                new Vec(0.0, 40.0, 3.5916417190562298),
                new Vec(0.0, 40.0, 3.6048691516168874),
                new Vec(0.0, 40.0, 3.6120913306338815),
                new Vec(0.0, 40.0, 3.616034640835186));
    }

    @Test
    public void flyingVelocity(Env env) {
        var instance = env.createFlatInstance();
        loadChunks(instance);

        var player = env.createPlayer(instance, new Pos(0, 42, 0));
        env.tick();

        final double epsilon = 0.000001;

        assertEquals(player.getVelocity().y(), -1.568, epsilon);
        double previousVelocity = player.getVelocity().y();

        player.setFlying(true);
        env.tick();

        // Every tick, the y velocity is multiplied by 0.6, and after 27 ticks it should be 0
        for (int i = 0; i < 22; i++) {
            assertEquals(player.getVelocity().y(), previousVelocity * 0.6, epsilon);
            previousVelocity = player.getVelocity().y();
            env.tick();
        }
        assertEquals(player.getVelocity().y(), 0);
    }

    @Test
    public void flyingPlayerMovement(Env env) {
        // Player movement should not send velocity packets as already client predicted
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 42, 0));
        player.setFlying(true);
        var witness = env.createConnection();
        witness.connect(instance, new Pos(0, 42, 0));

        var tracker = witness.trackIncoming(EntityVelocityPacket.class);
        env.tick(); // Process gravity velocity
        tracker.assertEmpty();
    }

    @Test
    public void testHasVelocity(Env env) {
        var instance = env.createFlatInstance();
        loadChunks(instance);

        var entity = new Entity(EntityType.ZOMBIE);
        // Should  be false because the new entity should have no velocity
        assertFalse(entity.hasVelocity());

        entity.setInstance(instance, new Pos(0, 41, 0)).join();
        entity.setVelocity(new Vec(0, -10, 0));

        env.tick();

        // Should be true: The entity is currently falling (in the air), so it does have a velocity.
        // Only entities on the ground should ignore the default velocity.
        assertTrue(entity.hasVelocity());

        // Tick entity so it falls on the ground
        for (int i = 0; i < 5; i++) {
            entity.tick(0);
        }

        // Now that the entity is on the ground, it should no longer have a velocity.
        assertFalse(entity.hasVelocity());
    }

    @Test
    public void countVelocityPackets(Env env) {
        var instance = env.createFlatInstance();
        var viewerConnection = env.createConnection();
        viewerConnection.connect(instance, new Pos(1, 40, 1));
        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 0)).join();
        instance.setBlock(new Vec(0, 39, 0), Block.STONE);
        env.tick(); // Tick because the entity is in the air, they'll send velocity from gravity

        AtomicInteger i = new AtomicInteger();
        BooleanSupplier tickLoopCondition = () -> i.getAndIncrement() < Math.max(entity.getSynchronizationTicks() - 1, 19);

        var tracker = viewerConnection.trackIncoming(EntityVelocityPacket.class);

        entity.setVelocity(new Vec(0, 5, 0));
        tracker = viewerConnection.trackIncoming(EntityVelocityPacket.class);
        i.set(0);
        env.tickWhile(tickLoopCondition, null);
        tracker.assertCount(1); // Verify the update is only sent once
    }

    private void testMovement(Env env, Entity entity, Vec... sample) {
        final double epsilon = 0.003;
        for (Vec vec : sample) {
            assertEquals(vec.x(), entity.getPosition().x(), epsilon);
            assertEquals(vec.y(), entity.getPosition().y(), epsilon);
            assertEquals(vec.z(), entity.getPosition().z(), epsilon);
            env.tick();
        }
    }

    private void loadChunks(Instance instance) {
        ChunkUtils.optionalLoadAll(instance, new long[]{
                CoordConversion.chunkIndex(-1, -1),
                CoordConversion.chunkIndex(-1, 0),
                CoordConversion.chunkIndex(-1, 1),
                CoordConversion.chunkIndex(0, -1),
                CoordConversion.chunkIndex(0, 0),
                CoordConversion.chunkIndex(0, 1),
                CoordConversion.chunkIndex(1, -1),
                CoordConversion.chunkIndex(1, 0),
                CoordConversion.chunkIndex(1, 1),
        }, null).join();
    }
}
