package net.minestom.server.particle;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
final class ParticleLoader {

    // Maps do not need to be thread-safe as they are fully populated
    // in the static initializer, should not be modified during runtime

    // Block namespace -> registry data
    private static final Map<String, Particle> NAMESPACE_MAP = new HashMap<>();
    // Block id -> registry data
    private static final Int2ObjectMap<Particle> ID_MAP = new Int2ObjectOpenHashMap<>();

    static Particle get(@NotNull String namespace) {
        if (namespace.indexOf(':') == -1) {
            // Default to minecraft namespace
            namespace = "minecraft:" + namespace;
        }
        return NAMESPACE_MAP.get(namespace);
    }

    static Particle getId(int id) {
        return ID_MAP.get(id);
    }

    static Collection<Particle> values() {
        return Collections.unmodifiableCollection(NAMESPACE_MAP.values());
    }

    static {
        // Load data from file
        JsonObject particles = Registry.load(Registry.Resource.PARTICLES);
        particles.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject object = entry.getValue().getAsJsonObject();
            final int id = object.get("id").getAsInt();

            final var particle = new ParticleImpl(NamespaceID.from(namespace), id);
            ID_MAP.put(id, particle);
            NAMESPACE_MAP.put(namespace, particle);
        });
    }
}
