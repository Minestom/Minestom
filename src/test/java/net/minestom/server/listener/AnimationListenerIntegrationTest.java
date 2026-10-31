package net.minestom.server.listener;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.SwingAnimation;
import net.minestom.server.network.packet.client.play.ClientPunchPacket;
import net.minestom.server.network.packet.server.play.SwingAnimationPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.testing.TestConnection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class AnimationListenerIntegrationTest {

    @Test
    public void punchSwingsWithHeldItemAnimation(Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 42, 0));
        TestConnection viewer = env.createConnection();
        viewer.connect(instance, new Pos(1, 42, 0));

        SwingAnimation stab = new SwingAnimation(SwingAnimation.Type.STAB, 3);
        player.setItemInMainHand(ItemStack.of(Material.TRIDENT).with(DataComponents.ATTACK_ANIMATION, stab));

        var tracker = viewer.trackIncoming(SwingAnimationPacket.class);
        player.addPacketToQueue(new ClientPunchPacket());
        player.interpretPacketQueue();
        tracker.assertSingle(packet -> {
            assertEquals(player.getEntityId(), packet.entityId());
            assertEquals(PlayerHand.MAIN, packet.hand());
            assertEquals(stab, packet.animation());
        });
    }

    @Test
    public void punchSwingsWithDefaultAnimation(Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 42, 0));
        TestConnection viewer = env.createConnection();
        viewer.connect(instance, new Pos(1, 42, 0));

        player.setItemInMainHand(ItemStack.of(Material.STICK));

        var tracker = viewer.trackIncoming(SwingAnimationPacket.class);
        player.addPacketToQueue(new ClientPunchPacket());
        player.interpretPacketQueue();
        tracker.assertSingle(packet -> assertEquals(SwingAnimation.DEFAULT, packet.animation()));
    }
}
