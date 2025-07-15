package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public record AdvancementsPacket(
        boolean reset,
        List<AdvancementMapping> advancementMappings,
        List<String> identifiersToRemove,
        List<ProgressMapping> progressMappings,
        boolean showAdvancements
) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final int MAX_ADVANCEMENTS = Short.MAX_VALUE;

    public static final NetworkBuffer.Type<AdvancementsPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.BOOLEAN, AdvancementsPacket::reset,
            AdvancementMapping.SERIALIZER.list(MAX_ADVANCEMENTS), AdvancementsPacket::advancementMappings,
            NetworkBuffer.STRING.list(MAX_ADVANCEMENTS), AdvancementsPacket::identifiersToRemove,
            ProgressMapping.SERIALIZER.list(MAX_ADVANCEMENTS), AdvancementsPacket::progressMappings,
            NetworkBuffer.BOOLEAN, AdvancementsPacket::showAdvancements,
            AdvancementsPacket::new
    );

    public AdvancementsPacket {
        advancementMappings = List.copyOf(advancementMappings);
        identifiersToRemove = List.copyOf(identifiersToRemove);
        progressMappings = List.copyOf(progressMappings);
    }

    // TODO is the display-item needed to be updated?
    @Override
    public Collection<Component> components() {
        final var displayData = this.advancementMappings.stream().map(AdvancementMapping::value).map(Advancement::displayData).filter(Objects::nonNull).toList();
        final var titles = displayData.stream().map(DisplayData::title).toList();
        final var descriptions = displayData.stream().map(DisplayData::description).toList();

        final var list = new ArrayList<Component>();

        list.addAll(titles);
        list.addAll(descriptions);

        return List.copyOf(list);
    }

    @Override
    public ServerPacket copyWithOperator(final UnaryOperator<Component> operator) {
        return new AdvancementsPacket(
                this.reset,
                this.advancementMappings.stream().map(mapping -> mapping.copyWithOperator(operator)).toList(),
                this.identifiersToRemove,
                this.progressMappings,
                this.showAdvancements
        );
    }

    /**
     * AdvancementMapping maps the namespaced ID to the Advancement.
     */
    public record AdvancementMapping(String key,
                                     Advancement value) implements ComponentHolder<AdvancementMapping> {
        public static final NetworkBuffer.Type<AdvancementMapping> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING, AdvancementMapping::key,
                Advancement.SERIALIZER, AdvancementMapping::value,
                AdvancementMapping::new
        );

        @Override
        public Collection<Component> components() {
            return this.value.components();
        }

        @Override
        public AdvancementMapping copyWithOperator(UnaryOperator<Component> operator) {
            return this.value.displayData == null ? this : new AdvancementMapping(this.key, this.value.copyWithOperator(operator));
        }
    }

    public record Advancement(@Nullable String parentIdentifier, @Nullable DisplayData displayData,
                              List<Requirement> requirements,
                              boolean sendTelemetryData) implements ComponentHolder<Advancement> {
        public Advancement {
            requirements = List.copyOf(requirements);
        }

        public static final NetworkBuffer.Type<Advancement> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING.optional(), Advancement::parentIdentifier,
                DisplayData.SERIALIZER.optional(), Advancement::displayData,
                Requirement.SERIALIZER.list(MAX_ADVANCEMENTS), Advancement::requirements,
                NetworkBuffer.BOOLEAN, Advancement::sendTelemetryData,
                Advancement::new
        );

        @Override
        public Collection<Component> components() {
            return this.displayData != null ? this.displayData.components() : List.of();
        }

        @Override
        public Advancement copyWithOperator(UnaryOperator<Component> operator) {
            return this.displayData == null ? this : new Advancement(this.parentIdentifier, this.displayData.copyWithOperator(operator), this.requirements, this.sendTelemetryData);
        }
    }

    public record Requirement(List<String> requirements) {
        public static final NetworkBuffer.Type<Requirement> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING.list(MAX_ADVANCEMENTS), Requirement::requirements,
                Requirement::new
        );

        public Requirement {
            requirements = List.copyOf(requirements);
        }
    }

    public record DisplayData(Component title, Component description,
                              ItemStack icon, FrameType frameType,
                              int flags, @Nullable String backgroundTexture,
                              float x, float y) implements ComponentHolder<DisplayData> {

        public static final NetworkBuffer.Type<DisplayData> SERIALIZER = new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, DisplayData value) {
                buffer.write(NetworkBuffer.COMPONENT, value.title);
                buffer.write(NetworkBuffer.COMPONENT, value.description);
                buffer.write(ItemStack.NETWORK_TYPE, value.icon);
                buffer.write(NetworkBuffer.Enum(FrameType.class), value.frameType);
                buffer.write(NetworkBuffer.INT, value.flags);
                if ((value.flags & 0x1) != 0) {
                    assert value.backgroundTexture != null;
                    buffer.write(NetworkBuffer.STRING, value.backgroundTexture);
                }
                buffer.write(NetworkBuffer.FLOAT, value.x);
                buffer.write(NetworkBuffer.FLOAT, value.y);
            }

            @Override
            public DisplayData read(NetworkBuffer buffer) {
                var title = buffer.read(NetworkBuffer.COMPONENT);
                var description = buffer.read(NetworkBuffer.COMPONENT);
                var icon = buffer.read(ItemStack.NETWORK_TYPE);
                var frameType = FrameType.values()[buffer.read(NetworkBuffer.VAR_INT)];
                var flags = buffer.read(NetworkBuffer.INT);
                var backgroundTexture = (flags & 0x1) != 0 ? buffer.read(NetworkBuffer.STRING) : null;
                var x = buffer.read(NetworkBuffer.FLOAT);
                var y = buffer.read(NetworkBuffer.FLOAT);
                return new DisplayData(title, description,
                        icon, frameType,
                        flags, backgroundTexture,
                        x, y);
            }
        };

        @Override
        public Collection<Component> components() {
            return List.of(this.title, this.description);
        }

        @Override
        public DisplayData copyWithOperator(UnaryOperator<Component> operator) {
            return new DisplayData(operator.apply(this.title), operator.apply(this.description), this.icon, this.frameType, this.flags, this.backgroundTexture, this.x, this.y);
        }
    }

    public record ProgressMapping(String key, AdvancementProgress progress) {
        public static final NetworkBuffer.Type<ProgressMapping> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING, ProgressMapping::key,
                AdvancementProgress.SERIALIZER, ProgressMapping::progress,
                ProgressMapping::new
        );
    }

    public record AdvancementProgress(List<Criteria> criteria) {
        public static final NetworkBuffer.Type<AdvancementProgress> SERIALIZER = NetworkBufferTemplate.template(
                Criteria.SERIALIZER.list(MAX_ADVANCEMENTS), AdvancementProgress::criteria,
                AdvancementProgress::new
        );

        public AdvancementProgress {
            criteria = List.copyOf(criteria);
        }
    }

    public record Criteria(String criterionIdentifier, CriterionProgress criterionProgress) {
        public static final NetworkBuffer.Type<Criteria> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING, Criteria::criterionIdentifier,
                CriterionProgress.SERIALIZER, Criteria::criterionProgress,
                Criteria::new
        );
    }

    public record CriterionProgress(@Nullable Long dateOfAchieving) {
        public static final NetworkBuffer.Type<CriterionProgress> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.LONG.optional(), CriterionProgress::dateOfAchieving,
                CriterionProgress::new
        );
    }
}