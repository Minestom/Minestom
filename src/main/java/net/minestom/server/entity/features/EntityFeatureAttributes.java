package net.minestom.server.entity.features;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.play.EntityPropertiesPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityFeatureAttributes extends EntityFeatureBase {

    private final Map<String, AttributeInstance> attributeModifiers = new ConcurrentHashMap<>(Attribute.values().length);

    public EntityFeatureAttributes(Entity entity) {
        super(entity);
    }

    @Override
    public void onAddViewer(Player player) {
        player.getPlayerConnection().sendPacket(getPropertiesPacket());
    }

    /**
     * Retrieves the attribute instance and its modifiers.
     *
     * @param attribute the attribute instance to get
     * @return the attribute instance
     */
    @NotNull
    public AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return attributeModifiers.computeIfAbsent(attribute.getKey(),
                s -> new AttributeInstance(attribute, this::onAttributeChanged));
    }

    /**
     * Retrieves the attribute value.
     *
     * @param attribute the attribute value to get
     * @return the attribute value
     */
    public float getAttributeValue(@NotNull Attribute attribute) {
        AttributeInstance instance = attributeModifiers.get(attribute.getKey());
        return (instance != null) ? instance.getValue() : attribute.getDefaultValue();
    }

    /**
     * Callback used when an attribute instance has been modified.
     *
     * @param attributeInstance the modified attribute instance
     */
    protected void onAttributeChanged(@NotNull AttributeInstance attributeInstance) {
        if (attributeInstance.getAttribute().isShared()) {
            boolean self = false;
            if (entity instanceof Player) {
                Player player = (Player) entity;
                PlayerConnection playerConnection = player.getPlayerConnection();
                // connection null during Player initialization (due to #super call)
                //noinspection ConstantConditions
                self = playerConnection != null && playerConnection.getConnectionState() == ConnectionState.PLAY;
            }
            EntityPropertiesPacket propertiesPacket = getPropertiesPacket(Collections.singleton(attributeInstance));
            if (self) {
                entity.sendPacketToViewersAndSelf(propertiesPacket);
            } else {
                entity.sendPacketToViewers(propertiesPacket);
            }
        }
    }

    /**
     * Gets an {@link EntityPropertiesPacket} for this entity with all of its attributes values.
     *
     * @return an {@link EntityPropertiesPacket} linked to this entity
     */
    @NotNull
    public EntityPropertiesPacket getPropertiesPacket() {
        return getPropertiesPacket(attributeModifiers.values());
    }

    /**
     * Gets an {@link EntityPropertiesPacket} for this entity with the specified attribute values.
     *
     * @param attributes the attributes to include in the packet
     * @return an {@link EntityPropertiesPacket} linked to this entity
     */
    @NotNull
    public EntityPropertiesPacket getPropertiesPacket(@NotNull Collection<AttributeInstance> attributes) {
        // Get all the attributes which should be sent to the client
        final AttributeInstance[] instances = attributes.stream()
                .filter(i -> i.getAttribute().isShared())
                .toArray(AttributeInstance[]::new);


        EntityPropertiesPacket propertiesPacket = new EntityPropertiesPacket();
        propertiesPacket.entityId = entity.getEntityId();

        EntityPropertiesPacket.Property[] properties = new EntityPropertiesPacket.Property[instances.length];
        for (int i = 0; i < properties.length; ++i) {
            EntityPropertiesPacket.Property property = new EntityPropertiesPacket.Property();

            final float value = instances[i].getBaseValue();

            property.instance = instances[i];
            property.attribute = instances[i].getAttribute();
            property.value = value;

            properties[i] = property;
        }

        propertiesPacket.properties = properties;
        return propertiesPacket;
    }
}
