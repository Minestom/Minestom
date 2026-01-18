package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;

public final class SnifferMeta extends AnimalMeta {
    public SnifferMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public State getState() {
        return get(MetadataDef.Sniffer.STATE);
    }

    public void setState(State value) {
        set(MetadataDef.Sniffer.STATE, value);
    }

    public int getDropSeedAtTick() {
        return get(MetadataDef.Sniffer.DROP_SEED_AT_TICK);
    }

    public void setDropSeedAtTick(int value) {
        set(MetadataDef.Sniffer.DROP_SEED_AT_TICK, value);
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
