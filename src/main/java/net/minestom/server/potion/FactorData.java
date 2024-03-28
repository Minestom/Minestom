package net.minestom.server.potion;

import net.minestom.server.entity.Entity;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

/**
 * Used to customize the {@link PotionEffect#DARKNESS} effect.
 */
public record FactorData(int paddingDuration, float factorStart, float factorTarget, float factorCurrent,
                         int effectChangedTimestamp, float factorPreviousFrame, boolean hadEffectLastTick) implements NetworkBuffer.Writer {
    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.NBT, this.toNBT());
    }

    public @NotNull NBTCompound toNBT() {
        return NBT.Compound(Map.of(
                "padding_duration", NBT.Int(paddingDuration),
                "factor_start", NBT.Float(factorStart),
                "factor_target", NBT.Float(factorTarget),
                "factor_current", NBT.Float(factorCurrent),
                "effect_changed_timestamp", NBT.Int(effectChangedTimestamp),
                "factor_previous_frame", NBT.Float(factorPreviousFrame),
                "had_effect_last_tick", NBT.Boolean(hadEffectLastTick)
        ));
    }

    /**
     * The default factor data that vanilla uses.
     *
     * @param entity the entity with the effect being applied to
     * @return the default factor data
     */
    public static @NotNull FactorData defaultFactorData(@NotNull Entity entity) {
        return new FactorData(22, 0, 1, 0, 0, 0, entity.hasEffect(PotionEffect.DARKNESS));
    }

    public static @NotNull FactorData fromNBT(@NotNull NBTCompound nbt) {
        return new FactorData(nbt.getInt("padding_duration"), nbt.getFloat("factor_start"), nbt.getFloat("factor_target"), nbt.getFloat("factor_current"),
                nbt.getInt("effect_changed_timestamp"), nbt.getFloat("factor_previous_frame"), nbt.getBoolean("had_effect_last_tick"));
    }
}
