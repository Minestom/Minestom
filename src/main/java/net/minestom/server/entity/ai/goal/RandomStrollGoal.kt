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
import java.util.*

class RandomStrollGoal(entityCreature: EntityCreature, val radius: Int) : GoalSelector(entityCreature) {
    private val closePositions: List<Vec>
    private val random = Random()
    private var lastStroll: Long = 0

    init {
        closePositions = getNearbyBlocks(radius)
    }

    override fun shouldStart(): Boolean {
        return System.currentTimeMillis() - lastStroll >= DELAY
    }

    override fun start() {
        var remainingAttempt = closePositions.size
        while (remainingAttempt-- > 0) {
            val index = random.nextInt(closePositions.size)
            val position = closePositions[index]
            val target = entityCreature.position.add(position)
            val result: Boolean = entityCreature.getNavigator().setPathTo(target)
            if (result) {
                break
            }
        }
    }

    override fun tick(time: Long) {}
    override fun shouldEnd(): Boolean {
        return true
    }

    override fun end() {
        lastStroll = System.currentTimeMillis()
    }

    companion object {
        private const val DELAY: Long = 2500
        private fun getNearbyBlocks(radius: Int): List<Vec> {
            val blocks: MutableList<Vec> = ArrayList()
            for (x in -radius..radius) {
                for (y in -radius..radius) {
                    for (z in -radius..radius) {
                        blocks.add(Vec(x.toDouble(), y.toDouble(), z.toDouble()))
                    }
                }
            }
            return blocks
        }
    }
}