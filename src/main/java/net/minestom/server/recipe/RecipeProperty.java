package net.minestom.server.recipe;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private static final Map<NamespaceID, RecipeProperty> BY_NAMESPACE = Arrays.stream(values())
            .collect(Collectors.toMap(RecipeProperty::namespace, Function.identity()));

    public static final NetworkBuffer.Type<RecipeProperty> NETWORK_TYPE = NetworkBuffer.STRING.transform(
            namespaceId -> Objects.requireNonNull(fromNamespaceId(namespaceId)),
            recipeProperty -> recipeProperty.namespace().asMinimalString());

    public static @Nullable RecipeProperty fromNamespaceId(@NotNull String namespaceId) {
        return fromNamespaceId(net.minestom.server.utils.NamespaceID.from(namespaceId));
    }

    public static @Nullable RecipeProperty fromNamespaceId(@NotNull NamespaceID namespaceId) {
        return BY_NAMESPACE.get(namespaceId);
    }

    private final NamespaceID namespace;

    RecipeProperty(@NotNull String id) {
        this.namespace = NamespaceID.from("minecraft", id);
    }

    public @NotNull NamespaceID namespace() {
        return namespace;
    }

    @Override
    public @NotNull Key key() {
        return namespace;
    }
}
