package net.minestom.demo.feature.recipe;

import net.kyori.adventure.text.Component;
import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.recipe.RecipeBookCategory;

import java.util.List;

/**
 * Registers a single demo {@link ShapelessRecipe}: one dirt block → one
 * gold block named "abc". Shapeless rather than shaped to keep the data
 * concise; the same {@link ShapelessRecipe} subclass can be reused for
 * any shapeless recipe.
 */
public final class RecipeFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.recipe().addRecipe(new ShapelessRecipe(
                RecipeBookCategory.CRAFTING_MISC,
                List.of(Material.DIRT),
                ItemStack.builder(Material.GOLD_BLOCK)
                        .set(DataComponents.CUSTOM_NAME, Component.text("abc"))
                        .build()
        ));
    }
}
