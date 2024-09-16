package net.minestom.server.network.packet;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeclareRecipesPacketTest {

    @Test
    public void cannotWriteAirIngredient() {
        var packet = new DeclareRecipesPacket(List.of(
                new Recipe(
                        "recipe1",
                        new Recipe.Shapeless("group1", RecipeCategory.Crafting.BUILDING,
                                List.of(new Recipe.Ingredient(List.of(ItemStack.AIR))),
                                ItemStack.of(Material.DIAMOND))
                )
        ));
        assertThrows(IllegalArgumentException.class, () -> NetworkBuffer.makeArray(DeclareRecipesPacket.SERIALIZER, packet));
    }
}
