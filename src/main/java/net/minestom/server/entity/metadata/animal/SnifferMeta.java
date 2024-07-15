package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public class SnifferMeta extends AnimalMeta {

    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    public SnifferMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull State getState() {
        return super.metadata.getIndex(OFFSET, State.IDLING);
    }

    public void setState(@NotNull State value) {
        super.metadata.setIndex(OFFSET, Metadata.SnifferState(value));
    }

    public int getDropSeedAtTick() {
        return super.metadata.getIndex(OFFSET + 1, 0);
    }

    public void setDropSeedAtTick(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public enum State {
        IDLING,
        FEELING_HAPPY,
        SCENTING,
        SNIFFING,
        SEARCHING,
        DIGGING,
        RISING;

        public static final NetworkBuffer.Type<State> NETWORK_TYPE = NetworkBuffer.Enum(State.class);
    }
}
