package net.minestom.demo.block;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import net.minestom.server.tag.TagWritable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CampfireHandler implements BlockHandler {

    public static final Tag<List<ItemStack>> ITEMS = Tag.View(new TagSerializer<>() {
        private final Tag<BinaryTag> internal = Tag.NBT("Items");

        @Override
        public @Nullable List<ItemStack> read(@NotNull TagReadable reader) {
            ListBinaryTag item = (ListBinaryTag) reader.getTag(internal);
            if (item == null)
                return null;
            List<ItemStack> result = new ArrayList<>();
            item.forEach(childTag -> {
                CompoundBinaryTag nbtCompound = (CompoundBinaryTag) childTag;
                int amount = nbtCompound.getByte("Count");
                String id = nbtCompound.getString("id");
                Material material = Material.fromNamespaceId(id);
                result.add(ItemStack.of(material, amount));
            });
            return result;
        }

        @Override
        public void write(@NotNull TagWritable writer, @Nullable List<ItemStack> value) {
            if (value == null) {
                writer.removeTag(internal);
                return;
            }
            writer.setTag(internal, ListBinaryTag.listBinaryTag(
                    BinaryTagTypes.COMPOUND,
                    value.stream()
                            .map(item -> (BinaryTag) CompoundBinaryTag.builder()
                                    .putByte("Count", (byte) item.amount())
                                    .putByte("Slot", (byte) 1)
                                    .putString("id", item.material().name())
                                    .build())
                            .toList()
            ));
        }
    });

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(ITEMS);
    }

    @Override
    public @NotNull Key getNamespaceId() {
        return Key.key("minestom:test");
    }
}
