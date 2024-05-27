package net.minestom.server.notifications;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * {@inheritDoc}
 */
record NotificationImpl(@NotNull Component title, @NotNull FrameType type,
                        @NotNull ItemStack icon) implements Notification {
    /**
     * {@inheritDoc}
     */
    @Override
    public void send(@NotNull Player player) {
        player.sendPacket(createPacket());
        player.sendPacket(REMOVE_PACKET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(@NotNull Collection<@NotNull Player> players) {
        players.forEach(this::send);
    }

    /**
     * Create the advancement packet that simulates the notification.
     * It's not private because integration tests
     * @return the packet
     */
    @NotNull AdvancementsPacket createPacket() {
        final var displayData = new AdvancementsPacket.DisplayData(
                title(), Component.empty(),
                icon(), type(),
                0x6, null, 0f, 0f);

        final var criteria = new AdvancementsPacket.Criteria("minestom:some_criteria",
                new AdvancementsPacket.CriterionProgress(System.currentTimeMillis()));

        final var advancement = new AdvancementsPacket.Advancement(null, displayData,
                List.of(new AdvancementsPacket.Requirement(List.of(criteria.criterionIdentifier()))),
                false);

        final var mapping = new AdvancementsPacket.AdvancementMapping(IDENTIFIER, advancement);
        final var progressMapping = new AdvancementsPacket.ProgressMapping(IDENTIFIER,
                new AdvancementsPacket.AdvancementProgress(List.of(criteria)));
        return new AdvancementsPacket(false, List.of(mapping), List.of(), List.of(progressMapping));
    }
}
