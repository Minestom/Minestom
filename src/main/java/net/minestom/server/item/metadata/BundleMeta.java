package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
public record BundleMeta(TagReadable readable) implements ItemMetaView {
    private static final Tag<List<ItemStack>> ITEMS = Tag.ItemStack("Items").list();

    public @NotNull List<ItemStack> getItems() {
        return getTag(ITEMS);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder items(@NotNull List<ItemStack> items) {
            setTag(ITEMS, items);
            return this;
        }

        @ApiStatus.Experimental
        public Builder addItem(@NotNull ItemStack item) {
            var current = getTag(ITEMS);
            var newList = new ArrayList<>(current);
            newList.add(item);
            setTag(ITEMS, newList);
            return this;
        }

        @ApiStatus.Experimental
        public Builder removeItem(@NotNull ItemStack item) {
            var current = getTag(ITEMS);
            var newList = new ArrayList<>(current);
            newList.remove(item);
            setTag(ITEMS, newList);
            return this;
        }
    }
}
