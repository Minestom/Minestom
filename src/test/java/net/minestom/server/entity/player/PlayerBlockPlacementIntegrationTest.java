package net.minestom.server.entity.player;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class PlayerBlockPlacementIntegrationTest {

    @Test
    public void placeBlockFromAdventureMode(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();

        instance.setBlock(2, 41, 0, Block.ACACIA_STAIRS.withProperty("facing", "south"));

        player.setGameMode(GameMode.ADVENTURE);
        player.setItemInMainHand(ItemStack.builder(Material.WHITE_WOOL).meta(m -> m.canPlaceOn(Block.ACACIA_STAIRS)).build());

        var packet = new ClientPlayerBlockPlacementPacket(
                Player.Hand.MAIN, new Pos(2, 41, 0), BlockFace.WEST,
                1f, 1f, 1f,
                false
        );
        player.addPacketToQueue(packet);
        player.interpretPacketQueue();

        var placedBlock = instance.getBlock(1, 41, 0);

        assertEquals("minecraft:white_wool", placedBlock.name());
    }

}
