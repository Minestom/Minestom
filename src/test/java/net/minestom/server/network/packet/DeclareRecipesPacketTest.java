package net.minestom.server.network.packet;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import net.minestom.server.recipe.RecipeCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeclareRecipesPacketTest {

    @Test
    public void cannotWriteAirIngredient() {
        var packet = new DeclareRecipesPacket(List.of(
                new DeclareRecipesPacket.DeclaredShapelessCraftingRecipe(
                        "recipe1", "group1", RecipeCategory.Crafting.BUILDING,
                        List.of(new DeclareRecipesPacket.Ingredient(List.of(ItemStack.AIR))),
                        ItemStack.of(Material.DIAMOND)
                )
        ));

        assertThrows(IllegalArgumentException.class, () -> NetworkBuffer.makeArray(packet::write));
    }
}
