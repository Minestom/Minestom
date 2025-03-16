package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FoxMeta extends AnimalMeta {
    public FoxMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }


    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#FOX_VARIANT} instead.
     */
    public @NotNull FoxMeta.Variant getVariant() {
        return Variant.VALUES[metadata.get(MetadataDef.Fox.VARIANT)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#FOX_VARIANT} instead.
     */
    public void setVariant(@NotNull FoxMeta.Variant variant) {
        metadata.set(MetadataDef.Fox.VARIANT, variant.ordinal());
    }

    public boolean isSitting() {
        return metadata.get(MetadataDef.Fox.IS_SITTING);
    }

    public void setSitting(boolean value) {
        metadata.set(MetadataDef.Fox.IS_SITTING, value);
    }

    public boolean isFoxSneaking() {
        return metadata.get(MetadataDef.Fox.IS_CROUCHING);
    }

    public void setFoxSneaking(boolean value) {
        metadata.set(MetadataDef.Fox.IS_CROUCHING, value);
    }

    public boolean isInterested() {
        return metadata.get(MetadataDef.Fox.IS_INTERESTED);
    }

    public void setInterested(boolean value) {
        metadata.set(MetadataDef.Fox.IS_INTERESTED, value);
    }

    public boolean isPouncing() {
        return metadata.get(MetadataDef.Fox.IS_POUNCING);
    }

    public void setPouncing(boolean value) {
        metadata.set(MetadataDef.Fox.IS_POUNCING, value);
    }

    public boolean isSleeping() {
        return metadata.get(MetadataDef.Fox.IS_SLEEPING);
    }

    public void setSleeping(boolean value) {
        metadata.set(MetadataDef.Fox.IS_SLEEPING, value);
    }

    public boolean isFaceplanted() {
        return metadata.get(MetadataDef.Fox.IS_FACEPLANTED);
    }

    public void setFaceplanted(boolean value) {
        metadata.set(MetadataDef.Fox.IS_FACEPLANTED, value);
    }

    public boolean isDefending() {
        return metadata.get(MetadataDef.Fox.IS_DEFENDING);
    }

    public void setDefending(boolean value) {
        metadata.set(MetadataDef.Fox.IS_DEFENDING, value);
    }

    @Nullable
    public UUID getFirstUUID() {
        return metadata.get(MetadataDef.Fox.FIRST_UUID);
    }

    public void setFirstUUID(@Nullable UUID value) {
        metadata.set(MetadataDef.Fox.FIRST_UUID, value);
    }

    @Nullable
    public UUID getSecondUUID() {
        return metadata.get(MetadataDef.Fox.SECOND_UUID);
    }

    public void setSecondUUID(@Nullable UUID value) {
        metadata.set(MetadataDef.Fox.SECOND_UUID, value);
    }

    public enum Variant {
        RED,
        SNOW;

        public static final NetworkBuffer.Type<Variant> NETWORK_TYPE = NetworkBuffer.Enum(Variant.class);
        public static final BinaryTagSerializer<Variant> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(Variant.class);

        private final static Variant[] VALUES = values();
    }

}
