package net.minestom.server.particle.data;

import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Scanner;

public class ParticleEffect implements Writeable {

    @Override
    public void write(@NotNull BinaryWriter writer) {}
    public @Nullable ParticleEffect read(@Nullable Scanner data) {
        return this;
    }
    public @Nullable ParticleEffect read(@NotNull BinaryReader reader) {
        return this;
    }
}
