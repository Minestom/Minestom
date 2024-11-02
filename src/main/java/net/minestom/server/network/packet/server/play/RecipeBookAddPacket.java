package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.recipe.RecipeSerializers;
import net.minestom.server.recipe.display.RecipeDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record RecipeBookAddPacket(@NotNull List<Entry> entries, boolean replace) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final byte FLAG_NOTIFICATION = 1;
    public static final byte FLAG_HIGHLIGHT = 1 << 1;

    public static final NetworkBuffer.Type<RecipeBookAddPacket> SERIALIZER = NetworkBufferTemplate.template(
            Entry.SERIALIZER.list(), RecipeBookAddPacket::entries,
            BOOLEAN, RecipeBookAddPacket::replace,
            RecipeBookAddPacket::new);

    public record Entry(
            int displayId, @NotNull RecipeDisplay display,
            @Nullable Integer group, @NotNull RecipeBookCategory category,
            @Nullable List<Recipe.Ingredient> craftingRequirements,
            byte flags
    ) {
        public static final NetworkBuffer.Type<Entry> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.VAR_INT, Entry::displayId,
                RecipeDisplay.NETWORK_TYPE, Entry::display,
                NetworkBuffer.OPTIONAL_VAR_INT, Entry::group,
                RecipeBookCategory.NETWORK_TYPE, Entry::category,
                RecipeSerializers.INGREDIENT.list().optional(), Entry::craftingRequirements,
                NetworkBuffer.BYTE, Entry::flags,
                Entry::new);

        public Entry(int displayId, @NotNull RecipeDisplay display,
                     @Nullable Integer group, @NotNull RecipeBookCategory category,
                     @Nullable List<Recipe.Ingredient> craftingRequirements,
                     boolean notification, boolean highlight) {
            this(displayId, display, group, category, craftingRequirements,
                    (byte) ((notification ? FLAG_NOTIFICATION : 0) | (highlight ? FLAG_HIGHLIGHT : 0)));
        }

        public boolean notification() {
            return (flags & FLAG_NOTIFICATION) != 0;
        }

        public boolean highlight() {
            return (flags & FLAG_HIGHLIGHT) != 0;
        }
    }

    @Override
    public @NotNull Collection<Component> components() {
        final var components = new ArrayList<Component>();
        for (Entry entry : entries)
            components.addAll(entry.display.components());
        return components;
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        final var entries = new ArrayList<Entry>();
        for (Entry entry : this.entries) {
            entries.add(new Entry(entry.displayId, entry.display.copyWithOperator(operator),
                    entry.group, entry.category, entry.craftingRequirements, entry.flags));
        }
        return new RecipeBookAddPacket(entries, replace);
    }
}
