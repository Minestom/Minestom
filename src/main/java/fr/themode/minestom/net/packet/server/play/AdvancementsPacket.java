package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

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
        return 0x57;
    }

    public enum FrameType {
        TASK, CHALLENGE, GOAL;
    }

    public static class AdvancementMapping {

        public String key;
        public Advancement value;

        private void write(PacketWriter writer) {
            writer.writeSizedString(key);
            value.write(writer);
        }

    }

    public static class Advancement {

        public boolean hasParent;
        public String identifier;
        public boolean hasDisplay;
        public DisplayData displayData;
        public String[] criterions;
        public Requirement[] requirements;

        private void write(PacketWriter writer) {
            writer.writeBoolean(hasParent);
            if (identifier != null) {
                writer.writeSizedString(identifier);
            }

            writer.writeBoolean(hasDisplay);
            if (hasDisplay) {
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
        public String title;
        public String description;
        public ItemStack icon;
        public FrameType frameType;
        public int flags;
        public String backgroundTexture;
        public float x;
        public float y;

        private void write(PacketWriter writer) {
            writer.writeSizedString(Chat.rawText(title));
            writer.writeSizedString(Chat.rawText(description));
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
