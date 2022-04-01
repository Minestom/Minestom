package net.minestom.server.entity.ai.goal

import net.minestom.server.entity.pathfinding.Navigator.setPathTo
import net.minestom.server.entity.pathfinding.Navigator.pathPosition
import net.minestom.server.utils.time.Cooldown.isReady
import net.minestom.server.utils.time.Cooldown.refreshLastUpdate
import net.minestom.server.utils.validate.Check.argCondition
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.goal.DoNothingGoal
import net.minestom.server.utils.time.Cooldown
import java.time.temporal.TemporalUnit
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.EntityProjectile
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.utils.MathUtils
import java.util.*

class DoNothingGoal(entityCreature: EntityCreature?, private val time: Long, chance: Float) : GoalSelector(
    entityCreature!!
) {
    private val chance: Float
    private var startTime: Long = 0

    /**
     * Create a DoNothing goal
     *
     * @param entityCreature the entity
     * @param time           the time in milliseconds where nothing happen
     * @param chance         the chance to do nothing (0-1)
     */
    init {
        this.chance = MathUtils.clamp(chance, 0f, 1f)
    }

    override fun end() {
        startTime = 0
    }

    override fun shouldEnd(): Boolean {
        return System.currentTimeMillis() - startTime >= time
    }

    override fun shouldStart(): Boolean {
        return RANDOM.nextFloat() <= chance
    }

    override fun start() {
        startTime = System.currentTimeMillis()
    }

    override fun tick(time: Long) {}

    companion object {
        private val RANDOM = Random()
    }
}