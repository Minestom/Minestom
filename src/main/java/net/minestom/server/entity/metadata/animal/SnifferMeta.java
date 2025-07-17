package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public class SnifferMeta extends AnimalMeta {
    public SnifferMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull State getState() {
        return metadata.get(MetadataDef.Sniffer.STATE);
    }

    public void setState(@NotNull State value) {
        metadata.set(MetadataDef.Sniffer.STATE, value);
    }

    public int getDropSeedAtTick() {
        return metadata.get(MetadataDef.Sniffer.DROP_SEED_AT_TICK);
    }

    public void setDropSeedAtTick(int value) {
        metadata.set(MetadataDef.Sniffer.DROP_SEED_AT_TICK, value);
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
