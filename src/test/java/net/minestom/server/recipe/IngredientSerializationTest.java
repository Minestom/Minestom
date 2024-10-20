package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class IngredientSerializationTest {

    @Test
    public void cannotWriteAirIngredient() {
        var ingredient = new Recipe.Ingredient(List.of(ItemStack.AIR));
        assertThrows(IllegalArgumentException.class, () ->
                NetworkBuffer.makeArray(RecipeSerializers.INGREDIENT, ingredient));
    }
}
