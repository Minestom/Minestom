package net.minestom.server.entity.player;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.instance.block.predicate.PropertiesPredicate;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.registry.RegistryTag;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class PlayerBlockPlacementIntegrationTest {

    @ParameterizedTest
    @MethodSource("placeBlockFromAdventureModeParams")
    public void placeBlockFromAdventureMode(Block baseBlock, BlockPredicates canPlaceOn, Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        instance.setBlock(2, 41, 0, baseBlock);

        player.setGameMode(GameMode.ADVENTURE);
        player.setItemInMainHand(ItemStack.builder(Material.WHITE_WOOL).set(DataComponents.CAN_PLACE_ON, canPlaceOn).build());

        var packet = new ClientPlayerBlockPlacementPacket(
                PlayerHand.MAIN, new Pos(2, 41, 0), BlockFace.WEST,
                1f, 1f, 1f,
                false, false, 0
        );
        player.addPacketToQueue(packet);
        player.interpretPacketQueue();

        var placedBlock = instance.getBlock(1, 41, 0);
        assertEquals("minecraft:white_wool", placedBlock.name());
    }

    @Test
    public void placeAgainstReplaceableBlock(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        player.setItemInMainHand(ItemStack.of(Material.WHITE_WOOL, 64));

        // clicking a replaceable block places IN PLACE, like vanilla and the client's prediction
        instance.setBlock(2, 41, 0, Block.SHORT_GRASS);
        placeAgainst(player, new Pos(2, 41, 0), BlockFace.WEST);
        assertEquals(Block.WHITE_WOOL, instance.getBlock(2, 41, 0), "the replaceable block is replaced in place");
        assertTrue(instance.getBlock(1, 41, 0).isAir(), "nothing lands on the clicked face's neighbor");

        // a non-replaceable clicked block still places against the face
        placeAgainst(player, new Pos(2, 41, 0), BlockFace.WEST);
        assertEquals(Block.WHITE_WOOL, instance.getBlock(1, 41, 0));
    }

    private static void placeAgainst(Player player, Pos clicked, BlockFace face) {
        player.addPacketToQueue(new ClientPlayerBlockPlacementPacket(
                PlayerHand.MAIN, clicked, face,
                1f, 1f, 1f,
                false, false, 0));
        player.interpretPacketQueue();
    }

    private static Stream<Arguments> placeBlockFromAdventureModeParams() {
        return Stream.of(
                Arguments.of(Block.ACACIA_STAIRS.withProperty("facing", "south"), new BlockPredicates(new BlockPredicate(Block.ACACIA_STAIRS))),
                Arguments.of(Block.ACACIA_STAIRS.withProperty("facing", "south"), new BlockPredicates(new BlockPredicate(RegistryTag.direct(Block.ACACIA_STAIRS), PropertiesPredicate.exact("facing", "south"), null))),
                Arguments.of(Block.AMETHYST_BLOCK, new BlockPredicates(new BlockPredicate(Block.AMETHYST_BLOCK)))
        );
    }

}
