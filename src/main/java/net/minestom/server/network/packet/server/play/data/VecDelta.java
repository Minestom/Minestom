package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.SHORT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

/**
 * A relative movement encoded against the receiver's last known position, either a single delta or a
 * series of timed deltas. Each component is a signed 1/4096 block offset, so a single delta cannot span
 * more than 8 blocks on an axis.
 * <p>
 * The step count is not part of the payload, it travels in the packed properties of the packet carrying
 * the delta, which is why reading takes it as an argument.
 */
public sealed interface VecDelta {
    /**
     * The number of steps, {@code 0} for a linear delta.
     */
    int stepCount();

    static VecDelta read(NetworkBuffer buffer, int stepCount) {
        if (stepCount <= 0) return buffer.read(Linear.NETWORK_TYPE);
        final long maxSteps = buffer.readableBytes() / Step.MIN_BYTES;
        if (stepCount > maxSteps) {
            throw new IllegalArgumentException("VecDelta with size " + stepCount + " is bigger than allowed " + maxSteps);
        }
        final Step[] steps = new Step[stepCount];
        for (int i = 0; i < stepCount; i++) steps[i] = buffer.read(Step.NETWORK_TYPE);
        return new Stepped(List.of(steps));
    }

    static void write(NetworkBuffer buffer, VecDelta delta) {
        switch (delta) {
            case Linear linear -> buffer.write(Linear.NETWORK_TYPE, linear);
            case Stepped(List<Step> steps) -> {
                for (Step step : steps) buffer.write(Step.NETWORK_TYPE, step);
            }
        }
    }

    /**
     * A single delta applied at once.
     *
     * @param deltaX the x offset in 1/4096 blocks
     * @param deltaY the y offset in 1/4096 blocks
     * @param deltaZ the z offset in 1/4096 blocks
     */
    record Linear(short deltaX, short deltaY, short deltaZ) implements VecDelta {
        static final NetworkBuffer.Type<Linear> NETWORK_TYPE = NetworkBufferTemplate.template(
                SHORT, Linear::deltaX,
                SHORT, Linear::deltaY,
                SHORT, Linear::deltaZ,
                Linear::new);

        @Override
        public int stepCount() {
            return 0;
        }
    }

    /**
     * A series of deltas applied in order, each relative to the previous step. Construction fails with an
     * {@link IllegalArgumentException} when the steps are empty.
     *
     * @param steps the deltas in the order they are applied, must not be empty
     */
    record Stepped(List<Step> steps) implements VecDelta {
        public Stepped {
            steps = List.copyOf(steps);
            // An empty stepped delta packs a step count of zero, which a reader takes as a linear delta and
            // then reads three shorts that were never written.
            if (steps.isEmpty()) throw new IllegalArgumentException("A stepped delta must have at least one step");
        }

        @Override
        public int stepCount() {
            return steps.size();
        }
    }

    /**
     * One delta of a stepped movement.
     *
     * @param ticks  the tick the step is reached at
     * @param deltaX the x offset in 1/4096 blocks
     * @param deltaY the y offset in 1/4096 blocks
     * @param deltaZ the z offset in 1/4096 blocks
     */
    record Step(int ticks, short deltaX, short deltaY, short deltaZ) {
        static final int MIN_BYTES = 7;

        static final NetworkBuffer.Type<Step> NETWORK_TYPE = NetworkBufferTemplate.template(
                VAR_INT, Step::ticks,
                SHORT, Step::deltaX,
                SHORT, Step::deltaY,
                SHORT, Step::deltaZ,
                Step::new);
    }
}
