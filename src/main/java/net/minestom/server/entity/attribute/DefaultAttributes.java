package net.minestom.server.entity.attribute;

import net.minestom.server.entity.EntityType;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryData.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads and exposes vanilla default attribute values for each entity type.
 */
public final class DefaultAttributes {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAttributes.class);
    private static final Map<EntityType, Map<Attribute, Double>> DEFAULTS;

    static {
        Map<EntityType, Map<Attribute, Double>> defaults = new HashMap<>();

        try {
            Properties properties = RegistryData.load("default_attributes.json", true);

            for (var entry : properties) {
                String entityKey = entry.getKey();
                EntityType entityType;

                try {
                    entityType = EntityType.fromKey(entityKey);
                } catch (Exception e) {
                    LOGGER.warn("Invalid entity namespace '{}' in default attributes", entityKey, e);
                    continue;
                }

                if (entityType == null) {
                    LOGGER.warn("Unknown entity type '{}' in default attributes", entityKey);
                    continue;
                }

                Properties attributeSection = properties.section(entityKey);

                if (attributeSection == null || attributeSection.size() == 0) {
                    continue;
                }

                Map<Attribute, Double> attributes = new HashMap<>();

                for (var attributeEntry : attributeSection) {
                    String attributeKey = attributeEntry.getKey();
                    Attribute attribute;

                    try {
                        attribute = Attribute.fromKey(attributeKey);
                    } catch (Exception e) {
                        LOGGER.debug("Invalid attribute namespace '{}' for entity '{}'", attributeKey, entityKey, e);
                        continue;
                    }

                    if (attribute == null) {
                        LOGGER.debug("Unknown attribute '{}' for entity '{}'", attributeKey, entityKey);
                        continue;
                    }

                    Object value = attributeEntry.getValue();

                    if (!(value instanceof Number number)) {
                        LOGGER.debug("Skipping attribute '{}' for entity '{}' due to non-numeric value {}", attributeKey, entityKey, value);
                        continue;
                    }

                    attributes.put(attribute, number.doubleValue());
                }

                if (!attributes.isEmpty()) {
                    defaults.put(entityType, Map.copyOf(attributes));
                }
            }

            LOGGER.debug("Loaded default attributes for {} entity types", defaults.size());
        } catch (Exception exception) {
            LOGGER.error("Failed to load default entity attributes", exception);
        }
        DEFAULTS = defaults.isEmpty() ? Map.of() : Map.copyOf(defaults);
    }

    private DefaultAttributes() {}

    public static Map<Attribute, Double> getDefaults(EntityType entityType) {
        return DEFAULTS.getOrDefault(entityType, Map.of());
    }

    public static boolean hasDefaults(EntityType entityType) {
        return DEFAULTS.containsKey(entityType);
    }
}
