package net.minestom.server.particle.data;

import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Scanner;

public class ParticleEffect {

    public void write(@NotNull BinaryWriter writer) {}
    public @Nullable ParticleEffect read(@Nullable Scanner data) {
        return this;
    }
}
