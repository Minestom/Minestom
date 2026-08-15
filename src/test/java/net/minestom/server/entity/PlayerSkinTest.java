package net.minestom.server.entity;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PlayerSkinTest {

    @Disabled
    @Test
    public void validName() {
        var skin = PlayerSkin.fromUsername("jeb_");
        assertNotNull(skin);
    }

    @Disabled
    @Test
    public void invalidName() {
        var skin = PlayerSkin.fromUsername("jfdsa84vvcxadubasdfcvn");
        assertNull(skin);
    }

    @Test
    void patchModelCodec() {
        var _ = Codec.COMPONENT; // init sucks here

        final var slimInput = CompoundBinaryTag.builder().putString("model", "slim").build();
        final var wideInput = CompoundBinaryTag.builder().putString("model", "wide").build();
        final var emptyInput = CompoundBinaryTag.empty();
        assertEquals(PlayerSkin.Model.SLIM, PlayerSkin.Patch.CODEC.decode(Transcoder.NBT, slimInput).orElseThrow().model());
        assertEquals(PlayerSkin.Model.WIDE, PlayerSkin.Patch.CODEC.decode(Transcoder.NBT, wideInput).orElseThrow().model());
        assertNull(PlayerSkin.Patch.CODEC.decode(Transcoder.NBT, emptyInput).orElseThrow().model());

        final var slimOutput = (CompoundBinaryTag) PlayerSkin.Patch.CODEC.encode(
                Transcoder.NBT, new PlayerSkin.Patch(null, null, null, true)).orElseThrow();
        final var wideOutput = (CompoundBinaryTag) PlayerSkin.Patch.CODEC.encode(
                Transcoder.NBT, new PlayerSkin.Patch(null, null, null, false)).orElseThrow();
        final var emptyOutput = (CompoundBinaryTag) PlayerSkin.Patch.CODEC.encode(
                Transcoder.NBT, PlayerSkin.Patch.EMPTY).orElseThrow();
        assertEquals("slim", slimOutput.getString("model"));
        assertEquals("wide", wideOutput.getString("model"));
        assertFalse(slimOutput.contains("slim"));
        assertFalse(wideOutput.contains("slim"));
        assertFalse(emptyOutput.contains("model"));
    }
}
