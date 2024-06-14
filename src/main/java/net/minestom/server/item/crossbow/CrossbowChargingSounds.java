package net.minestom.server.item.crossbow;

import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.Nullable;

public record CrossbowChargingSounds(
        @Nullable ObjectSet<SoundEvent> start,
        @Nullable ObjectSet<SoundEvent> mid,
        @Nullable ObjectSet<SoundEvent> end
) {
    private static final BinaryTagSerializer<ObjectSet<SoundEvent>> SOUND_SET_NBT_TYPE = ObjectSet.nbtType(Tag.BasicType.SOUND_EVENTS);
    public static final BinaryTagSerializer<CrossbowChargingSounds> NBT_TYPE = BinaryTagSerializer.object(
            "start", SOUND_SET_NBT_TYPE.optional(), CrossbowChargingSounds::start,
            "mid", SOUND_SET_NBT_TYPE.optional(), CrossbowChargingSounds::mid,
            "end", SOUND_SET_NBT_TYPE.optional(), CrossbowChargingSounds::end,
            CrossbowChargingSounds::new
    );
}
