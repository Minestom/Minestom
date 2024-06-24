package net.minestom.scratch.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EntityLogic {
    private final List<Goal> goals;

    private long lastTick;
    private long lastPathTick;
    private final Map<Class<?>, Long> cooldowns = new HashMap<>();

    public EntityLogic(List<Goal> goals) {
        this.goals = List.copyOf(goals);
    }

    public EntityLogic(Goal... goals) {
        this(List.of(goals));
    }

    public List<Action> process(boolean hasTarget) {
        final long tick = this.lastTick++;
        for (Goal goal : goals) {
            final int cooldown = goal.tickCooldown();
            final long lastTick = cooldowns.getOrDefault(goal.getClass(), (long) -cooldown);
            if (tick - lastTick < cooldown) {
                continue;
            }
            final List<Action> actions = switch (goal) {
                case Goal.ActiveTarget activeTarget when hasTarget -> List.of();
                case Goal.AttackTarget attackTarget when hasTarget -> List.of();
                case Goal.FleeTarget fleeTarget when hasTarget -> List.of();
                case Goal.LookAround lookAround -> {
                    final double n = Math.PI * 2 * Math.random();
                    final Vec direction = new Vec((float) Math.cos(n), 0, (float) Math.sin(n));
                    yield List.of(new Action.LookAt(direction));
                }
                case Goal.LookAtTarget lookAtTarget when hasTarget -> List.of();
                case Goal.Revenge revenge when hasTarget -> List.of();
                case Goal.Wander wander -> {
                    this.lastPathTick = tick;
                    var nearby = nearbyBlocks(wander.range);
                    var randomPoint = nearby.get((int) (Math.random() * nearby.size()));
                    yield List.of(new Action.SetPath(randomPoint));
                }
                default -> null;
            };
            if (actions != null) {
                cooldowns.put(goal.getClass(), tick);
                return actions;
            }
        }
        return List.of();
    }

    private static @NotNull List<Vec> nearbyBlocks(int radius) {
        List<Vec> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(new Vec(x, y, z));
                }
            }
        }
        return blocks;
    }

    public sealed interface Goal {
        int tickCooldown();

        record Wander(int range, int tickCooldown) implements Goal {
        }

        record Revenge(int tickCooldown) implements Goal {
        }

        record LookAround(int tickCooldown) implements Goal {
        }

        record ActiveTarget(EntityType type, int tickCooldown) implements Goal {
        }

        record LookAtTarget(int tickCooldown) implements Goal {
        }

        record AttackTarget(int tickCooldown) implements Goal {
        }

        record FleeTarget(int tickCooldown) implements Goal {
        }
    }

    public sealed interface Action {
        record LookAt(Point point) implements Action {
        }

        record SetPath(Point point) implements Action {
        }

        record SearchTarget(EntityType entityType) implements Action {
        }

        record Attack() implements Action {
        }
    }
}
