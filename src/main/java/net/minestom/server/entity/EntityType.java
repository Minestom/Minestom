package net.minestom.server.entity;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.translation.Translatable;
import net.minestom.server.codec.Codec;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public sealed interface EntityType extends StaticProtocolObject<EntityType>, EntityTypes, Translatable
        permits EntityTypeImpl {
    NetworkBuffer.Type<EntityType> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(EntityType::fromId, EntityType::id);
    Codec<EntityType> CODEC = Codec.KEY.transform(EntityType::fromKey, EntityType::key);

    /**
     * Returns the entity registry.
     *
     * @return the entity registry
     * @deprecated use the direct accessors on {@link EntityType}
     */
    @Deprecated(forRemoval = true)
    @Contract(pure = true)
    RegistryData.EntityEntry registry();

    @Override
    default Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    /**
     * Returns the drag applied to this entity type.
     *
     * @return the drag
     */
    @Contract(pure = true)
    default double drag() {
        return registry().drag();
    }

    /**
     * Returns the base acceleration used by this entity type.
     *
     * @return the acceleration
     */
    @Contract(pure = true)
    default double acceleration() {
        return registry().acceleration();
    }

    /**
     * Returns the horizontal velocity multiplier applied while airborne.
     *
     * @return the horizontal air resistance
     */
    @Contract(pure = true)
    default double horizontalAirResistance() {
        return registry().horizontalAirResistance();
    }

    /**
     * Returns the vertical velocity multiplier applied while airborne.
     *
     * @return the vertical air resistance
     */
    @Contract(pure = true)
    default double verticalAirResistance() {
        return registry().verticalAirResistance();
    }

    /**
     * Returns whether entities of this type synchronize attributes with clients.
     *
     * @return {@code true} if attributes should be sent
     */
    @Contract(pure = true)
    default boolean shouldSendAttributes() {
        return registry().shouldSendAttributes();
    }

    /**
     * Returns the entity type's bounding-box width.
     *
     * @return the width
     */
    @Contract(pure = true)
    default double width() {
        return registry().width();
    }

    /**
     * Returns the entity type's bounding-box height.
     *
     * @return the height
     */
    @Contract(pure = true)
    default double height() {
        return registry().height();
    }

    /**
     * Returns the default eye height for this entity type.
     *
     * @return the eye height
     */
    @Contract(pure = true)
    default double eyeHeight() {
        return registry().eyeHeight();
    }

    /**
     * Returns whether this entity type is immune to fire damage.
     *
     * @return {@code true} if this entity type is fire immune
     */
    @Contract(pure = true)
    default boolean fireImmune() {
        return registry().fireImmune();
    }

    /**
     * Returns the client tracking range for this entity type.
     *
     * @return the client tracking range
     */
    @Contract(pure = true)
    default int clientTrackingRange() {
        return registry().clientTrackingRange();
    }

    /**
     * Returns all attachment offsets with the given name.
     *
     * @param attachmentName the attachment name
     * @return the immutable offsets, or {@code null} when absent
    */
    @Contract(pure = true)
    default @Unmodifiable @Nullable List<Vec> entityAttachments(String attachmentName) {
        final List<List<Double>> attachments = registry().entityAttachments(attachmentName);
        if (attachments == null) return null;
        return attachments.stream()
                .map(attachment -> new Vec(attachment.get(0), attachment.get(1), attachment.get(2)))
                .toList();
    }

    /**
     * Returns the default bounding box for this entity type.
     *
     * @return the bounding box
     */
    @Contract(pure = true)
    default BoundingBox boundingBox() {
        return registry().boundingBox();
    }

    /**
     * Returns the immutable default attribute values for this entity type.
     *
     * @return the default attribute values
     */
    @Contract(pure = true)
    default @Unmodifiable Map<Attribute, Double> defaultAttributes() {
        return registry().defaultAttributes();
    }

    @Override
    default String translationKey() {
        return registry().translationKey();
    }

    static Collection<EntityType> values() {
        return EntityTypeImpl.REGISTRY.values();
    }

    static @Nullable EntityType fromKey(@KeyPattern String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable EntityType fromKey(Key key) {
        return EntityTypeImpl.REGISTRY.get(key);
    }

    static @Nullable EntityType fromId(int id) {
        return EntityTypeImpl.REGISTRY.get(id);
    }

    static Registry<EntityType> staticRegistry() {
        return EntityTypeImpl.REGISTRY;
    }
}
