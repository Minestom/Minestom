package net.minestom.server.particle;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class VibrationParticle extends ParticleImpl {
    private final SourceType sourceType;
    private final Point sourceBlockPosition;
    private final int sourceEntityId;
    private final float sourceEntityEyeHeight;
    private final int travelTicks;

    VibrationParticle(@NotNull NamespaceID namespace, int id, @NotNull SourceType sourceType, @Nullable Point sourceBlockPosition,
                      int sourceEntityId, float sourceEntityEyeHeight, int travelTicks) {
        super(namespace, id);
        this.sourceType = sourceType;
        this.sourceBlockPosition = sourceBlockPosition;
        this.sourceEntityId = sourceEntityId;
        this.sourceEntityEyeHeight = sourceEntityEyeHeight;
        this.travelTicks = travelTicks;
    }

    @Contract(pure = true)
    public @NotNull VibrationParticle withProperties(@NotNull SourceType sourceType, @Nullable Point sourceBlockPosition,
                                                     int sourceEntityId, float sourceEntityEyeHeight, int travelTicks) {
        return new VibrationParticle(namespace(), id(), sourceType, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
    }

    @Contract(pure = true)
    public @NotNull VibrationParticle withSourceBlockPosition(@Nullable Point sourceBlockPosition, int travelTicks) {
        return new VibrationParticle(namespace(), id(), SourceType.BLOCK, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
    }

    @Contract(pure = true)
    public @NotNull VibrationParticle withSourceEntity(int sourceEntityId, float sourceEntityEyeHeight, int travelTicks) {
        return new VibrationParticle(namespace(), id(), SourceType.ENTITY, sourceBlockPosition, sourceEntityId, sourceEntityEyeHeight, travelTicks);
    }

    public @NotNull SourceType sourceType() {
        return sourceType;
    }

    public @Nullable Point sourceBlockPosition() {
        return sourceBlockPosition;
    }

    public int sourceEntityId() {
        return sourceEntityId;
    }

    public float sourceEntityEyeHeight() {
        return sourceEntityEyeHeight;
    }

    public int travelTick() {
        return travelTicks;
    }

    @Override
    public @NotNull VibrationParticle readData(@NotNull NetworkBuffer reader) {
        SourceType type = reader.readEnum(SourceType.class);
        if (type == SourceType.BLOCK) {
            return this.withSourceBlockPosition(reader.read(NetworkBuffer.BLOCK_POSITION), reader.read(NetworkBuffer.VAR_INT));
        } else {
            return this.withSourceEntity(reader.read(NetworkBuffer.VAR_INT), reader.read(NetworkBuffer.FLOAT), reader.read(NetworkBuffer.VAR_INT));
        }
    }

    @Override
    public void writeData(@NotNull NetworkBuffer writer) {
        writer.writeEnum(SourceType.class, sourceType);
        if (sourceType == SourceType.BLOCK) {
            Objects.requireNonNull(sourceBlockPosition);
            writer.write(NetworkBuffer.BLOCK_POSITION, sourceBlockPosition);
            writer.write(NetworkBuffer.VAR_INT, travelTicks);
        } else {
            writer.write(NetworkBuffer.VAR_INT, sourceEntityId);
            writer.write(NetworkBuffer.FLOAT, sourceEntityEyeHeight);
            writer.write(NetworkBuffer.VAR_INT, travelTicks);
        }

    }

    public enum SourceType {
        BLOCK, ENTITY
    }
}
