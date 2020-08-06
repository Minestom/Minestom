package net.minestom.server.entity.ai.target;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.instance.Instance;

import java.util.Set;

public class PlayerTarget extends TargetSelector {

    private float range;

    public PlayerTarget(EntityCreature entityCreature, float range) {
        super(entityCreature);
        this.range = range;
    }

    @Override
    public Entity findTarget() {
        final Instance instance = getEntityCreature().getInstance();
        final Set<Player> players = instance.getPlayers();

        Player player = null;
        {
            float distance = Float.MAX_VALUE;
            for (Player p : players) {
                final float d = getEntityCreature().getDistance(p);
                if ((player == null || d < distance) && d < range) {
                    player = p;
                    distance = d;
                    continue;
                }
            }
        }

        return player;
    }
}
