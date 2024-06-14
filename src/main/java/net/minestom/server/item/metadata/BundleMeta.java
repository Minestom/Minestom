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
public record BundleMeta(TagReadable readable) implements ItemMetaView<BundleMeta.Builder> {
    private static final Tag<List<ItemStack>> ITEMS = Tag.ItemStack("Items").list().defaultValue(List.of());

    public @NotNull List<ItemStack> getItems() {
        return getTag(ITEMS);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder() {
            this(TagHandler.newHandler());
        }

        public Builder items(@NotNull List<ItemStack> items) {
            setTag(ITEMS, items);
            return this;
        }

        @ApiStatus.Experimental
        public Builder addItem(@NotNull ItemStack item) {
            var newList = new ArrayList<>(getTag(ITEMS));
            newList.add(item);
            return items(newList);
        }

        @ApiStatus.Experimental
        public Builder removeItem(@NotNull ItemStack item) {
            var newList = new ArrayList<>(getTag(ITEMS));
            newList.remove(item);
            return items(newList);
        }
    }
}
