package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record AdvancementsPacket(boolean reset, @NotNull List<AdvancementMapping> advancementMappings,
                                 @NotNull List<String> identifiersToRemove,
                                 @NotNull List<ProgressMapping> progressMappings) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final int MAX_ADVANCEMENTS = Short.MAX_VALUE;

    public static final NetworkBuffer.Type<AdvancementsPacket> SERIALIZER = NetworkBufferTemplate.template(
            BOOLEAN, AdvancementsPacket::reset,
            AdvancementMapping.SERIALIZER.list(MAX_ADVANCEMENTS), AdvancementsPacket::advancementMappings,
            STRING.list(MAX_ADVANCEMENTS), AdvancementsPacket::identifiersToRemove,
            ProgressMapping.SERIALIZER.list(MAX_ADVANCEMENTS), AdvancementsPacket::progressMappings,
            AdvancementsPacket::new
    );

    public AdvancementsPacket {
        advancementMappings = List.copyOf(advancementMappings);
        identifiersToRemove = List.copyOf(identifiersToRemove);
        progressMappings = List.copyOf(progressMappings);
    }

    // TODO is the display-item needed to be updated?
    @Override
    public @NotNull Collection<Component> components() {
        final var displayData = this.advancementMappings.stream().map(AdvancementMapping::value).map(Advancement::displayData).filter(Objects::nonNull).toList();
        final var titles = displayData.stream().map(DisplayData::title).toList();
        final var descriptions = displayData.stream().map(DisplayData::description).toList();

        final var list = new ArrayList<Component>();

        list.addAll(titles);
        list.addAll(descriptions);

        return List.copyOf(list);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(final @NotNull UnaryOperator<Component> operator) {
        return new AdvancementsPacket(
                this.reset,
                this.advancementMappings.stream().map(mapping -> mapping.copyWithOperator(operator)).toList(),
                this.identifiersToRemove,
                this.progressMappings
        );
    }

    /**
     * AdvancementMapping maps the namespaced ID to the Advancement.
     */
    public record AdvancementMapping(@NotNull String key,
                                     @NotNull Advancement value) implements ComponentHolder<AdvancementMapping> {
        public static final NetworkBuffer.Type<AdvancementMapping> SERIALIZER = NetworkBufferTemplate.template(
                STRING, AdvancementMapping::key,
                Advancement.SERIALIZER, AdvancementMapping::value,
                AdvancementMapping::new
        );

        @Override
        public @NotNull Collection<Component> components() {
            return this.value.components();
        }

        @Override
        public @NotNull AdvancementMapping copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return this.value.displayData == null ? this : new AdvancementMapping(this.key, this.value.copyWithOperator(operator));
        }
    }

    public record Advancement(@Nullable String parentIdentifier, @Nullable DisplayData displayData,
                              @NotNull List<Requirement> requirements,
                              boolean sendTelemetryData) implements ComponentHolder<Advancement> {
        public Advancement {
            requirements = List.copyOf(requirements);
        }

        public static final NetworkBuffer.Type<Advancement> SERIALIZER = NetworkBufferTemplate.template(
                STRING.optional(), Advancement::parentIdentifier,
                DisplayData.SERIALIZER.optional(), Advancement::displayData,
                Requirement.SERIALIZER.list(MAX_ADVANCEMENTS), Advancement::requirements,
                BOOLEAN, Advancement::sendTelemetryData,
                Advancement::new
        );

        @Override
        public @NotNull Collection<Component> components() {
            return this.displayData != null ? this.displayData.components() : List.of();
        }

        @Override
        public @NotNull Advancement copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return this.displayData == null ? this : new Advancement(this.parentIdentifier, this.displayData.copyWithOperator(operator), this.requirements, this.sendTelemetryData);
        }
    }

    public record Requirement(@NotNull List<String> requirements) {
        public static final NetworkBuffer.Type<Requirement> SERIALIZER = NetworkBufferTemplate.template(
                STRING.list(MAX_ADVANCEMENTS), Requirement::requirements,
                Requirement::new
        );

        public Requirement {
            requirements = List.copyOf(requirements);
        }
    }

    public record DisplayData(@NotNull Component title, @NotNull Component description,
                              @NotNull ItemStack icon, @NotNull FrameType frameType,
                              int flags, @Nullable String backgroundTexture,
                              float x, float y) implements ComponentHolder<DisplayData> {

        public static final NetworkBuffer.Type<DisplayData> SERIALIZER = new Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, DisplayData value) {
                buffer.write(COMPONENT, value.title);
                buffer.write(COMPONENT, value.description);
                buffer.write(ItemStack.NETWORK_TYPE, value.icon);
                buffer.write(NetworkBuffer.Enum(FrameType.class), value.frameType);
                buffer.write(INT, value.flags);
                if ((value.flags & 0x1) != 0) {
                    assert value.backgroundTexture != null;
                    buffer.write(STRING, value.backgroundTexture);
                }
                buffer.write(FLOAT, value.x);
                buffer.write(FLOAT, value.y);
            }

            @Override
            public DisplayData read(@NotNull NetworkBuffer buffer) {
                var title = buffer.read(COMPONENT);
                var description = buffer.read(COMPONENT);
                var icon = buffer.read(ItemStack.NETWORK_TYPE);
                var frameType = FrameType.values()[buffer.read(VAR_INT)];
                var flags = buffer.read(INT);
                var backgroundTexture = (flags & 0x1) != 0 ? buffer.read(STRING) : null;
                var x = buffer.read(FLOAT);
                var y = buffer.read(FLOAT);
                return new DisplayData(title, description,
                        icon, frameType,
                        flags, backgroundTexture,
                        x, y);
            }
        };

        @Override
        public @NotNull Collection<Component> components() {
            return List.of(this.title, this.description);
        }

        @Override
        public @NotNull DisplayData copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return new DisplayData(operator.apply(this.title), operator.apply(this.description), this.icon, this.frameType, this.flags, this.backgroundTexture, this.x, this.y);
        }
    }

    public record ProgressMapping(@NotNull String key, @NotNull AdvancementProgress progress) {
        public static final NetworkBuffer.Type<ProgressMapping> SERIALIZER = NetworkBufferTemplate.template(
                STRING, ProgressMapping::key,
                AdvancementProgress.SERIALIZER, ProgressMapping::progress,
                ProgressMapping::new
        );
    }

    public record AdvancementProgress(@NotNull List<@NotNull Criteria> criteria) {
        public static final NetworkBuffer.Type<AdvancementProgress> SERIALIZER = NetworkBufferTemplate.template(
                Criteria.SERIALIZER.list(MAX_ADVANCEMENTS), AdvancementProgress::criteria,
                AdvancementProgress::new
        );

        public AdvancementProgress {
            criteria = List.copyOf(criteria);
        }
    }

    public record Criteria(@NotNull String criterionIdentifier, @NotNull CriterionProgress criterionProgress) {
        public static final NetworkBuffer.Type<Criteria> SERIALIZER = NetworkBufferTemplate.template(
                STRING, Criteria::criterionIdentifier,
                CriterionProgress.SERIALIZER, Criteria::criterionProgress,
                Criteria::new
        );
    }

    public record CriterionProgress(@Nullable Long dateOfAchieving) {
        public static final NetworkBuffer.Type<CriterionProgress> SERIALIZER = NetworkBufferTemplate.template(
                LONG.optional(), CriterionProgress::dateOfAchieving,
                CriterionProgress::new
        );
    }
}