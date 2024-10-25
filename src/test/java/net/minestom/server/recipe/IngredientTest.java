package net.minestom.server.recipe;

import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class IngredientTest {

    @Test
    public void cannotCreateAirIngredient() {
        assertThrows(IllegalArgumentException.class, () -> new Recipe.Ingredient(Material.AIR));
    }

    @Test
    public void cannotCreateEmptyIngredient() {
        assertThrows(IllegalArgumentException.class, () -> new Recipe.Ingredient(List.of()));
    }
}
