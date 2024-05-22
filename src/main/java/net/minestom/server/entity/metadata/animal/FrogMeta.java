package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FrogMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    public FrogMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * Get the current {@link Variant} from a frog.
     * @return the given variant entry
     */
    public @NotNull Variant getVariant() {
        return super.metadata.getIndex(OFFSET, Variant.TEMPERATE);
    }

    /**
     * Set the variant for a frog.
     * @param value the value to set
     */
    public void setVariant(@NotNull Variant value) {
        super.metadata.setIndex(OFFSET, Metadata.FrogVariant(value));
    }

    /**
     * Get the current tongue target value.
     * @return the given value
     */
    public int getTongueTarget() {
        return super.metadata.getIndex(OFFSET + 1, 0);
    }

    /**
     * Set's the current tongue target back to the default value from the protocol.
     */
    public void resetTongueTarget() {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(0));
    }

    /**
     * Set the target value for a tongue.
     * @param value the target to set
     */
    public void setTongueTarget(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.OptVarInt(value));
    }

    /**
     * The enum contains all variants from a frog which are currently implemented in the game.
     */
    public enum Variant {
        TEMPERATE,
        WARM,
        COLD;

        public static final NetworkBuffer.Type<Variant> NETWORK_TYPE = NetworkBuffer.Enum(Variant.class);
    }
}
