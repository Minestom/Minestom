package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.ItemComponent;
import net.minestom.server.item.component.ItemComponentPatch;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public record BundleMeta(ItemComponentPatch components) implements ItemMetaView<BundleMeta.Builder> {

    public @NotNull List<ItemStack> getItems() {
        return components.get(ItemComponent.BUNDLE_CONTENTS, List.of());
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
    }

    @Deprecated
    public record Builder(ItemComponentPatch.Builder components) implements ItemMetaView.Builder {

        public Builder items(@NotNull List<ItemStack> items) {
            if (items.isEmpty()) {
                components.remove(ItemComponent.BUNDLE_CONTENTS);
            } else {
                components.set(ItemComponent.BUNDLE_CONTENTS, items);
            }
            return this;
        }

        public Builder addItem(@NotNull ItemStack item) {
            var newList = new ArrayList<>(components.get(ItemComponent.BUNDLE_CONTENTS, List.of()));
            newList.add(item);
            return items(newList);
        }

        public Builder removeItem(@NotNull ItemStack item) {
            var newList = new ArrayList<>(components.get(ItemComponent.BUNDLE_CONTENTS, List.of()));
            newList.remove(item);
            return items(newList);
        }

    }
}
