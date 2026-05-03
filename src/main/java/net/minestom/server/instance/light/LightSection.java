package net.minestom.server.instance.light;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface LightSection<T extends LightSection<T, Type, ChunkData>, Type extends LightSectionType<T, ChunkData>, ChunkData> {
    byte[] getBlockLight();

    byte[] getSkyLight();

    int getBlockLight(int relativeX, int relativeY, int relativeZ);

    int getSkyLight(int relativeX, int relativeY, int relativeZ);

    void blockChanged(int relativeX, int relativeY, int relativeZ);

    boolean getAndResetResendBlockLight();

    boolean getAndResetResendSkyLight();

    void copyFrom(T section);

    void neighborLoadUnloadDetected();

    void initAboveBelow(@Nullable T above, @Nullable T below);
}
