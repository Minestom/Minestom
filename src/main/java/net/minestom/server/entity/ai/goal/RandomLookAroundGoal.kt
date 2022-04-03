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
import java.util.function.Function
import java.util.function.Supplier

class RandomLookAroundGoal
/**
 * @param entityCreature          Creature that should randomly look around.
 * @param chancePerTick           The chance (per tick) that the entity looks around. Setting this to N would mean there is a 1 in N chance.
 * @param minimalLookTimeSupplier A supplier that returns the minimal amount of time an entity looks in a direction.
 * @param randomDirectionFunction A function that returns a random vector that the entity will look in/at.
 */ @JvmOverloads constructor(
    entityCreature: EntityCreature?,
    private val chancePerTick: Int,
    private val minimalLookTimeSupplier: Supplier<Int> =  // These two functions act similarly enough to how MC randomly looks around.
    // Look in one direction for at most 40 ticks and at minimum 20 ticks.
        Supplier { 20 + RANDOM.nextInt(20) },
    private val randomDirectionFunction: Function<EntityCreature?, Vec> =  // Look at a random block
        Function { creature: EntityCreature? ->
            val n = Math.PI * 2 * RANDOM.nextDouble()
            Vec(
                Math.cos(n).toFloat().toDouble(),
                0, Math.sin(n).toFloat().toDouble()
            )
        }
) : GoalSelector(entityCreature!!) {
    private var lookDirection: Vec? = null
    private var lookTime = 0
    override fun shouldStart(): Boolean {
        return if (RANDOM.nextInt(chancePerTick) != 0) {
            false
        } else entityCreature.getNavigator().pathPosition == null
    }

    override fun start() {
        lookTime = minimalLookTimeSupplier.get()
        lookDirection = randomDirectionFunction.apply(entityCreature)
    }

    override fun tick(time: Long) {
        --lookTime
        entityCreature.refreshPosition(entityCreature.position.withDirection(lookDirection!!))
    }

    override fun shouldEnd(): Boolean {
        return lookTime < 0
    }

    override fun end() {}

    companion object {
        private val RANDOM = Random()
    }
}