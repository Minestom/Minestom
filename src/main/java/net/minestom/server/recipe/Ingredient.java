package net.minestom.server.recipe;


import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.recipe.display.SlotDisplay;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record Ingredient(@NotNull List<@NotNull Material> items) {
    public static final NetworkBuffer.Type<Ingredient> NETWORK_TYPE = NetworkBufferTemplate.template(
            // FIXME(1.21.2): This is really an ObjectSet, but currently ObjectSet does not properly support
            //  non-dynamic registry types. We need to improve how the tag system works generally.
            new NetworkBuffer.Type<>() {
                @Override
                public void write(@NotNull NetworkBuffer buffer, List<Material> value) {
                    // +1 because 0 indicates that an item tag name follows (in this case it does not).
                    buffer.write(VAR_INT, value.size() + 1);
                    for (Material material : value) {
                        buffer.write(Material.NETWORK_TYPE, material);
                    }
                }

                @Override
                public List<Material> read(@NotNull NetworkBuffer buffer) {
                    int size = buffer.read(VAR_INT) - 1;
                    Check.notNull(size > Short.MAX_VALUE, "too many ingredients");
                    if (size == -1) {
                        throw new UnsupportedOperationException("cannot read ingredient tags yet");
                    }

                    final List<Material> materials = new ArrayList<>(size);
                    for (int i = 0; i < size; i++)
                        materials.add(buffer.read(Material.NETWORK_TYPE));
                    return materials;
                }
            }, Ingredient::items,
            Ingredient::new
    );

    public Ingredient {
        items = List.copyOf(items);
        Check.argCondition(items.isEmpty(), "Ingredients can't be empty");
        Check.argCondition(items.contains(Material.AIR), "Ingredient can't contain air");
    }

    public Ingredient(@NotNull Material @NotNull ... items) {
        this(List.of(items));
    }

    public static @Nullable Ingredient fromSlotDisplay(@NotNull SlotDisplay slotDisplay) {
        return switch (slotDisplay) {
            case SlotDisplay.Item item -> new Ingredient(item.material());
            case SlotDisplay.Tag ignored -> {
                // TODO: Support tags in ingredients (ObjectSet for non static registries)
                yield null;
            }
            default -> null;
        };
    }
}
