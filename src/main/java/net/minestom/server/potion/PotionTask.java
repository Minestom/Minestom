package net.minestom.server.potion;

import javafx.util.Pair;
import net.minestom.server.MinecraftServer;

public class PotionTask extends Pair<Potion, PotionTimeTask> {
    /**
     * Creates a new PotionTask
     *
     * @param potion The {@link Potion}
     * @param task   The {@link PotionTimeTask}
     */
    public PotionTask(Potion potion, PotionTimeTask task) {
        super(potion, task);
        task.potionTask = this;
    }

    public Potion getPotion() {
        return this.getKey();
    }

    public void removeEffect() {
        MinecraftServer.getPotionEffectManager().removeEffect(this);
    }
}
