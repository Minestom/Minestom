package net.minestom.demo.block;

import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import net.minestom.server.tag.TagWritable;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CampfireHandler implements BlockHandler {

    public static final Tag<List<ItemStack>> ITEMS = Tag.View(new TagSerializer<>() {
        private final Tag<NBTList<NBTCompound>> internal = Tag.NBT("Items");

        @Override
        public @Nullable List<ItemStack> read(@NotNull TagReadable reader) {
            NBTList<NBTCompound> item = reader.getTag(internal);
            if (item == null)
                return null;
            List<ItemStack> result = new ArrayList<>();
            item.forEach(nbtCompound -> {
                int amount = nbtCompound.getAsByte("Count");
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
            writer.setTag(internal, NBT.List(
                    NBTType.TAG_Compound,
                    value.stream()
                            .map(item -> NBT.Compound(nbt -> {
                                nbt.setByte("Count", (byte) item.getAmount());
                                nbt.setByte("Slot", (byte) 1);
                                nbt.setString("id", item.getMaterial().name());
                            }))
                            .toList()
            ));
        }
    });

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(ITEMS);
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return NamespaceID.from("minestom:test");
    }
}
