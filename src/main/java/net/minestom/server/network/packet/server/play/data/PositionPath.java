package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

/**
 * An absolute path an entity follows, either a single destination or a series of timed waypoints.
 * Positions are full precision, so unlike {@link VecDelta} a path covers any distance and does not
 * depend on the receiver agreeing about where the entity was.
 * <p>
 * Both the variant and the waypoint count travel in the payload, so a path reads on its own, where a
 * delta has to be told its step count by the packet carrying it.
 */
public sealed interface PositionPath {
    NetworkBuffer.Type<PositionPath> NETWORK_TYPE = NetworkBuffer.Enum(Type.class)
            .unionType(Type::networkType, PositionPath::type);

    Point endPosition();

    @ApiStatus.Internal
    Type type();

    /**
     * A path with a single destination.
     *
     * @param endPosition the absolute position the entity moves to
     */
    record Linear(Point endPosition) implements PositionPath {
        static final NetworkBuffer.Type<PositionPath> NETWORK_TYPE = VECTOR3D
                .transform(Linear::new, PositionPath::endPosition);

        @Override
        @ApiStatus.Internal
        public Type type() {
            return Type.LINEAR;
        }
    }

    /**
     * A path visiting every waypoint in order.
     * <p>
     * Only the steps are written, the receiver takes the last step as the destination. The end position
     * must therefore match the position of the last step, {@link #Stepped(List)} derives it. Construction
     * fails with an {@link IllegalArgumentException} when the steps are empty, exceed the number the protocol
     * allows, or end somewhere other than {@code endPosition}.
     *
     * @param endPosition where the path ends, always the position of the last step
     * @param steps       the waypoints in the order they are reached, must not be empty
     */
    record Stepped(Point endPosition, List<Step> steps) implements PositionPath {
        private static final int MAX_STEPS = 256;

        static final NetworkBuffer.Type<PositionPath> NETWORK_TYPE = Step.NETWORK_TYPE.list(MAX_STEPS)
                .transform(Stepped::new, path -> ((Stepped) path).steps());

        /**
         * Creates a path ending at the position of the last step.
         *
         * @param steps the waypoints in the order they are reached, must not be empty
         * @throws IllegalArgumentException if {@code steps} is empty or has more entries than the protocol allows
         */
        public Stepped(List<Step> steps) {
            if (steps.isEmpty()) throw new IllegalArgumentException("A stepped path must have at least one step");
            this(steps.getLast().position(), steps);
        }

        public Stepped {
            steps = List.copyOf(steps);
            if (steps.isEmpty()) throw new IllegalArgumentException("A stepped path must have at least one step");
            if (steps.size() > MAX_STEPS) throw new IllegalArgumentException("A stepped path may have at most " + MAX_STEPS + " steps");
            final Point lastPosition = steps.getLast().position();
            if (!endPosition.samePoint(lastPosition)) {
                throw new IllegalArgumentException("End position " + endPosition + " is not the last step position " + lastPosition);
            }
        }

        @Override
        @ApiStatus.Internal
        public Type type() {
            return Type.STEPPED;
        }
    }

    /**
     * One waypoint of a stepped path, reached {@code tickOffset} ticks after the packet is applied.
     *
     * @param position   the absolute position of the waypoint
     * @param tickOffset the tick the waypoint is reached at
     */
    record Step(Point position, int tickOffset) {
        static final NetworkBuffer.Type<Step> NETWORK_TYPE = NetworkBufferTemplate.template(
                VECTOR3D, Step::position,
                VAR_INT, Step::tickOffset,
                Step::new);
    }

    @ApiStatus.Internal
    enum Type {
        LINEAR, STEPPED;

        private NetworkBuffer.Type<PositionPath> networkType() {
            return switch (this) {
                case LINEAR -> Linear.NETWORK_TYPE;
                case STEPPED -> Stepped.NETWORK_TYPE;
            };
        }
    }
}
