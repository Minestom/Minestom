package net.minestom.server.adventure;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.util.Codec;

import java.io.IOException;

public record BinaryTagHolderImpl(BinaryTag nbt) implements BinaryTagHolder {

    @Override
    public String string() {
        try {
            return MinestomAdventure.tagStringIO().asString(nbt);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert BinaryTag to String", e);
        }
    }

    @Override
    public <T, DX extends Exception> T get(Codec<T, String, DX, ?> codec) throws DX {
        return codec.decode(string());
    }

}
