package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EntityVelocityTest {
    private static final Vec[] singleTest = new Vec[] {
            new Vec(0.0, 41.0, 0.0),
            new Vec(0.0, 41.4, 0.4000000059604645),
            new Vec(0.0, 41.71360000610351, 0.6184000345826153),
            new Vec(0.0, 41.94252801654052, 0.8171440663565412),
            new Vec(0.0, 42.088477469609366, 0.9980011404830835),
            new Vec(0.0, 42.153107934874726, 1.1625810826814025),
            new Vec(0.0, 42.138045790541625, 1.3123488343981535),
            new Vec(0.0, 42.04488488728202, 1.4486374923882126),
            new Vec(0.0, 41.87518719878482, 1.5726601747334787),
            new Vec(0.0, 41.630483459294965, 1.685520818920295),
            new Vec(0.0, 41.312273788401676, 1.7882240080901861),
            new Vec(0.0, 41.0, 1.8816839129282854),
            new Vec(0.0, 41.0, 1.9667324287820391),
            new Vec(0.0, 41.0, 2.0131689238319095),
            new Vec(0.0, 41.0, 2.0385232530741106),
            new Vec(0.0, 41.0, 2.052366718448307),
            new Vec(0.0, 41.0, 2.059925251420562),
            new Vec(0.0, 41.0, 2.06405221090277)
    };
    private static final Vec[] doubleTest = new Vec[] {
            new Vec(0.0, 41.0, 0.0),
            new Vec(0.0, 41.4, 0.7000000029802322),
            new Vec(0.0, 41.71360000610351, 1.0822000490009787),
            new Vec(0.0, 41.94252801654052, 1.4300021009034531),
            new Vec(0.0, 42.088477469609366, 1.7465019772561767),
            new Vec(0.0, 42.153107934874726, 2.0345168730376946),
            new Vec(0.0, 42.138045790541625, 2.2966104357523673),
            new Vec(0.0, 42.04488488728202, 2.5351155846963964),
            new Vec(0.0, 41.87518719878482, 2.7521552764905097),
            new Vec(0.0, 41.630483459294965, 2.949661401715245),
            new Vec(0.0, 41.312273788401676, 3.1293919808495585),
            new Vec(0.0, 41.0, 3.292946812575406),
            new Vec(0.0, 41.0, 3.441781713735323),
            new Vec(0.0, 41.0, 3.523045579207649),
            new Vec(0.0, 41.0, 3.56741565490924),
            new Vec(0.0, 41.0, 3.5916417190562298),
            new Vec(0.0, 41.0, 3.6048691516168874),
            new Vec(0.0, 41.0, 3.6120913306338815),
            new Vec(0.0, 41.0, 3.616034640835186)
    };

    @Test
    public void singleKnockback(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 41, 0)).join();
        env.tick(); // Ensures the entity is onGround
        entity.takeKnockback(0.4f, 0, -1);

        testMovement(singleTest, env, entity);
    }

    @Test
    public void doubleKnockback(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 41, 0)).join();
        env.tick(); // Ensures the entity is onGround
        entity.takeKnockback(0.4f, 0, -1);
        entity.takeKnockback(0.5f, 0, -1);

        testMovement(doubleTest, env, entity);
    }

    private void testMovement(Vec[] sample, Env env, Entity entity) {
        for (Vec vec : sample) {
            assertTrue(vec.sub(entity.getPosition()).apply(Vec.Operator.EPSILON).isZero());
            env.tick();
        }
    }
}
