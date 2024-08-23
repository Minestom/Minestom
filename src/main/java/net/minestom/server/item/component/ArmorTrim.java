package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record ArmorTrim(@NotNull DynamicRegistry.Key<TrimMaterial> material,
                        @NotNull DynamicRegistry.Key<TrimPattern> pattern, boolean showInTooltip) {

    public static final NetworkBuffer.Type<ArmorTrim> NETWORK_TYPE = NetworkBufferTemplate.template(
            TrimMaterial.NETWORK_TYPE, ArmorTrim::material,
            TrimPattern.NETWORK_TYPE, ArmorTrim::pattern,
            NetworkBuffer.BOOLEAN, ArmorTrim::showInTooltip,
            ArmorTrim::new
    );

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
