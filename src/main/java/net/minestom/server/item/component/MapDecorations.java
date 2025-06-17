package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record MapDecorations(@NotNull Map<String, Entry> decorations) {
    public static final Codec<MapDecorations> CODEC = Codec.STRING.mapValue(Entry.CODEC)
            .transform(MapDecorations::new, MapDecorations::decorations);

    public MapDecorations {
        decorations = Map.copyOf(decorations);
    }

    public @NotNull MapDecorations with(@NotNull String id, @NotNull String type, double x, double z, float rotation) {
        return with(id, new Entry(type, x, z, rotation));
    }

    public @NotNull MapDecorations with(@NotNull String id, @NotNull Entry entry) {
        Map<String, Entry> newDecorations = new HashMap<>(decorations);
        newDecorations.put(id, entry);
        return new MapDecorations(newDecorations);
    }

    public @NotNull MapDecorations remove(@NotNull String id) {
        Map<String, Entry> newDecorations = new HashMap<>(decorations);
        newDecorations.remove(id);
        return new MapDecorations(newDecorations);
    }

    public record Entry(@NotNull String type, double x, double z, float rotation) {
        public static final Codec<Entry> CODEC = StructCodec.struct(
                "type", Codec.STRING, Entry::type,
                "x", Codec.DOUBLE, Entry::x,
                "z", Codec.DOUBLE, Entry::z,
                "rotation", Codec.FLOAT, Entry::rotation,
                Entry::new);
    }
}
