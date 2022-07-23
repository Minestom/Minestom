package net.minestom.server.item.banner;

import net.minestom.server.color.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

public record BannerPattern(@NotNull DyeColor color, @NotNull PatternType type) {

    /**
     * Retrieves a banner pattern from the given {@code compound}.
     *
     * @param compound The NBT connection, which should be a banner pattern.
     * @return A new created banner pattern.
     */
    public static @NotNull BannerPattern fromCompound(@NotNull NBTCompound compound) {
        DyeColor color = compound.containsKey("Color") ? DyeColor.byPatternColorId(compound.getByte("Color")) : DyeColor.WHITE;
        PatternType type = compound.containsKey("Pattern") ? PatternType.getByIdentifier(compound.getString("Pattern")) : PatternType.BASE;
        return new BannerPattern(color != null ? color : DyeColor.WHITE, type != null ? type : PatternType.BASE);
    }

    /**
     * Retrieves the {@link BannerPattern} as an {@link NBTCompound}.
     *
     * @return The banner pattern as a nbt compound.
     */
    public @NotNull NBTCompound asCompound() {
        return NBT.Compound(Map.of(
                "Color", NBT.Byte(color.patternColorId()),
                "Pattern", NBT.String(type.getIdentifier())
        ));
    }

}
