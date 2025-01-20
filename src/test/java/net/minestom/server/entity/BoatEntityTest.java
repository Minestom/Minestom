package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@EnvTest
class BoatEntityTest {

    @Test
    void testIfVelocityPacketIsNotSent(Env env) {
        final Entity entity = Mockito.spy(new Entity(EntityTypes.ACACIA_BOAT));
        Instance flatInstance = env.createFlatInstance();
        entity.setInstance(flatInstance, Pos.ZERO).join();
        for (int i = 0; i < 21; i++) {
            env.tick();
        }
        Mockito.verify(entity, Mockito.never()).sendPacketToViewers(Mockito.any());
    }

    @Test
    void testIfVelocityPacketGetSent(Env env) {
        Instance flatInstance = env.createFlatInstance();
        final Entity entity = Mockito.spy(new Entity(EntityTypes.ZOMBIE));
        entity.setInstance(flatInstance, Pos.ZERO).join();
        for (int i = 0; i < 21; i++) {
            env.tick();
        }
        Mockito.verify(entity, Mockito.times(1)).sendPacketToViewers(Mockito.any());
    }
}
