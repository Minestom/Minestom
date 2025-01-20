package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;
import org.mockito.Mockito;

@EnvTest
class BoatEntityTest {

    @Test
    @DisplayName("Test if velocity packet is not sent after 20 ticks if the entity a boat")
    @Issue("1880")
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
    @DisplayName("Test if velocity packet is sent after 20 ticks if the entity is not a boat")
    @Issue("1880")
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
