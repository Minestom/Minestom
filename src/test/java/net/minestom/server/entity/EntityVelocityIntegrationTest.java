package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EntityVelocityIntegrationTest {
    @Test
    public void singleKnockback(Env env) {
        var instance = env.createFlatInstance();
        loadChunks(instance);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 0)).join();
        env.tick(); // Ensures the entity is onGround
        entity.takeKnockback(0.4f, 0, -1);

        testMovement(env, entity, new Vec[] {
                new Vec(0.0, 40.0, 0.0),
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
                new Vec(0.0, 40.0, 1.9886287253634418),
        });
    }

    @Test
    public void doubleKnockback(Env env) {
        var instance = env.createFlatInstance();
        loadChunks(instance);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 0)).join();
        env.tick(); // Ensures the entity is onGround
        entity.takeKnockback(0.4f, 0, -1);
        entity.takeKnockback(0.5f, 0, -1);

        testMovement(env, entity, new Vec[] {
                new Vec(0.0, 40.0, 0.0),
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
                new Vec(0.0, 40.0, 3.616034640835186)
        });
    }

    private void testMovement(Env env, Entity entity, Vec[] sample) {
        for (Vec vec : sample) {
            assertTrue(vec.sub(entity.getPosition()).apply(Vec.Operator.EPSILON).isZero());
            env.tick();
        }
    }

    private void loadChunks(Instance instance) {
        ChunkUtils.optionalLoadAll(instance, new long[] {
                ChunkUtils.getChunkIndex(-1, -1),
                ChunkUtils.getChunkIndex(-1, 0),
                ChunkUtils.getChunkIndex(-1, 1),
                ChunkUtils.getChunkIndex(0, -1),
                ChunkUtils.getChunkIndex(0, 0),
                ChunkUtils.getChunkIndex(0, 1),
                ChunkUtils.getChunkIndex(1, -1),
                ChunkUtils.getChunkIndex(1, 0),
                ChunkUtils.getChunkIndex(1, 1),
        }, null).join();
    }
}
