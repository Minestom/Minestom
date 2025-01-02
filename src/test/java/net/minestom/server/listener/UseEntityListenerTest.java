package net.minestom.server.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class UseEntityListenerTest {

    private Player player;
    private Entity targetEntity;
    private boolean eventWasCalled;

    @BeforeEach
    public void setup(Env env) {
        Instance instance = env.createFlatInstance();

        player = env.createPlayer(instance, new Pos(0, 0, 0));
        player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(5.0);

        targetEntity = new Entity(EntityType.SLIME);
        targetEntity.setInstance(instance, new Pos(2, 0, 2)).join();

        eventWasCalled = false;

        player.eventNode().addListener(PlayerEntityInteractEvent.class, event -> {
            if (event.getPlayer().equals(player) && event.getTarget().equals(targetEntity)) {
                eventWasCalled = true;
            }
        });
    }

    @Test
    public void testInteractionWithinRange() {
        ClientInteractEntityPacket packet = new ClientInteractEntityPacket(
                targetEntity.getEntityId(),
                new ClientInteractEntityPacket.InteractAt(0, 0, 0, PlayerHand.MAIN),
                false
        );

        UseEntityListener.useEntityListener(packet, player);
        assertTrue(eventWasCalled, "Expected PlayerEntityInteractEvent to be called for nearby target");
    }

    @Test
    public void testInteractionOutOfRange() {
        player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(1.0);

        targetEntity.teleport(new Pos(10, 0, 10)).join();
        ClientInteractEntityPacket packet = new ClientInteractEntityPacket(
                targetEntity.getEntityId(),
                new ClientInteractEntityPacket.InteractAt(0, 0, 0, PlayerHand.MAIN),
                false
        );

        eventWasCalled = false;
        UseEntityListener.useEntityListener(packet, player);
        assertFalse(eventWasCalled, "Expected PlayerEntityInteractEvent NOT to be called for out-of-range target");
    }

    @Test
    public void testInteractionConsideringHitboxAndEyePosition() {
        player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(1.5);

        targetEntity.teleport(new Pos(1.6, 0, 0)).join();

        ClientInteractEntityPacket packet = new ClientInteractEntityPacket(
                targetEntity.getEntityId(),
                new ClientInteractEntityPacket.InteractAt(0, 0, 0, PlayerHand.MAIN),
                false
        );

        eventWasCalled = false;
        UseEntityListener.useEntityListener(packet, player);
        assertTrue(eventWasCalled, "Expected PlayerEntityInteractEvent to be called considering hitbox size and eye position");
    }


    @Test
    public void testInteractionConsideringEyeHeight() {
        player.teleport(new Pos(0, 1.6, 0)).join();
        targetEntity.teleport(new Pos(0, 1.6, 2)).join();

        ClientInteractEntityPacket packet = new ClientInteractEntityPacket(
                targetEntity.getEntityId(),
                new ClientInteractEntityPacket.InteractAt(0, 0, 0, PlayerHand.MAIN),
                false
        );

        eventWasCalled = false;
        UseEntityListener.useEntityListener(packet, player);
        assertTrue(eventWasCalled, "Expected PlayerEntityInteractEvent to be called considering eye height");
    }
}
