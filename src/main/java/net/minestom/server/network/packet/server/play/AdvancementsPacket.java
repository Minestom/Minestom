package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
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

    public static NetworkBuffer.Type<AdvancementsPacket> SERIALIZER = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, AdvancementsPacket value) {
            buffer.write(BOOLEAN, value.reset);
            buffer.writeCollection(value.advancementMappings);
            buffer.writeCollection(STRING, value.identifiersToRemove);
            buffer.writeCollection(value.progressMappings);
        }

        @Override
        public AdvancementsPacket read(@NotNull NetworkBuffer buffer) {
            return new AdvancementsPacket(
                    buffer.read(BOOLEAN),
                    buffer.readCollection(AdvancementMapping::new, MAX_ADVANCEMENTS),
                    buffer.readCollection(STRING, MAX_ADVANCEMENTS),
                    buffer.readCollection(ProgressMapping::new, MAX_ADVANCEMENTS)
            );
        }
    };

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
                                     @NotNull Advancement value) implements NetworkBuffer.Writer, ComponentHolder<AdvancementMapping> {
        public AdvancementMapping(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), new Advancement(reader));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, key);
            writer.write(value);
        }

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
                              boolean sendTelemetryData) implements NetworkBuffer.Writer, ComponentHolder<Advancement> {
        public Advancement {
            requirements = List.copyOf(requirements);
        }

        public Advancement(@NotNull NetworkBuffer reader) {
            this(reader.readOptional(STRING), reader.readOptional(DisplayData::new),
                    reader.readCollection(Requirement::new, MAX_ADVANCEMENTS), reader.read(BOOLEAN));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.writeOptional(STRING, parentIdentifier);
            writer.writeOptional(displayData);
            writer.writeCollection(requirements);
            writer.write(BOOLEAN, sendTelemetryData);
        }

        @Override
        public @NotNull Collection<Component> components() {
            return this.displayData != null ? this.displayData.components() : List.of();
        }

        @Override
        public @NotNull Advancement copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return this.displayData == null ? this : new Advancement(this.parentIdentifier, this.displayData.copyWithOperator(operator), this.requirements, this.sendTelemetryData);
        }
    }

    public record Requirement(@NotNull List<String> requirements) implements NetworkBuffer.Writer {
        public Requirement {
            requirements = List.copyOf(requirements);
        }

        public Requirement(@NotNull NetworkBuffer reader) {
            this(reader.readCollection(STRING, MAX_ADVANCEMENTS));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.writeCollection(STRING, requirements);
        }
    }

    public record DisplayData(@NotNull Component title, @NotNull Component description,
                              @NotNull ItemStack icon, @NotNull FrameType frameType,
                              int flags, @Nullable String backgroundTexture,
                              float x, float y) implements NetworkBuffer.Writer, ComponentHolder<DisplayData> {
        public DisplayData(@NotNull NetworkBuffer reader) {
            this(read(reader));
        }

        private DisplayData(DisplayData displayData) {
            this(displayData.title, displayData.description,
                    displayData.icon, displayData.frameType,
                    displayData.flags, displayData.backgroundTexture,
                    displayData.x, displayData.y);
        }

        private static DisplayData read(@NotNull NetworkBuffer reader) {
            var title = reader.read(COMPONENT);
            var description = reader.read(COMPONENT);
            var icon = reader.read(ItemStack.NETWORK_TYPE);
            var frameType = FrameType.values()[reader.read(VAR_INT)];
            var flags = reader.read(INT);
            var backgroundTexture = (flags & 0x1) != 0 ? reader.read(STRING) : null;
            var x = reader.read(FLOAT);
            var y = reader.read(FLOAT);
            return new DisplayData(title, description,
                    icon, frameType,
                    flags, backgroundTexture,
                    x, y);
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(COMPONENT, title);
            writer.write(COMPONENT, description);
            writer.write(ItemStack.NETWORK_TYPE, icon);
            writer.writeEnum(FrameType.class, frameType);
            writer.write(INT, flags);
            if ((flags & 0x1) != 0) {
                assert backgroundTexture != null;
                writer.write(STRING, backgroundTexture);
            }
            writer.write(FLOAT, x);
            writer.write(FLOAT, y);
        }

        @Override
        public @NotNull Collection<Component> components() {
            return List.of(this.title, this.description);
        }

        @Override
        public @NotNull DisplayData copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return new DisplayData(operator.apply(this.title), operator.apply(this.description), this.icon, this.frameType, this.flags, this.backgroundTexture, this.x, this.y);
        }
    }

    public record ProgressMapping(@NotNull String key,
                                  @NotNull AdvancementProgress progress) implements NetworkBuffer.Writer {
        public ProgressMapping(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), new AdvancementProgress(reader));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, key);
            writer.write(progress);
        }
    }

    public record AdvancementProgress(@NotNull List<Criteria> criteria) implements NetworkBuffer.Writer {
        public AdvancementProgress {
            criteria = List.copyOf(criteria);
        }

        public AdvancementProgress(@NotNull NetworkBuffer reader) {
            this(reader.readCollection(Criteria::new, MAX_ADVANCEMENTS));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.writeCollection(criteria);
        }
    }

    public record Criteria(@NotNull String criterionIdentifier,
                           @NotNull CriterionProgress criterionProgress) implements NetworkBuffer.Writer {
        public Criteria(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), new CriterionProgress(reader));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, criterionIdentifier);
            writer.write(criterionProgress);
        }
    }

    public record CriterionProgress(@Nullable Long dateOfAchieving) implements NetworkBuffer.Writer {
        public CriterionProgress(@NotNull NetworkBuffer reader) {
            this(reader.readOptional(LONG));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.writeOptional(LONG, dateOfAchieving);
        }
    }
}