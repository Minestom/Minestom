package net.minestom.server.recipe;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.network.NetworkBuffer;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum RecipeProperty implements Keyed {
    SMITHING_BASE("smithing_base"),
    SMITHING_TEMPLATE("smithing_template"),
    SMITHING_ADDITION("smithing_addition"),
    FURNACE_INPUT("furnace_input"),
    BLAST_FURNACE_INPUT("blast_furnace_input"),
    SMOKER_INPUT("smoker_input"),
    CAMPFIRE_INPUT("campfire_input");

    private static final Map<Key, RecipeProperty> BY_KEY = Arrays.stream(values())
            .collect(Collectors.toMap(RecipeProperty::key, Function.identity()));

    public static final NetworkBuffer.Type<RecipeProperty> NETWORK_TYPE = NetworkBuffer.STRING.transform(
            key -> Objects.requireNonNull(fromKey(key)),
            recipeProperty -> recipeProperty.key().asMinimalString());

    public static @Nullable RecipeProperty fromKey(String key) {
        return fromKey(Key.key(key));
    }

    public static @Nullable RecipeProperty fromKey(Key key) {
        return BY_KEY.get(key);
    }

    private final Key key;

    RecipeProperty(String id) {
        this.key = Key.key("minecraft", id);
    }

    @Override
    public Key key() {
        return key;
    }
}
