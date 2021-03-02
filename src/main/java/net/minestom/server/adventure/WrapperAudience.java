package net.minestom.server.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link ForwardingAudience} that acts as a wrapper around another
 * iterable collection of audiences.
 */
public class WrapperAudience implements ForwardingAudience {
    private final Iterable<? extends Audience> audiences;

    /**
     * Creates a new wrapper audience.
     *
     * @param audiences the audiences to wrap
     */
    public WrapperAudience(@NotNull Iterable<? extends Audience> audiences) {
        this.audiences = audiences;
    }

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        return this.audiences;
    }
}
