package net.minestom.server.entity.player;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.instance.block.predicate.PropertiesPredicate;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
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
    public void placementRule(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        player.setItemInMainHand(ItemStack.of(Material.WHITE_WOOL, 64));
        instance.setBlock(2, 41, 0, Block.STONE);

        var blocker = new Entity(EntityType.ZOMBIE);
        blocker.setInstance(instance, new Pos(1.5, 41, 0.5)).join();

        placeAgainstStone(player);
        assertTrue(instance.getBlock(1, 41, 0).isAir(), "a colliding entity blocks placement by default");

        blocker.updatePlacementRule(p -> false);
        placeAgainstStone(player);
        assertEquals(Block.WHITE_WOOL, instance.getBlock(1, 41, 0), "an exempting rule lets the placement through");

        instance.setBlock(1, 41, 0, Block.AIR);
        blocker.updatePlacementRule(null);
        placeAgainstStone(player);
        assertTrue(instance.getBlock(1, 41, 0).isAir(), "clearing the rule restores the default");

        // the rule outranks the type default in the other direction too: a marker armor stand never blocks...
        blocker.remove();
        var marker = new Entity(EntityType.ARMOR_STAND);
        ((ArmorStandMeta) marker.getEntityMeta()).setMarker(true);
        marker.setInstance(instance, new Pos(1.5, 41, 0.5)).join();
        placeAgainstStone(player);
        assertEquals(Block.WHITE_WOOL, instance.getBlock(1, 41, 0));

        // ...unless its rule says otherwise
        instance.setBlock(1, 41, 0, Block.AIR);
        marker.updatePlacementRule(p -> true);
        placeAgainstStone(player);
        assertTrue(instance.getBlock(1, 41, 0).isAir(), "a blocking rule outranks the type default");
    }

    private static void placeAgainstStone(Player player) {
        player.addPacketToQueue(new ClientPlayerBlockPlacementPacket(
                PlayerHand.MAIN, new Pos(2, 41, 0), BlockFace.WEST,
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
