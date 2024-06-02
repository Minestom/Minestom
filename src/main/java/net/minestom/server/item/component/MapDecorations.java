package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record MapDecorations(@NotNull Map<String, Entry> decorations) {

    public record Entry(@NotNull String type, double x, double z, float rotation) {
    }

    static final BinaryTagSerializer<MapDecorations> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                Map<String, Entry> map = new HashMap<>(tag.size());
                for (Map.Entry<String, ? extends BinaryTag> entry : tag) {
                    if (!(entry instanceof CompoundBinaryTag entryTag)) continue;
                    map.put(entry.getKey(), new Entry(
                            entryTag.getString("type"),
                            entryTag.getDouble("x"),
                            entryTag.getDouble("z"),
                            entryTag.getFloat("rotation")
                    ));
                }
                return new MapDecorations(map);
            },
            decorations -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                for (Map.Entry<String, Entry> entry : decorations.decorations.entrySet()) {
                    CompoundBinaryTag entryTag = CompoundBinaryTag.builder()
                            .putString("type", entry.getValue().type)
                            .putDouble("x", entry.getValue().x)
                            .putDouble("z", entry.getValue().z)
                            .putFloat("rotation", entry.getValue().rotation)
                            .build();
                    builder.put(entry.getKey(), entryTag);
                }
                return builder.build();
            }
    );
}
