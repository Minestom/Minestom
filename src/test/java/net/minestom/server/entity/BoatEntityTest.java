package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.Issue;
import org.mockito.Mockito;

import java.util.stream.Stream;

@EnvTest
class BoatEntityTest {

    private static final Stream<Arguments> TEST_TYPES = Stream.of(
            Arguments.of(EntityType.ACACIA_BOAT),
            Arguments.of(EntityType.ACACIA_CHEST_BOAT),
            Arguments.of(EntityType.BIRCH_BOAT),
            Arguments.of(EntityType.BIRCH_CHEST_BOAT),
            Arguments.of(EntityType.CHERRY_BOAT),
            Arguments.of(EntityType.CHERRY_CHEST_BOAT),
            Arguments.of(EntityType.DARK_OAK_BOAT),
            Arguments.of(EntityType.DARK_OAK_CHEST_BOAT),
            Arguments.of(EntityType.JUNGLE_BOAT),
            Arguments.of(EntityType.JUNGLE_CHEST_BOAT),
            Arguments.of(EntityType.MANGROVE_BOAT),
            Arguments.of(EntityType.MANGROVE_CHEST_BOAT),
            Arguments.of(EntityType.OAK_BOAT),
            Arguments.of(EntityType.OAK_CHEST_BOAT),
            Arguments.of(EntityType.PALE_OAK_BOAT),
            Arguments.of(EntityType.PALE_OAK_CHEST_BOAT),
            Arguments.of(EntityType.SPRUCE_BOAT),
            Arguments.of(EntityType.SPRUCE_CHEST_BOAT),
            Arguments.of(EntityType.BAMBOO_CHEST_RAFT),
            Arguments.of(EntityType.BAMBOO_RAFT)
    );

    @ParameterizedTest
    @MethodSource("getTestTypes")
    @DisplayName("Test if velocity packet is not sent after 20 ticks if the entity a boat")
    @Issue("1880")
    void testIfVelocityPacketIsNotSent(EntityType entityType, Env env) {
        final Entity entity = Mockito.spy(new Entity(entityType));
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



    private static Stream<Arguments> getTestTypes() {
        return TEST_TYPES;
    }
}
