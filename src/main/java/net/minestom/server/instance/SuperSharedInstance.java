package net.minestom.server.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * SuperSharedInstance is an instance that acts exactly like a {@link SharedInstance} in that chunks are shared and entities
 * are separated. However, entities within the underlying {@link InstanceContainer} will be shown in this instance.
 */
public class SuperSharedInstance extends SharedInstance {
    public SuperSharedInstance(@NotNull UUID uniqueId, @NotNull InstanceContainer instanceContainer) {
        super(uniqueId, instanceContainer);
        // register entities already inside of the instanceContainer
        EntityTracker entityTracker = getEntityTracker();
        for (Entity e : instanceContainer.getEntities()) {
            entityTracker.register(e, e.getPosition(), e.getTrackingTarget(), e.getTrackingUpdate());
        }
    }

    @Override
    public @NotNull Set<@NotNull Player> getPlayers() {
        Set<Player> allPlayers = super.getPlayers();
        Set<Player> playersHere = new HashSet<>();
        for (Player p : allPlayers) {
            if (p.getInstance().equals(this)) playersHere.add(p);
        }
        return Collections.unmodifiableSet(playersHere);
    }
}
