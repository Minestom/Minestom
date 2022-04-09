package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record AdvancementsPacket(boolean reset, @NotNull List<AdvancementMapping> advancementMappings,
                                 @NotNull List<String> identifiersToRemove,
                                 @NotNull List<ProgressMapping> progressMappings) implements ServerPacket {
    public AdvancementsPacket {
        advancementMappings = List.copyOf(advancementMappings);
        identifiersToRemove = List.copyOf(identifiersToRemove);
        progressMappings = List.copyOf(progressMappings);
    }

    public AdvancementsPacket(BinaryReader reader) {
        this(reader.readBoolean(), reader.readVarIntList(AdvancementMapping::new),
                reader.readVarIntList(BinaryReader::readSizedString),
                reader.readVarIntList(ProgressMapping::new));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(reset);
        writer.writeVarIntList(advancementMappings, BinaryWriter::write);
        writer.writeVarIntList(identifiersToRemove, BinaryWriter::writeSizedString);
        writer.writeVarIntList(progressMappings, BinaryWriter::write);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ADVANCEMENTS;
    }

    /**
     * AdvancementMapping maps the namespaced ID to the Advancement.
     */
    public record AdvancementMapping(@NotNull String key, @NotNull Advancement value) implements Writeable {
        public AdvancementMapping(BinaryReader reader) {
            this(reader.readSizedString(), new Advancement(reader));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(key);
            writer.write(value);
        }
    }

    public record Advancement(@Nullable String parentIdentifier, @Nullable DisplayData displayData,
                              @NotNull List<String> criteria,
                              @NotNull List<Requirement> requirements) implements Writeable {
        public Advancement {
            criteria = List.copyOf(criteria);
            requirements = List.copyOf(requirements);
        }

        public Advancement(BinaryReader reader) {
            this(reader.readBoolean() ? reader.readSizedString() : null,
                    reader.readBoolean() ? new DisplayData(reader) : null,
                    reader.readVarIntList(BinaryReader::readSizedString),
                    reader.readVarIntList(Requirement::new));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeBoolean(parentIdentifier != null);
            if (parentIdentifier != null) writer.writeSizedString(parentIdentifier);
            writer.writeBoolean(displayData != null);
            if (displayData != null) writer.write(displayData);
            writer.writeVarIntList(criteria, BinaryWriter::writeSizedString);
            writer.writeVarIntList(requirements, BinaryWriter::write);
        }
    }

    public record Requirement(@NotNull List<String> requirements) implements Writeable {
        public Requirement {
            requirements = List.copyOf(requirements);
        }

        public Requirement(BinaryReader reader) {
            this(reader.readVarIntList(BinaryReader::readSizedString));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarIntList(requirements, BinaryWriter::writeSizedString);
        }
    }

    public record DisplayData(@NotNull Component title, @NotNull Component description,
                              @NotNull ItemStack icon, @NotNull FrameType frameType,
                              int flags, @Nullable String backgroundTexture,
                              float x, float y) implements Writeable {
        public DisplayData(BinaryReader reader) {
            this(read(reader));
        }

        private DisplayData(DisplayData displayData) {
            this(displayData.title, displayData.description,
                    displayData.icon, displayData.frameType,
                    displayData.flags, displayData.backgroundTexture,
                    displayData.x, displayData.y);
        }

        private static DisplayData read(BinaryReader reader) {
            var title = reader.readComponent();
            var description = reader.readComponent();
            var icon = reader.readItemStack();
            var frameType = FrameType.values()[reader.readVarInt()];
            var flags = reader.readInt();
            var backgroundTexture = (flags & 0x1) != 0 ? reader.readSizedString() : null;
            var x = reader.readFloat();
            var y = reader.readFloat();
            return new DisplayData(title, description,
                    icon, frameType,
                    flags, backgroundTexture,
                    x, y);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeComponent(title);
            writer.writeComponent(description);
            writer.writeItemStack(icon);
            writer.writeVarInt(frameType.ordinal());
            writer.writeInt(flags);
            if ((flags & 0x1) != 0) {
                assert backgroundTexture != null;
                writer.writeSizedString(backgroundTexture);
            }
            writer.writeFloat(x);
            writer.writeFloat(y);
        }
    }

    public record ProgressMapping(@NotNull String key, @NotNull AdvancementProgress progress) implements Writeable {
        public ProgressMapping(BinaryReader reader) {
            this(reader.readSizedString(), new AdvancementProgress(reader));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(key);
            writer.write(progress);
        }
    }

    public record AdvancementProgress(@NotNull List<Criteria> criteria) implements Writeable {
        public AdvancementProgress {
            criteria = List.copyOf(criteria);
        }

        public AdvancementProgress(BinaryReader reader) {
            this(reader.readVarIntList(Criteria::new));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarIntList(criteria, BinaryWriter::write);
        }
    }

    public record Criteria(@NotNull String criterionIdentifier,
                           @NotNull CriterionProgress criterionProgress) implements Writeable {
        public Criteria(BinaryReader reader) {
            this(reader.readSizedString(), new CriterionProgress(reader));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(criterionIdentifier);
            writer.write(criterionProgress);
        }
    }

    public record CriterionProgress(@Nullable Long dateOfAchieving) implements Writeable {
        public CriterionProgress(BinaryReader reader) {
            this(reader.readBoolean() ? reader.readLong() : null);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeBoolean(dateOfAchieving != null);
            if (dateOfAchieving != null) writer.writeLong(dateOfAchieving);
        }
    }
}