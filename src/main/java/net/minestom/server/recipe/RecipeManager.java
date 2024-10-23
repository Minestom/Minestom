package net.minestom.server.recipe;

import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import net.minestom.server.recipe.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class RecipeManager {
    private final CachedPacket declareRecipesPacket = new CachedPacket(this::createDeclareRecipesPacket);
    private final Map<Recipe, Predicate<Player>> recipes = new ConcurrentHashMap<>();

    public void addRecipe(@NotNull Recipe recipe, @NotNull Predicate<Player> predicate) {
        var previous = recipes.put(recipe, predicate);
        if (previous == null) {
            declareRecipesPacket.invalidate();
        }
    }

    public void addRecipe(@NotNull Recipe recipe) {
        addRecipe(recipe, player -> true);
    }

    public void removeRecipe(@NotNull Recipe recipe) {
        if (this.recipes.remove(recipe) != null) {
            declareRecipesPacket.invalidate();
        }
    }

    public List<Recipe> consumeRecipes(Player player) {
        return recipes.entrySet().stream()
                .filter(entry -> entry.getValue().test(player))
                .map(Map.Entry::getKey)
                .toList();
    }

    public @NotNull Set<Recipe> getRecipes() {
        return recipes.keySet();
    }

    public @NotNull SendablePacket getDeclareRecipesPacket() {
        return declareRecipesPacket;
    }

    private @NotNull DeclareRecipesPacket createDeclareRecipesPacket() {
        //TODO(1.21.2): Recipe property lists & stonecutter recipes
        return new DeclareRecipesPacket(Map.of(), List.of(new DeclareRecipesPacket.StonecutterRecipe(
                new Recipe.Ingredient(Material.DIAMOND),
                SlotDisplay.AnyFuel.INSTANCE)));
    }
}
