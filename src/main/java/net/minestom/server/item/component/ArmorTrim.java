package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ArmorTrim(@NotNull TrimMaterial material, @NotNull TrimPattern pattern, boolean showInTooltip) {

    public static final NetworkBuffer.Type<ArmorTrim> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ArmorTrim value) {
            buffer.write(NetworkBuffer.VAR_INT, value.material.id());
            buffer.write(NetworkBuffer.VAR_INT, value.pattern.id());
            buffer.write(NetworkBuffer.BOOLEAN, value.showInTooltip);
        }

        @Override
        public ArmorTrim read(@NotNull NetworkBuffer buffer) {
            TrimMaterial material = Objects.requireNonNull(TrimMaterial.fromId(buffer.read(NetworkBuffer.VAR_INT)), "unknown trim material");
            TrimPattern pattern = Objects.requireNonNull(TrimPattern.fromId(buffer.read(NetworkBuffer.VAR_INT)), "unknown trim pattern");
            return new ArmorTrim(material, pattern, buffer.read(NetworkBuffer.BOOLEAN));
        }
    };

    public static final BinaryTagSerializer<ArmorTrim> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                TrimMaterial material = Objects.requireNonNull(TrimMaterial.fromNamespaceId(tag.getString("material")), "unknown trim material");
                TrimPattern pattern = Objects.requireNonNull(TrimPattern.fromNamespaceId(tag.getString("pattern")), "unknown trim pattern");
                boolean showInTooltip = tag.getBoolean("show_in_tooltip", true);
                return new ArmorTrim(material, pattern, showInTooltip);
            },
            value -> CompoundBinaryTag.builder()
                    .putString("material", value.material.name())
                    .putString("pattern", value.pattern.name())
                    .putBoolean("show_in_tooltip", value.showInTooltip)
                    .build()
    );
}
