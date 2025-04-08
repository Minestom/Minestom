package net.minestom.server.item.crossbow;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.registry.ObjectSet;
import org.jetbrains.annotations.Nullable;

public record CrossbowChargingSounds(
        @Nullable ObjectSet start,
        @Nullable ObjectSet mid,
        @Nullable ObjectSet end
) {
    private static final Codec<ObjectSet> SOUND_SET_CODEC = ObjectSet.codec(Tag.BasicType.SOUND_EVENTS);
    public static final Codec<CrossbowChargingSounds> CODEC = StructCodec.struct(
            "start", SOUND_SET_CODEC.optional(), CrossbowChargingSounds::start,
            "mid", SOUND_SET_CODEC.optional(), CrossbowChargingSounds::mid,
            "end", SOUND_SET_CODEC.optional(), CrossbowChargingSounds::end,
            CrossbowChargingSounds::new
    );
}
