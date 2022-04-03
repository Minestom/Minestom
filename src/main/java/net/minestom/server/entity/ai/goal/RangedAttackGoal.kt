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
import net.minestom.server.entity.Entity
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.EntityProjectile
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.pathfinding.Navigator
import net.minestom.server.utils.time.TimeUnit
import java.time.Duration
import java.util.function.Function

class RangedAttackGoal(
    entityCreature: EntityCreature,
    private val delay: Duration,
    attackRange: Int,
    desirableRange: Int,
    comeClose: Boolean,
    power: Double,
    spread: Double
) : GoalSelector(entityCreature) {
    val cooldown = Cooldown(Duration.of(5, TimeUnit.SERVER_TICK))
    private var lastShot: Long = 0
    private val attackRangeSquared: Int
    private val desirableRangeSquared: Int
    private val comeClose: Boolean
    private val power: Double
    private val spread: Double
    private var projectileGenerator: Function<Entity, EntityProjectile>? = null
    private var stop = false
    private var cachedTarget: Entity? = null

    /**
     * @param entityCreature the entity to add the goal to.
     * @param delay          the delay between each shots.
     * @param attackRange    the allowed range the entity can shoot others.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      whether entity should go as close as possible to the target whether target is not in line of sight.
     * @param spread         shot spread (0 for best accuracy).
     * @param power          shot power (1 for normal).
     * @param timeUnit       the unit of the delay.
     */
    constructor(
        entityCreature: EntityCreature,
        delay: Int,
        attackRange: Int,
        desirableRange: Int,
        comeClose: Boolean,
        power: Double,
        spread: Double,
        timeUnit: TemporalUnit
    ) : this(
        entityCreature,
        Duration.of(delay.toLong(), timeUnit),
        attackRange,
        desirableRange,
        comeClose,
        power,
        spread
    ) {
    }

    /**
     * @param entityCreature the entity to add the goal to.
     * @param delay          the delay between each shots.
     * @param attackRange    the allowed range the entity can shoot others.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      whether entity should go as close as possible to the target whether target is not in line of sight.
     * @param spread         shot spread (0 for best accuracy).
     * @param power          shot power (1 for normal).
     */
    init {
        attackRangeSquared = attackRange * attackRange
        desirableRangeSquared = desirableRange * desirableRange
        this.comeClose = comeClose
        this.power = power
        this.spread = spread
        argCondition(desirableRange > attackRange, "Desirable range can not exceed attack range!")
    }

    fun setProjectileGenerator(projectileGenerator: Function<Entity, EntityProjectile>?) {
        this.projectileGenerator = projectileGenerator
    }

    override fun shouldStart(): Boolean {
        cachedTarget = findTarget()
        return cachedTarget != null
    }

    override fun start() {
        entityCreature.getNavigator().setPathTo(cachedTarget!!.position)
    }

    override fun tick(time: Long) {
        val target: Entity?
        if (cachedTarget != null) {
            target = cachedTarget
            cachedTarget = null
        } else {
            target = findTarget()
        }
        if (target == null) {
            stop = true
            return
        }
        val distanceSquared = entityCreature.getDistanceSquared(target)
        var comeClose = false
        if (distanceSquared <= attackRangeSquared) {
            if (!Cooldown.hasCooldown(time, lastShot, delay)) {
                if (entityCreature.hasLineOfSight(target)) {
                    val to = target.position.add(0.0, target.eyeHeight, 0.0)
                    var projectileGenerator = projectileGenerator
                    if (projectileGenerator == null) {
                        projectileGenerator =
                            Function { shooter: Entity? -> EntityProjectile(shooter, EntityType.ARROW) }
                    }
                    val projectile = projectileGenerator.apply(entityCreature)
                    projectile.setInstance(
                        entityCreature.instance!!,
                        entityCreature.position.add(0.0, entityCreature.eyeHeight, 0.0)
                    )
                    projectile.shoot(to, power, spread)
                    lastShot = time
                } else {
                    comeClose = this.comeClose
                }
            }
        }
        val navigator: Navigator = entityCreature.getNavigator()
        val pathPosition = navigator.pathPosition
        if (!comeClose && distanceSquared <= desirableRangeSquared) {
            if (pathPosition != null) {
                navigator.setPathTo(null)
            }
            entityCreature.lookAt(target)
            return
        }
        val targetPosition = target.position
        if (pathPosition == null || !pathPosition.samePoint(targetPosition)) {
            if (cooldown.isReady(time)) {
                cooldown.refreshLastUpdate(time)
                navigator.setPathTo(targetPosition)
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