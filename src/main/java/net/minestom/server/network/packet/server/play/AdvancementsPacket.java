package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class AdvancementsPacket implements ComponentHoldingServerPacket {

    public boolean resetAdvancements;
    public AdvancementMapping[] advancementMappings;
    public String[] identifiersToRemove;
    public ProgressMapping[] progressMappings;

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
    public static class AdvancementMapping implements Writeable {

        public String key;
        public Advancement value;

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(key);
            value.write(writer);
        }

    }

    public static class Advancement implements Writeable {
        public String parentIdentifier;
        public DisplayData displayData;
        public String[] criterions;
        public Requirement[] requirements;

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
    }

    public static class DisplayData implements Writeable {
        public Component title; // Only text
        public Component description; // Only text
        public ItemStack icon;
        public FrameType frameType;
        public int flags;
        public String backgroundTexture;
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

    }

    public static class Requirement implements Writeable {

        public String[] requirements;

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(requirements.length);
            for (String requirement : requirements) {
                writer.writeSizedString(requirement);
            }
        }
    }

    public static class ProgressMapping implements Writeable {
        public String key;
        public AdvancementProgress value;

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(key);
            value.write(writer);
        }
    }

    public static class AdvancementProgress implements Writeable {
        public Criteria[] criteria;

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(criteria.length);
            for (Criteria criterion : criteria) {
                criterion.write(writer);
            }
        }
    }

    public static class Criteria implements Writeable {
        public String criterionIdentifier;
        public CriterionProgress criterionProgress;

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(criterionIdentifier);
            criterionProgress.write(writer);
        }

    }

    public static class CriterionProgress implements Writeable {
        public boolean achieved;
        public long dateOfAchieving;

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeBoolean(achieved);
            if (dateOfAchieving != 0)
                writer.writeLong(dateOfAchieving);
        }

    }

}