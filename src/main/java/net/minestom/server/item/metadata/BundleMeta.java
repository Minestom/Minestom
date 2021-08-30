package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Experimental
public class BundleMeta extends ItemMeta implements ItemMetaBuilder.Provider<BundleMeta.Builder> {

    private final List<ItemStack> items;

    protected BundleMeta(ItemMetaBuilder metaBuilder,
                         @NotNull List<ItemStack> items) {
        super(metaBuilder);
        this.items = List.copyOf(items);
    }

    public @NotNull List<ItemStack> getItems() {
        return items;
    }

    public static class Builder extends ItemMetaBuilder {
        private List<ItemStack> items = new ArrayList<>();

        public Builder items(@NotNull List<ItemStack> items) {
            this.items = new ArrayList<>(items); // defensive copy
            updateItems();
            return this;
        }

        @ApiStatus.Experimental
        public Builder addItem(@NotNull ItemStack item) {
            items.add(item);
            updateItems();
            return this;
        }

        @ApiStatus.Experimental
        public Builder removeItem(@NotNull ItemStack item) {
            items.remove(item);
            updateItems();
            return this;
        }

        @Override
        public @NotNull BundleMeta build() {
            return new BundleMeta(this, items);
        }

        private void updateItems() {
            mutateNbt(compound -> {
                NBTList<NBTCompound> itemList = new NBTList<>(NBTTypes.TAG_Compound);
                for (ItemStack item : items) {
                    itemList.add(item.toItemNBT());
                }
                compound.set("Items", itemList);
            });
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.containsKey("Items")) {
                final NBTList<NBTCompound> items = nbtCompound.getList("Items");
                for (NBTCompound item : items) {
                    this.items.add(ItemStack.fromItemNBT(item));
                }
            }
        }

        @Override
        protected @NotNull Supplier<@NotNull ItemMetaBuilder> getSupplier() {
            return Builder::new;
        }
    }
}
