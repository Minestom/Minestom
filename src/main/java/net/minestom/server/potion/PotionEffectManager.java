package net.minestom.server.potion;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Manages active potion effects on players
 */
public class PotionEffectManager {

    private HashMap<UUID, ArrayList<PotionTask>> playerEffects;
    private boolean persistEffects = false;

    /**
     * Creates a new PotionEffectManager
     */
    public PotionEffectManager() {
        playerEffects = new HashMap<>();

        addEventHandlers();
    }

    /**
     * Returns the active effects of a player.
     *
     * @param player The player to get the effects of.
     * @return Null if no active effects, otherwise the active effects.
     */
    public @Nullable List<PotionTask> getActiveEffects(@Nullable Player player) {
        if (player == null) return null;
        return playerEffects.get(player.getUuid());
    }

    /**
     * Returns a {@link PotionTask} on a player of a type {@link PotionEffect}
     *
     * @param player The player with the effect.
     * @param potionEffect The type of potion to look for.
     * @return Null if the player does not have the effect, otherwise the effect.
     */
    public @Nullable PotionTask getPotionTask(@Nullable Player player, @Nullable PotionEffect potionEffect) {
        if (player == null) return null;
        if (potionEffect == null) return null;

        ArrayList<PotionTask> potionTasks = playerEffects.get(player.getUuid());
        if (potionTasks == null) return null;

        for (PotionTask potionTask : potionTasks) {
            if (potionTask.getPotion().effect == potionEffect) {
                return potionTask;
            }
        }
        return null;
    }

    /**
     * Returns if the player has an effect of specified type.
     *
     * @param player The player to check.
     * @param potionEffect The type of potion to check for.
     * @return If the player has a potion effect with the specified type.
     */
    public boolean hasPotionEffect(@Nullable Player player, @Nullable PotionEffect potionEffect) {
        return getPotionTask(player, potionEffect) != null;
    }

    /**
     * Removes the {@link PotionTask}.
     *
     * @param task The {@link PotionTask} to remove.
     */
    public void removeEffect(@NotNull PotionTask task) {
        task.getValue().task.cancel();
        for (UUID uuid : playerEffects.keySet()) {
            if (playerEffects.get(uuid).contains(task)) {
                playerEffects.get(uuid).remove(task);
                Player player = MinecraftServer.getConnectionManager().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    task.getPotion().sendRemovePacket(player);
                }
                return;
            }
        }
    }

    /**
     * If the player has a potion of type effect, remove it.
     *
     * @param player The player to remove from.
     * @param effect The {@link PotionEffect} to remove.
     */
    public void removeEffect(@NotNull Player player, @NotNull PotionEffect effect) {
        PotionTask task = null;
        ArrayList<PotionTask> potionTasks = playerEffects.get(player.getUuid());
        if (potionTasks == null) return;
        for (PotionTask potionTask : potionTasks) {
            if (potionTask.getPotion().effect == effect) {
                task = potionTask;
                break;
            }
        }
        if (task == null) return;
        removeEffect(task);
    }

    /**
     * Clears a player's effects.
     *
     * @param player The player to clear.
     */
    public void clearEffects(@Nullable Player player) {
        if (player == null) return;

        ArrayList<PotionTask> potionTasks = playerEffects.get(player.getUuid());
        if (potionTasks == null) return;
        for (PotionTask potionTask : potionTasks) {
            potionTask.getValue().task.cancel();
            if (player.isOnline()) {
                potionTask.getPotion().sendRemovePacket(player);
            }
        }
        potionTasks.clear();
        playerEffects.remove(player.getUuid());
    }

    /**
     * Gives a player a potion effect.
     *
     * @param player The player to give to.
     * @param potion The {@link Potion} to give.
     */
    public void addPotion(@Nullable Player player, @Nullable Potion potion) {
        if (player == null) return;
        if (potion == null) return;
        removeEffect(player, potion.effect);
        ArrayList<PotionTask> potionTasks = playerEffects.computeIfAbsent(player.getUuid(), k -> new ArrayList<>());
        SchedulerManager schedulerManager = MinecraftServer.getSchedulerManager();
        PotionTimeTask ptt = new PotionTimeTask(MinecraftServer.getSchedulerManager(),
                TimeUnit.TICK.toMilliseconds(potion.duration));
        potionTasks.add(new PotionTask(potion, ptt));
        potion.sendAddPacket(player);
    }

//    NOT IMPLEMENTED
//    public void setPersistEffects(Boolean persistEffects) {
//        this.persistEffects = persistEffects;
//    }
//
//    public boolean getPersistEffects() {
//        return persistEffects;
//    }

    /**
     * Adds a {@link PlayerSpawnEvent} and {@link PlayerDisconnectEvent} handler to pause and resume potion effects.
     */
    private void addEventHandlers() {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addEventCallback(PlayerSpawnEvent.class, playerSpawnEvent -> {
            if (persistEffects) {
                // TODO: Implement persist
            }
        });

        globalEventHandler.addEventCallback(PlayerDisconnectEvent.class, playerDisconnectEvent -> {
            if (persistEffects) {
                // TODO: Implement persist
            } else {
                clearEffects(playerDisconnectEvent.getPlayer());
            }
        });
    }

}
