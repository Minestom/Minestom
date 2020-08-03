package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class AdvancementsPacket implements ServerPacket {

    public boolean resetAdvancements;
    public AdvancementMapping[] advancementMappings;
    public String[] identifiersToRemove;
    public ProgressMapping[] progressMappings;

    @Override
    public void write(PacketWriter writer) {
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

    /**
     * Describes the frame around the Advancement.
     * Also describes the type of advancement it is for "toast" notifications.
     */
    public enum FrameType {
        /**
         * A simple rounded square as the frame.
         */
        TASK,
        /**
         * A spike in all 8 directions as the frame.
         */
        CHALLENGE,
        /**
         * A square with a outward rounded edge on the top and bottom as the frame.
         */
        GOAL
    }

    /**
     * AdvancementMapping maps the namespaced ID to the Advancement.
     */
    public static class AdvancementMapping {

        public String key;
        public Advancement value;

        private void write(PacketWriter writer) {
            writer.writeSizedString(key);
            value.write(writer);
        }

    }

    public static class Advancement {
        public String parentIdentifier;
        public DisplayData displayData;
        public String[] criterions;
        public Requirement[] requirements;

        private void write(PacketWriter writer) {
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

    public static class DisplayData {
        public ColoredText title;
        public ColoredText description;
        public ItemStack icon;
        public FrameType frameType;
        public int flags;
        public String backgroundTexture;
        public float x;
        public float y;

        private void write(PacketWriter writer) {
            writer.writeSizedString(title.toString());
            writer.writeSizedString(description.toString());
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

    public static class Requirement {

        public String[] requirements;

        private void write(PacketWriter writer) {
            writer.writeVarInt(requirements.length);
            for (String requirement : requirements) {
                writer.writeSizedString(requirement);
            }
        }
    }

    public static class ProgressMapping {
        public String key;
        public AdvancementProgress value;

        private void write(PacketWriter writer) {
            writer.writeSizedString(key);
            value.write(writer);
        }
    }

    public static class AdvancementProgress {
        public Criteria[] criteria;

        private void write(PacketWriter writer) {
            writer.writeVarInt(criteria.length);
            for (Criteria criterion : criteria) {
                criterion.write(writer);
            }
        }
    }

    public static class Criteria {
        public String criterionIdentifier;
        public CriterionProgress criterionProgress;

        private void write(PacketWriter writer) {
            writer.writeSizedString(criterionIdentifier);
            criterionProgress.write(writer);
        }
    }

    public static class CriterionProgress {
        public boolean achieved;
        public long dateOfAchieving;

        private void write(PacketWriter writer) {
            writer.writeBoolean(achieved);
            if (dateOfAchieving != 0)
                writer.writeLong(dateOfAchieving);
        }

    }

}
