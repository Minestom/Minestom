package net.minestom.server.event.player;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minestom.server.FeatureFlag;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.event.trait.mutation.EventMutator;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.configuration.ResetChatPacket;
import net.minestom.server.network.packet.server.configuration.UpdateEnabledFeaturesPacket;
import org.jetbrains.annotations.Contract;
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
 *
 * <p>It is valid to kick a player using {@link Player#kick(net.kyori.adventure.text.Component)} during this event.</p>
 */
public record AsyncPlayerConfigurationEvent(@NotNull Player player, boolean firstConfig,
                                            ObjectSet<FeatureFlag> featureFlags, boolean hardcore,
                                            boolean clearChat, boolean sendRegistryData,
                                            @Nullable Instance spawningInstance) implements PlayerEvent, MutableEvent<AsyncPlayerConfigurationEvent> {

    public AsyncPlayerConfigurationEvent(@NotNull Player player, boolean isFirstConfig) {
        // Vanilla feature-set, without this you get nothing at all. Kinda wacky!
        this(player, isFirstConfig, ObjectArraySet.of(FeatureFlag.VANILLA), false, false, isFirstConfig, null);
    }

    /**
     * Returns true if this is the first time the player is in the configuration phase (they are joining), false otherwise.
     */
    @Override
    public boolean firstConfig() {
        return firstConfig;
    }

    /**
     * The list of currently added feature flags. This is an unmodifiable copy of what will be sent to the client.
     *
     * @return An unmodifiable set of feature flags
     *
     * @see UpdateEnabledFeaturesPacket
     * @see net.minestom.server.FeatureFlag
     */
    @Override
    public ObjectSet<FeatureFlag> featureFlags() {
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
    @Override
    public boolean clearChat() {
        return clearChat;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutator<AsyncPlayerConfigurationEvent> {
        private final Player player;
        private final boolean isFirstConfig;

        private final ObjectSet<FeatureFlag> featureFlags;
        private boolean hardcore;
        private boolean clearChat;
        private boolean sendRegistryData;
        private Instance spawningInstance;

        public Mutator(AsyncPlayerConfigurationEvent event) {
            this.player = event.player;
            this.isFirstConfig = event.firstConfig;

            this.featureFlags = new ObjectArraySet<>(event.featureFlags); // Copy to ensure immutability.
            this.hardcore = event.hardcore;
            this.clearChat = event.clearChat;
            this.sendRegistryData = event.sendRegistryData;
            this.spawningInstance = event.spawningInstance;
        }

        public boolean isHardcore() {
            return this.hardcore;
        }

        public void setHardcore(boolean hardcore) {
            this.hardcore = hardcore;
        }

        /**
         * Add a feature flag, see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Feature_Flags">Minecraft Wiki's Feature Flags</a> for a list of applicable features
         * Note: the flag "minecraft:vanilla" is already included by default.
         *
         * @param feature A minecraft feature flag
         * @see UpdateEnabledFeaturesPacket
         * @see net.minestom.server.FeatureFlag
         */
        public void addFeatureFlag(@NotNull FeatureFlag feature) {
            this.featureFlags.add(feature);
        }

        /**
         * Remove a feature flag, see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Feature_Flags">Minecraft Wiki's Feature Flags</a> for a list of applicable features
         * Note: removing the flag "minecraft:vanilla" may result in weird behavior
         *
         * @param feature A minecraft feature flag
         * @return if the feature specified existed prior to being removed
         * @see UpdateEnabledFeaturesPacket
         * @see net.minestom.server.FeatureFlag
         */
        public boolean removeFeatureFlag(@NotNull FeatureFlag feature) {
            return this.featureFlags.remove(feature); // Should this have sanity checking to see if the feature was actually contained in the list?
        }

        /**
         * The list of currently added feature flags. This is an unmodifiable copy of what will be sent to the client.
         *
         * @return An unmodifiable set of feature flags
         * @see UpdateEnabledFeaturesPacket
         * @see net.minestom.server.FeatureFlag
         */
        public @NotNull Set<FeatureFlag> getFeatureFlags() {
            return ObjectSets.unmodifiable(this.featureFlags);
        }

        /**
         * If true, the player's chat will be cleared when exiting the configuration state, otherwise
         * it will be preserved. The default is not to clear the chat.
         *
         * @return true if the chat will be cleared, false otherwise
         * @see ResetChatPacket
         */
        public boolean willClearChat() {
            return clearChat;
        }

        /**
         * Set whether the player's chat will be cleared when exiting the configuration state.
         *
         * @param clearChat true to clear the chat, false otherwise
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

        @Contract(pure = true)
        @Override
        public @NotNull AsyncPlayerConfigurationEvent mutated() {
            return new AsyncPlayerConfigurationEvent(this.player, this.isFirstConfig, this.featureFlags, this.hardcore, this.clearChat, this.sendRegistryData, this.spawningInstance);
        }
    }
}
