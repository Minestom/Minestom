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

/**
 * Allows entity to perform both melee and ranged attacks.
 */
class CombinedAttackGoal(
    entityCreature: EntityCreature,
    meleeRange: Int, meleeDelay: Duration?,
    rangedRange: Int, rangedPower: Double, rangedSpread: Double, rangedDelay: Duration?,
    desirableRange: Int, comeClose: Boolean
) : GoalSelector(entityCreature) {
    val cooldown = Cooldown(Duration.of(5, TimeUnit.SERVER_TICK))
    private val meleeRangeSquared: Int
    private val meleeDelay: Duration?
    private val rangedRangeSquared: Int
    private val rangedPower: Double
    private val rangedSpread: Double
    private val rangedDelay: Duration?
    private val desirableRangeSquared: Int
    private val comeClose: Boolean
    private var projectileGenerator: Function<Entity, EntityProjectile>? = null
    private var lastAttack: Long = 0
    private var stop = false
    private var cachedTarget: Entity? = null

    /**
     * @param entityCreature the entity to add the goal to.
     * @param meleeRange     the allowed range the entity can hit others in melee.
     * @param rangedRange    the allowed range the entity can shoot others.
     * @param rangedPower    shot power (1 for normal).
     * @param rangedSpread   shot spread (0 for best accuracy).
     * @param delay          the delay between any attacks.
     * @param timeUnit       the unit of the delay.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      if entity should go as close as possible to the target whether target is not in line of sight for a ranged attack.
     */
    constructor(
        entityCreature: EntityCreature,
        meleeRange: Int, rangedRange: Int, rangedPower: Double, rangedSpread: Double,
        delay: Int, timeUnit: TemporalUnit?,
        desirableRange: Int, comeClose: Boolean
    ) : this(
        entityCreature,
        meleeRange, delay, timeUnit,
        rangedRange, rangedPower, rangedSpread, delay, timeUnit,
        desirableRange, comeClose
    ) {
    }

    /**
     * @param entityCreature the entity to add the goal to.
     * @param meleeRange     the allowed range the entity can hit others in melee.
     * @param rangedRange    the allowed range the entity can shoot others.
     * @param rangedPower    shot power (1 for normal).
     * @param rangedSpread   shot spread (0 for best accuracy).
     * @param delay          the delay between any attacks.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      if entity should go as close as possible to the target whether target is not in line of sight for a ranged attack.
     */
    constructor(
        entityCreature: EntityCreature,
        meleeRange: Int, rangedRange: Int, rangedPower: Double, rangedSpread: Double,
        delay: Duration?,
        desirableRange: Int, comeClose: Boolean
    ) : this(
        entityCreature,
        meleeRange, delay,
        rangedRange, rangedPower, rangedSpread, delay,
        desirableRange, comeClose
    ) {
    }

    /**
     * @param entityCreature the entity to add the goal to.
     * @param meleeRange     the allowed range the entity can hit others in melee.
     * @param meleeDelay     the delay between melee attacks.
     * @param meleeTimeUnit  the unit of the melee delay.
     * @param rangedRange    the allowed range the entity can shoot others.
     * @param rangedPower    shot power (1 for normal).
     * @param rangedSpread   shot spread (0 for best accuracy).
     * @param rangedDelay    the delay between ranged attacks.
     * @param rangedTimeUnit the unit of the ranged delay.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      if entity should go as close as possible to the target whether target is not in line of sight for a ranged attack.
     */
    constructor(
        entityCreature: EntityCreature,
        meleeRange: Int, meleeDelay: Int, meleeTimeUnit: TemporalUnit?,
        rangedRange: Int, rangedPower: Double, rangedSpread: Double, rangedDelay: Int, rangedTimeUnit: TemporalUnit?,
        desirableRange: Int, comeClose: Boolean
    ) : this(
        entityCreature,
        meleeRange,
        Duration.of(meleeDelay.toLong(), meleeTimeUnit),
        rangedRange,
        rangedPower,
        rangedSpread,
        Duration.of(rangedDelay.toLong(), rangedTimeUnit),
        desirableRange,
        comeClose
    ) {
    }

    /**
     * @param entityCreature the entity to add the goal to.
     * @param meleeRange     the allowed range the entity can hit others in melee.
     * @param meleeDelay     the delay between melee attacks.
     * @param rangedRange    the allowed range the entity can shoot others.
     * @param rangedPower    shot power (1 for normal).
     * @param rangedSpread   shot spread (0 for best accuracy).
     * @param rangedDelay    the delay between ranged attacks.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      if entity should go as close as possible to the target whether target is not in line of sight for a ranged attack.
     */
    init {
        meleeRangeSquared = meleeRange * meleeRange
        this.meleeDelay = meleeDelay
        rangedRangeSquared = rangedRange * rangedRange
        this.rangedPower = rangedPower
        this.rangedSpread = rangedSpread
        this.rangedDelay = rangedDelay
        desirableRangeSquared = desirableRange * desirableRange
        this.comeClose = comeClose
        argCondition(desirableRange > rangedRange, "Desirable range can not exceed ranged range!")
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
        // First of all, checking if to perform melee or ranged attack depending on the distance to target.
        if (distanceSquared <= meleeRangeSquared) {
            if (!Cooldown.hasCooldown(time, lastAttack, meleeDelay!!)) {
                entityCreature.attack(target, true)
                lastAttack = time
            }
        } else if (distanceSquared <= rangedRangeSquared) {
            if (!Cooldown.hasCooldown(time, lastAttack, rangedDelay!!)) {
                if (entityCreature.hasLineOfSight(target)) {
                    // If target is on line of entity sight, ranged attack can be performed
                    val to = target.position.add(0.0, target.eyeHeight, 0.0)
                    var projectileGenerator = projectileGenerator
                    if (projectileGenerator == null) {
                        projectileGenerator =
                            Function { shooter: Entity? -> EntityProjectile(shooter, EntityType.ARROW) }
                    }
                    val projectile = projectileGenerator.apply(entityCreature)
                    projectile.setInstance(entityCreature.instance!!, entityCreature.position)
                    projectile.shoot(to, rangedPower, rangedSpread)
                    lastAttack = time
                } else {
                    // Otherwise deciding whether to go to the enemy.
                    comeClose = this.comeClose
                }
            }
        }
        val navigator: Navigator = entityCreature.getNavigator()
        val pathPosition = navigator.pathPosition
        // If we don't want to come close and we're already within desirable range, no movement is needed.
        if (!comeClose && distanceSquared <= desirableRangeSquared) {
            if (pathPosition != null) {
                navigator.setPathTo(null)
            }
            entityCreature.lookAt(target)
            return
        }
        // Otherwise going to the target.
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