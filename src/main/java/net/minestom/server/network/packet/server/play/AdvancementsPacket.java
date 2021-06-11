package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class AdvancementsPacket implements ComponentHoldingServerPacket {

    public boolean resetAdvancements;
    public AdvancementMapping[] advancementMappings = new AdvancementMapping[0];
    public String[] identifiersToRemove = new String[0];
    public ProgressMapping[] progressMappings = new ProgressMapping[0];

    public AdvancementsPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(resetAdvancements);

        writer.writeVarInt(advancementMappings.length);
        for (AdvancementMapping advancementMapping : advancementMappings) {
            advancementMapping.write(writer);
        }
        writer.writeStringArray(identifiersToRemove);

        writer.writeVarInt(progressMappings.length);
        for (ProgressMapping progressMapping : progressMappings) {
            progressMapping.write(writer);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        resetAdvancements = reader.readBoolean();

        int mappingCount = reader.readVarInt();
        advancementMappings = new AdvancementMapping[mappingCount];
        for (int i = 0; i < mappingCount; i++) {
            advancementMappings[i] = new AdvancementMapping();
            advancementMappings[i].read(reader);
        }

        identifiersToRemove = reader.readSizedStringArray(Integer.MAX_VALUE);

        int progressCount = reader.readVarInt();
        progressMappings = new ProgressMapping[progressCount];
        for (int i = 0; i < progressCount; i++) {
            progressMappings[i] = new ProgressMapping();
            progressMappings[i].read(reader);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ADVANCEMENTS;
    }

    @Override
    public @NotNull Collection<Component> components() {
        List<Component> components = new ArrayList<>();
        for (AdvancementMapping advancementMapping : advancementMappings) {
            components.add(advancementMapping.value.displayData.title);
            components.add(advancementMapping.value.displayData.description);
        }
        return components;
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        AdvancementsPacket packet = new AdvancementsPacket();
        packet.resetAdvancements = this.resetAdvancements;
        packet.advancementMappings = Arrays.copyOf(this.advancementMappings, this.advancementMappings.length);
        packet.identifiersToRemove = Arrays.copyOf(this.identifiersToRemove, this.identifiersToRemove.length);
        packet.progressMappings = Arrays.copyOf(this.progressMappings, this.progressMappings.length);

        for (AdvancementMapping advancementMapping : packet.advancementMappings) {
            advancementMapping.value.displayData.title = operator.apply(advancementMapping.value.displayData.title);
            advancementMapping.value.displayData.description = operator.apply(advancementMapping.value.displayData.title);
        }

        return packet;
    }

    /**
     * AdvancementMapping maps the namespaced ID to the Advancement.
     */
    public static class AdvancementMapping implements Writeable, Readable {

        public String key;
        public Advancement value;

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(key);
            value.write(writer);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            key = reader.readSizedString();
            value = new Advancement();
            value.read(reader);
        }
    }

    public static class Advancement implements Writeable, Readable {
        public String parentIdentifier;
        public DisplayData displayData;
        public String[] criterions = new String[0];
        public Requirement[] requirements = new Requirement[0];

        @Override
        public void write(@NotNull BinaryWriter writer) {
            // hasParent
            writer.writeBoolean(parentIdentifier != null);
            if (parentIdentifier != null) {
                writer.writeSizedString(parentIdentifier);
            }

            // hasDisplay
            writer.writeBoolean(displayData != null);
            if (displayData != null) {
                displayData.write(writer);
            }

            writer.writeStringArray(criterions);


            writer.writeVarInt(requirements.length);
            for (Requirement requirement : requirements) {
                requirement.write(writer);
            }
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            boolean hasParent = reader.readBoolean();
            if (hasParent) {
                parentIdentifier = reader.readSizedString();
            } else {
                parentIdentifier = null;
            }

            boolean hasDisplay = reader.readBoolean();
            if (hasDisplay) {
                displayData = new DisplayData();
                displayData.read(reader);
            } else {
                displayData = null;
            }

            criterions = reader.readSizedStringArray();

            int requirementCount = reader.readVarInt();
            requirements = new Requirement[requirementCount];
            for (int i = 0; i < requirementCount; i++) {
                requirements[i] = new Requirement();
                requirements[i].read(reader);
            }
        }
    }

    public static class DisplayData implements Writeable, Readable {
        public Component title = Component.empty(); // Only text
        public Component description = Component.empty(); // Only text
        public ItemStack icon = ItemStack.AIR;
        public FrameType frameType = FrameType.TASK;
        public int flags;
        public String backgroundTexture = "";
        public float x;
        public float y;

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeComponent(title);
            writer.writeComponent(description);
            writer.writeItemStack(icon);
            writer.writeVarInt(frameType.ordinal());
            writer.writeInt(flags);
            if ((flags & 0x1) != 0) {
                writer.writeSizedString(backgroundTexture);
            }
            writer.writeFloat(x);
            writer.writeFloat(y);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            title = reader.readComponent();
            description = reader.readComponent();
            icon = reader.readItemStack();
            frameType = FrameType.values()[reader.readVarInt()];
            flags = reader.readInt();
            if ((flags & 0x1) != 0) {
                backgroundTexture = reader.readSizedString();
            } else {
                backgroundTexture = null;
            }
            x = reader.readFloat();
            y = reader.readFloat();
        }
    }

    public static class Requirement implements Writeable, Readable {

        public String[] requirements = new String[0];

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeStringArray(requirements);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            requirements = reader.readSizedStringArray(Integer.MAX_VALUE);
        }
    }

    public static class ProgressMapping implements Writeable, Readable {
        public String key = "";
        public AdvancementProgress value = new AdvancementProgress();

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(key);
            value.write(writer);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            key = reader.readSizedString();
            value = new AdvancementProgress();
            value.read(reader);
        }
    }

    public static class AdvancementProgress implements Writeable, Readable {
        public Criteria[] criteria = new Criteria[0];

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(criteria.length);
            for (Criteria criterion : criteria) {
                criterion.write(writer);
            }
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            int count = reader.readVarInt();
            criteria = new Criteria[count];
            for (int i = 0; i < count; i++) {
                criteria[i] = new Criteria();
                criteria[i].read(reader);
            }
        }
    }

    public static class Criteria implements Writeable, Readable {
        public String criterionIdentifier = "";
        public CriterionProgress criterionProgress = new CriterionProgress();

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(criterionIdentifier);
            criterionProgress.write(writer);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            criterionIdentifier = reader.readSizedString();
            criterionProgress = new CriterionProgress();
            criterionProgress.read(reader);
        }
    }

    public static class CriterionProgress implements Writeable, Readable {
        public boolean achieved;
        public long dateOfAchieving;

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeBoolean(achieved);
            if (achieved)
                writer.writeLong(dateOfAchieving);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            achieved = reader.readBoolean();
            if (achieved) {
                dateOfAchieving = reader.readLong();
            }
        }
    }

}