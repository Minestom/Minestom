package net.minestom.server.entity.ai.goal

import net.minestom.server.coordinate.Point
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
import net.minestom.server.entity.Entity
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.EntityProjectile
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.pathfinding.Navigator
import net.minestom.server.utils.time.TimeUnit
import java.time.Duration

/**
 * Attacks the entity's target ([EntityCreature.getTarget]) OR the closest entity
 * which can be targeted with the entity [TargetSelector].
 */
class MeleeAttackGoal
/**
 * @param entityCreature the entity to add the goal to
 * @param range          the allowed range the entity can attack others.
 * @param delay          the delay between each attacks
 */(entityCreature: EntityCreature, private val range: Double, private val delay: Duration) :
    GoalSelector(entityCreature) {
    val cooldown = Cooldown(Duration.of(5, TimeUnit.SERVER_TICK))
    private var lastHit: Long = 0
    private var stop = false
    private var cachedTarget: Entity? = null

    /**
     * @param entityCreature the entity to add the goal to
     * @param range          the allowed range the entity can attack others.
     * @param delay          the delay between each attacks
     * @param timeUnit       the unit of the delay
     */
    constructor(
        entityCreature: EntityCreature,
        range: Double,
        delay: Int,
        timeUnit: TemporalUnit
    ) : this(entityCreature, range, Duration.of(delay.toLong(), timeUnit)) {
    }

    override fun shouldStart(): Boolean {
        cachedTarget = findTarget()
        return cachedTarget != null
    }

    override fun start() {
        val targetPosition: Point = cachedTarget!!.position
        entityCreature.getNavigator().setPathTo(targetPosition)
    }

    override fun tick(time: Long) {
        val target: Entity?
        if (cachedTarget != null) {
            target = cachedTarget
            cachedTarget = null
        } else {
            target = findTarget()
        }
        stop = target == null
        if (!stop) {

            // Attack the target entity
            if (entityCreature.getDistance(target!!) <= range) {
                entityCreature.lookAt(target)
                if (!Cooldown.hasCooldown(time, lastHit, delay)) {
                    entityCreature.attack(target, true)
                    lastHit = time
                }
                return
            }

            // Move toward the target entity
            val navigator: Navigator = entityCreature.getNavigator()
            val pathPosition = navigator.pathPosition
            val targetPosition = target.position
            if (pathPosition == null || !pathPosition.samePoint(targetPosition)) {
                if (cooldown.isReady(time)) {
                    cooldown.refreshLastUpdate(time)
                    navigator.setPathTo(targetPosition)
                }
            }
        }
    }

    override fun shouldEnd(): Boolean {
        return stop
    }

    override fun end() {
        // Stop following the target
        entityCreature.getNavigator().setPathTo(null)
    }
}