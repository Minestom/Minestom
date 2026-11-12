package net.minestom.server.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.Tickable;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.common.ClientCustomClickActionPacket;
import net.minestom.server.utils.UUIDUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manager for Adventure click callbacks.
 */
public final class ClickCallbackManager implements Tickable {
    private static final Key KEY = Key.key("minestom", "click_callback");
    private static final ClickEvent<ClickEvent.Payload.Custom> EXPIRED = clickEvent(new UUID(0L, 0L));

    private final Map<UUID, ClickCallback<Audience>> permanent = new ConcurrentHashMap<>(0);
    private final Map<UUID, CallbackData> temporary = new ConcurrentHashMap<>(0);

    private record CallbackData(ClickCallback<Audience> callback, long expiry, @Nullable AtomicInteger uses) {
        boolean isExpired(final long time) {
            return time >= this.expiry || (this.uses != null && this.uses.get() <= 0);
        }

        void consume(final Audience audience, final long time) {
            if (time >= this.expiry) return;
            if (this.uses != null && this.uses.getAndUpdate(current -> current > 0 ? current - 1 : current) <= 0) return;
            this.callback.accept(audience);
        }
    }

    @Override
    public void tick(final long time) {
        if (this.temporary.isEmpty()) return;
        this.temporary.values().removeIf(data -> data.isExpired(time));
    }

    /**
     * Consumes a custom click event.
     *
     * @param player the player who performed the click
     * @param packet the packet
     */
    public void consumeCustomClick(final Player player, final ClientCustomClickActionPacket packet) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(packet, "packet");
        if (!packet.key().equals(KEY)) return;
        if (!(packet.payload() instanceof final IntArrayBinaryTag tag)) return;

        final UUID uuid;
        try {
            uuid = UUIDUtils.fromNbt(tag);
        } catch (final IndexOutOfBoundsException _) {
            return;
        }

        final ClickCallback<Audience> callback = this.permanent.get(uuid);
        if (callback != null) {
            callback.accept(player);
            return;
        }
        final CallbackData data = this.temporary.get(uuid);
        if (data != null) data.consume(player, System.nanoTime());
    }

    /**
     * Creates a click event from the given callback and options.
     *
     * @param callback the callback
     * @param options the options
     * @return the click event
     */
    public ClickEvent<ClickEvent.Payload.Custom> createClickEvent(final ClickCallback<Audience> callback, final ClickCallback.Options options) {
        Objects.requireNonNull(callback, "callback");
        Objects.requireNonNull(options, "options");
        final int uses = options.uses();
        final boolean unlimitedUses = uses == ClickCallback.UNLIMITED_USES;
        final Duration lifetime = options.lifetime();
        if (!lifetime.isPositive() || (!unlimitedUses && uses <= 0)) return EXPIRED;

        final UUID uuid = UUID.randomUUID();
        final long expiry = expiry(System.nanoTime(), lifetime);
        if (unlimitedUses && expiry == Long.MAX_VALUE) {
            this.permanent.put(uuid, callback);
        } else {
            this.temporary.put(uuid, new CallbackData(callback, expiry, unlimitedUses ? null : new AtomicInteger(uses)));
        }
        return clickEvent(uuid);
    }

    private static ClickEvent<ClickEvent.Payload.Custom> clickEvent(final UUID uuid) {
        return ClickEvent.custom(KEY, new BinaryTagHolderImpl(UUIDUtils.toNbt(uuid)));
    }

    private static long expiry(final long time, final Duration lifetime) {
        try {
            return Math.addExact(time, lifetime.toNanos());
        } catch (final ArithmeticException _) {
            return Long.MAX_VALUE;
        }
    }
}
