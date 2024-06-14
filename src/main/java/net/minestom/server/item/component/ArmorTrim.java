package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record ArmorTrim(@NotNull DynamicRegistry.Key<TrimMaterial> material, @NotNull DynamicRegistry.Key<TrimPattern> pattern, boolean showInTooltip) {

    public static final NetworkBuffer.Type<ArmorTrim> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ArmorTrim value) {
            buffer.write(TrimMaterial.NETWORK_TYPE, value.material);
            buffer.write(TrimPattern.NETWORK_TYPE, value.pattern);
            buffer.write(NetworkBuffer.BOOLEAN, value.showInTooltip);
        }

        @Override
        public ArmorTrim read(@NotNull NetworkBuffer buffer) {
            return new ArmorTrim(buffer.read(TrimMaterial.NETWORK_TYPE),
                    buffer.read(TrimPattern.NETWORK_TYPE),
                    buffer.read(NetworkBuffer.BOOLEAN));
        }
    };

    public static final BinaryTagSerializer<ArmorTrim> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                DynamicRegistry.Key<TrimMaterial> material = TrimMaterial.NBT_TYPE.read(tag.get("material"));
                DynamicRegistry.Key<TrimPattern> pattern = TrimPattern.NBT_TYPE.read(tag.get("pattern"));
                boolean showInTooltip = tag.getBoolean("show_in_tooltip", true);
                return new ArmorTrim(material, pattern, showInTooltip);
            },
            value -> CompoundBinaryTag.builder()
                    .put("material", TrimMaterial.NBT_TYPE.write(value.material))
                    .putString("pattern", value.pattern.name())
                    .putBoolean("show_in_tooltip", value.showInTooltip)
                    .build()
    );

    public @NotNull ArmorTrim withTooltip(boolean showInTooltip) {
        return new ArmorTrim(material, pattern, showInTooltip);
    }

}
