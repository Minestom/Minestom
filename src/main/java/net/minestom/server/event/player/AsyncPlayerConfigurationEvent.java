package net.minestom.server.event.player;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.configuration.ResetChatPacket;
import net.minestom.server.network.packet.server.configuration.UpdateEnabledFeaturesPacket;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

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

    private final ObjectArraySet<NamespaceID> featureFlags = new ObjectArraySet<>();
    private boolean hardcore;
    private boolean clearChat;
    private boolean sendRegistryData;
    private Instance spawningInstance;

    public AsyncPlayerConfigurationEvent(@NotNull Player player, boolean isFirstConfig) {
        this.player = player;
        this.isFirstConfig = isFirstConfig;

        this.featureFlags.add(NamespaceID.from("minecraft:vanilla")); // Vanilla feature-set, without this you get nothing at all. Kinda wacky!

        this.hardcore = false;
        this.clearChat = false;
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

    /**
     * Add a feature flag, see <a href="https://wiki.vg/Protocol#Feature_Flags">Wiki.vg Feature Flags</a> for a list of applicable features
     * Note: the flag "minecraft:vanilla" is already included by default.
     *
     * @param feature A minecraft feature flag
     *
     * @see UpdateEnabledFeaturesPacket
     */
    public void addFeatureFlag(@NotNull NamespaceID feature) {
        this.featureFlags.add(feature);
    }

    /**
     * Remove a feature flag, see <a href="https://wiki.vg/Protocol#Feature_Flags">Wiki.vg Feature Flags</a> for a list of applicable features
     * Note: removing the flag "minecraft:vanilla" may result in weird behavior
     *
     * @param feature A minecraft feature flag
     * @return if the feature specified existed prior to being removed
     *
     * @see UpdateEnabledFeaturesPacket
     */
    public boolean removeFeatureFlag(@NotNull NamespaceID feature) {
        return this.featureFlags.remove(feature); // Should this have sanity checking to see if the feature was actually contained in the list?
    }

    /**
     * The list of currently added feature flags. This is an unmodifiable copy of what will be sent to the client.
     *
     * @return An unmodifiable set of feature flags
     *
     * @see UpdateEnabledFeaturesPacket
     */
    public @NotNull Set<NamespaceID> getFeatureFlags() {
        return ObjectSets.unmodifiable(this.featureFlags);
    }

    /**
     * If true, the player's chat will be cleared when exiting the configuration state, otherwise
     * it will be preserved. The default is not to clear the chat.
     *
     * @return true if the chat will be cleared, false otherwise
     *
     * @see ResetChatPacket
     */
    public boolean willClearChat() {
        return clearChat;
    }

    /**
     * Set whether the player's chat will be cleared when exiting the configuration state.
     *
     * @param clearChat true to clear the chat, false otherwise
     *
     * @see ResetChatPacket
     */
    public void setClearChat(boolean clearChat) {
        this.clearChat = clearChat;
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
}
