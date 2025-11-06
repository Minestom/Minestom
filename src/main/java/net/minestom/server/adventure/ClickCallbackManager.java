package net.minestom.server.adventure;

import java.util.Map;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.Tickable;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.common.ClientCustomClickActionPacket;
import net.minestom.server.utils.UUIDUtils;

/**
 * Manager for Adventure click callbacks.
 */
public class ClickCallbackManager implements Tickable {
    private static final Key KEY = Key.key("minestom", "click_callback");

    private final Map<UUID, ClickCallback<Audience>> permanent = new ConcurrentHashMap<>();
    private final Map<UUID, CallbackData> temporary = new ConcurrentHashMap<>();

    private static class CallbackData {
        private final ClickCallback<Audience> callback;
        private final long expiry;

        private long uses;

        CallbackData(final ClickCallback<Audience> callback, final long expiry, final long uses) {
            this.callback = callback;
            this.expiry = expiry;
            this.uses = uses;
        }

        boolean isExpired(final long time) {
            return this.uses <= 0 || time >= this.expiry;
        }

        void consume(final Audience audience) {
            if (!this.isExpired(System.nanoTime())) {
                this.uses--;
                this.callback.accept(audience);
            }
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
        if (!packet.key().equals(KEY)) return;

        if (packet.payload() instanceof final IntArrayBinaryTag tag) {
            final UUID uuid;
            try {
                uuid = UUIDUtils.fromNbt(tag);
            } catch (final IndexOutOfBoundsException ignored) {
                return;
            }

            final ClickCallback<Audience> data = this.permanent.get(uuid);
            if (data != null) {
                data.accept(player);
            } else {
                final CallbackData temp = this.temporary.get(uuid);
                if (temp != null) {
                    temp.consume(player);
                }
            }
        }
    }

    /**
     * Creates a click event from the given callback and options.
     *
     * @param callback the callback
     * @param options the options
     * @return the click event
     */
    public ClickEvent createClickEvent(final ClickCallback<Audience> callback, final ClickCallback.Options options) {
        final UUID uuid = UUID.randomUUID();
        final int uses = options.uses();

        long expiry;
        try {
            expiry = System.nanoTime() + options.lifetime().toNanos();
        } catch (final ArithmeticException ignored) {
            expiry = Long.MAX_VALUE;
        }

        if (expiry == Long.MAX_VALUE && uses == ClickCallback.UNLIMITED_USES) {
            this.permanent.put(uuid, callback);
        } else if (uses > 0) {
            this.temporary.put(uuid, new CallbackData(callback, expiry, uses));
        }

        return ClickEvent.custom(KEY, new BinaryTagHolderImpl(UUIDUtils.toNbt(uuid)));
    }
}
