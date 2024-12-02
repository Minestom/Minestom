package net.minestom.server.recipe;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import net.minestom.server.network.packet.server.play.RecipeBookAddPacket;
import net.minestom.server.recipe.display.RecipeDisplay;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public final class RecipeManager {
    private static final AtomicInteger NEXT_DISPLAY_ID = new AtomicInteger();

    private record RecipeData(
            @NotNull Recipe recipe,
            @NotNull List<RecipeBookAddPacket.Entry> displays,
            @NotNull Predicate<Player> predicate
    ) {
    }

    private final CachedPacket declareRecipesPacket = new CachedPacket(this::createDeclareRecipesPacket);

    private final Map<Recipe, RecipeData> recipes = new ConcurrentHashMap<>();
    private final Int2ObjectMap<Map.Entry<RecipeBookAddPacket.Entry, Predicate<Player>>> recipeBookEntryIdMap =
            Int2ObjectMaps.synchronize(new Int2ObjectArrayMap<>());

    public void addRecipe(@NotNull Recipe recipe) {
        addRecipe(recipe, player -> true);
    }

    public void addRecipe(@NotNull Recipe recipe, @NotNull Predicate<Player> predicate) {
        List<RecipeBookAddPacket.Entry> recipeBookEntries = new ArrayList<>();
        final RecipeBookCategory recipeBookCategory = recipe.recipeBookCategory();
        if (recipeBookCategory != null) {
            for (var display : recipe.createRecipeDisplays()) {
                int displayId = NEXT_DISPLAY_ID.getAndIncrement();
                recipeBookEntries.add(new RecipeBookAddPacket.Entry( //todo groups
                        displayId, display, null, recipeBookCategory,
                        recipe.craftingRequirements(), false, false
                ));
            }
        }

        var existingRecipe = recipes.putIfAbsent(recipe, new RecipeData(recipe, recipeBookEntries, predicate));
        Check.argCondition(existingRecipe != null, "Recipe is already registered: " + recipe);
        for (RecipeBookAddPacket.Entry entry : recipeBookEntries) {
            recipeBookEntryIdMap.put(entry.displayId(), Map.entry(entry, predicate));
        }
    }

    public void removeRecipe(@NotNull Recipe recipe) {
        final RecipeData removed = recipes.remove(recipe);
        if (removed != null) {
            for (var entry : removed.displays) {
                recipeBookEntryIdMap.remove(entry.displayId());
            }
        }
    }

    public @NotNull Set<Recipe> getRecipes() {
        return recipes.keySet();
    }

    /**
     * Get the recipe display for the specified display id, optionally testing visibility against the given player.
     *
     * @param displayId the display id
     * @param player    the player to test visibility against, or null to ignore visibility
     * @return the recipe display, or null if not found or not visible
     */
    public @Nullable RecipeDisplay getRecipeDisplay(int displayId, @Nullable Player player) {
        var recipeBookEntry = recipeBookEntryIdMap.get(displayId);
        if (recipeBookEntry == null || (player != null && !recipeBookEntry.getValue().test(player))) return null;

        return recipeBookEntry.getKey().display();
    }

    public @NotNull SendablePacket getDeclareRecipesPacket() {
        return declareRecipesPacket;
    }

    /**
     * Creates a {@link RecipeBookAddPacket} which replaces the recipe book with the currently unlocked
     * recipes for this player.
     *
     * @param player the player to create the packet for
     * @return the recipe book add packet with replace set to true
     */
    public @NotNull RecipeBookAddPacket createRecipeBookResetPacket(@NotNull Player player) {
        final List<RecipeBookAddPacket.Entry> entries = new ArrayList<>();
        for (final Map.Entry<Recipe, RecipeData> recipeEntry : recipes.entrySet()) {
            if (!recipeEntry.getValue().predicate.test(player)) continue;

            entries.addAll(recipeEntry.getValue().displays);
        }
        return new RecipeBookAddPacket(entries, true);
    }

    private @NotNull DeclareRecipesPacket createDeclareRecipesPacket() {
        // Collect the item properties for the client
        final Map<RecipeProperty, Set<Material>> itemProperties = new HashMap<>();
        for (var recipe : recipes.keySet()) {
            for (var entry : recipe.itemProperties().entrySet()) {
                itemProperties.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).addAll(entry.getValue());
            }
        }
        final Map<RecipeProperty, List<Material>> itemPropertiesLists = new HashMap<>();
        for (var entry : itemProperties.entrySet()) { // Sets to lists
            itemPropertiesLists.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // Collect the stonecutter recipes for the client
        final List<DeclareRecipesPacket.StonecutterRecipe> stonecutterRecipes = new ArrayList<>();
        for (var recipeBookEntry : recipeBookEntryIdMap.values()) {
            if (!(recipeBookEntry.getKey().display() instanceof RecipeDisplay.Stonecutter stonecutterDisplay))
                continue;

            final Ingredient input = Ingredient.fromSlotDisplay(stonecutterDisplay.ingredient());
            if (input == null) continue;

            stonecutterRecipes.add(new DeclareRecipesPacket.StonecutterRecipe(input, stonecutterDisplay.result()));
        }

        return new DeclareRecipesPacket(itemPropertiesLists, stonecutterRecipes);
    }

}
