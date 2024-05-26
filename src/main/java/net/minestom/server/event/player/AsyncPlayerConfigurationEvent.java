package net.minestom.server.event.player;

import java.util.List;
import java.util.Set;

import net.kyori.adventure.key.Namespaced;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.featureflag.FeatureFlag;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player enters the configuration state (either on first connection, or if they are
 * sent back to configuration later). The player is moved to the play state as soon as all event
 * handles finish processing this event.
 *
 * <p>The spawning instance <b>must</b> be set for the player to join.</p>
 *
 * <p>The event is called off the tick threads, so it is safe to block here</p>
 */
public class AsyncPlayerConfigurationEvent implements PlayerEvent {
    private final Player player;
    private final boolean isFirstConfig;

    private boolean hardcore;
    private boolean sendRegistryData;
    private Instance spawningInstance;
    private Set<FeatureFlag> enabledFeatures;

    public AsyncPlayerConfigurationEvent(@NotNull Player player, boolean isFirstConfig, @NotNull Set<FeatureFlag> enabledFeatures) {
        this.player = player;
        this.isFirstConfig = isFirstConfig;
        this.enabledFeatures = enabledFeatures;

        this.hardcore = false;
        this.sendRegistryData = isFirstConfig;
        this.spawningInstance = null;
    }

    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }

    /**
     * Returns true if this is the first time the player is in the configuration phase (they are joining), false otherwise.
     */
    public boolean isFirstConfig() {
        return isFirstConfig;
    }

    public boolean isHardcore() {
        return this.hardcore;
    }

    public void setHardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }

    public boolean willSendRegistryData() {
        return sendRegistryData;
    }

    public void setSendRegistryData(boolean sendRegistryData) {
        this.sendRegistryData = sendRegistryData;
    }

    public @Nullable Instance getSpawningInstance() {
        return spawningInstance;
    }

    public void setSpawningInstance(@Nullable Instance spawningInstance) {
        this.spawningInstance = spawningInstance;
    }

    public void setEnabledFeatures(Set<FeatureFlag> enabledFeatures) {
        this.enabledFeatures = enabledFeatures;
    }

    public Set<FeatureFlag> getEnabledFeatures() {
        return enabledFeatures;
    }
}
