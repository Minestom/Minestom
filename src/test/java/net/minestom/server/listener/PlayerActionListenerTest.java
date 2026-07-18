package net.minestom.server.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.player.PlayerStabEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
public class PlayerActionListenerTest {

    @Test
    public void testStabInvalidWeapon(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 0, 0));

        var tracker = env.trackEvent(PlayerStabEvent.class, EventFilter.PLAYER, player);

        PlayerActionListener.playerActionListener(new ClientPlayerActionPacket(
                ClientPlayerActionPacket.Status.STAB,
                Vec.ZERO, BlockFace.NORTH, 0
        ), player);
        tracker.assertEmpty();
    }

    @Test
    public void testStabWithWeapon(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 0, 0));
        player.setItemInMainHand(ItemStack.of(Material.NETHERITE_SPEAR));

        var tracker = env.trackEvent(PlayerStabEvent.class, EventFilter.PLAYER, player);
        PlayerActionListener.playerActionListener(new ClientPlayerActionPacket(
                ClientPlayerActionPacket.Status.STAB,
                Vec.ZERO, BlockFace.NORTH, 0
        ), player);

        tracker.assertSingle();
    }

}
