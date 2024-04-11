package net.minestom.server.item.metadata;

import net.minestom.server.color.Color;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.DyedItemColor;
import net.minestom.server.item.component.ItemComponent;
import net.minestom.server.item.component.ItemComponentPatch;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Deprecated
public record LeatherArmorMeta(@NotNull ItemComponentPatch components) implements ItemMetaView<LeatherArmorMeta.Builder> {

    public @Nullable Color getColor() {
        DyedItemColor value = components.get(ItemComponent.DYED_COLOR);
        return value == null ? null : value.color();
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
    }

    @Deprecated
    public record Builder(@NotNull ItemComponentPatch.Builder components) implements ItemMetaView.Builder {

        public Builder color(@Nullable Color color) {
            if (color == null) {
                components.remove(ItemComponent.DYED_COLOR);
            } else {
                DyedItemColor value = components.get(ItemComponent.DYED_COLOR, DyedItemColor.LEATHER);
                components.set(ItemComponent.DYED_COLOR, value.withColor(color));
            }
            return this;
        }
    }
}
