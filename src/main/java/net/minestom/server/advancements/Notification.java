package net.minestom.server.advancements;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a toast which can be sent to the player using {@link net.minestom.server.entity.Player#sendNotification(Notification)}.
 */
public record Notification(@NotNull Component title, @NotNull FrameType frameType, @NotNull ItemStack icon) {
    private static final String IDENTIFIER = "minestom:notification";

    public Notification(@NotNull Component title, @NotNull FrameType frameType, @NotNull Material icon) {
        this(title, frameType, ItemStack.of(icon));
    }

    @ApiStatus.Internal
    public @NotNull AdvancementsPacket buildAddPacket() {
        // For an advancement to be shown, it must have all of its criteria achieved (progress 100%)
        // Create a criteria that we can set to 100% achieved.
        final var displayData = new AdvancementsPacket.DisplayData(
                title, Component.text("Articdive was here. #Minestom"),
                icon, frameType,
                0x6, null, 0f, 0f);

        final var criteria = new AdvancementsPacket.Criteria("minestom:some_criteria",
                new AdvancementsPacket.CriterionProgress(System.currentTimeMillis()));

        final var advancement = new AdvancementsPacket.Advancement(null, displayData,
                List.of(new AdvancementsPacket.Requirement(List.of(criteria.criterionIdentifier()))),
                false);

        final var mapping = new AdvancementsPacket.AdvancementMapping(IDENTIFIER, advancement);
        final var progressMapping = new AdvancementsPacket.ProgressMapping(IDENTIFIER,
                new AdvancementsPacket.AdvancementProgress(List.of(criteria)));

        return new AdvancementsPacket(
                false,
                List.of(mapping),
                List.of(),
                List.of(progressMapping));
    }

    @ApiStatus.Internal
    public @NotNull AdvancementsPacket buildRemovePacket() {
        return new AdvancementsPacket(
                false,
                List.of(),
                List.of(IDENTIFIER),
                List.of());
    }
}
