package net.minestom.server.item;

import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static net.minestom.server.api.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemBlockTest {

    @Test
    public void canPlace() {
        var item = ItemStack.builder(Material.STONE)
                .meta(builder -> builder.canPlaceOn(Block.STONE))
                .build();
        assertTrue(item.meta().getCanPlaceOn().contains(Block.STONE.name()));
        assertTrue(item.meta().canPlaceOn(Block.STONE));
    }

    @Test
    public void canPlaceNbt() {
        var item = ItemStack.builder(Material.STONE)
                .meta(builder -> builder.canPlaceOn(Block.STONE))
                .build();
        assertEqualsSNBT("""
                {"CanPlaceOn":["minecraft:stone"]}
                """, item.meta().toNBT());
    }

    @Test
    public void canPlaceMismatchProperties() {
        var item = ItemStack.builder(Material.STONE)
                .meta(builder -> builder.canPlaceOn(Block.SANDSTONE_STAIRS.withProperty("facing", "south")))
                .build();
        assertTrue(item.meta().getCanPlaceOn().contains(Block.SANDSTONE_STAIRS.name()));
        assertTrue(item.meta().getCanPlaceOn().contains(Block.SANDSTONE_STAIRS.withProperty("facing", "south").name()));
        assertTrue(item.meta().canPlaceOn(Block.SANDSTONE_STAIRS.withProperty("facing", "south")));
    }

    @Test
    public void canDestroy() {
        var item = ItemStack.builder(Material.STONE)
                .meta(builder -> builder.canDestroy(Block.STONE))
                .build();
        assertTrue(item.meta().getCanDestroy().contains(Block.STONE.name()));
        assertTrue(item.meta().canDestroy(Block.STONE));
    }

    @Test
    public void canDestroyNbt() {
        var item = ItemStack.builder(Material.STONE)
                .meta(builder -> builder.canDestroy(Block.STONE))
                .build();
        assertEqualsSNBT("""
                {"CanDestroy":["minecraft:stone"]}
                """, item.meta().toNBT());
    }

    @Test
    public void canDestroyMismatchProperties() {
        var item = ItemStack.builder(Material.STONE)
                .meta(builder -> builder.canDestroy(Block.SANDSTONE_STAIRS.withProperty("facing", "south")))
                .build();
        assertTrue(item.meta().getCanDestroy().contains(Block.SANDSTONE_STAIRS.name()));
        assertTrue(item.meta().getCanDestroy().contains(Block.SANDSTONE_STAIRS.withProperty("facing", "south").name()));
        assertTrue(item.meta().canDestroy(Block.SANDSTONE_STAIRS.withProperty("facing", "south")));
    }
}
