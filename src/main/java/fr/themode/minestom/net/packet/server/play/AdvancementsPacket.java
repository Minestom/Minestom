package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class AdvancementsPacket implements ServerPacket {

    public boolean resetAdvancements;
    public AdvancementMapping[] advancementMappings;
    public String[] identifiersToRemove;
    public ProgressMapping[] progressMappings;

    @Override
    public void write(Buffer buffer) {
        buffer.putBoolean(resetAdvancements);

        Utils.writeVarInt(buffer, advancementMappings.length);
        for (AdvancementMapping advancementMapping : advancementMappings) {
            advancementMapping.write(buffer);
        }

        Utils.writeVarInt(buffer, identifiersToRemove.length);
        for (String identifierToRemove : identifiersToRemove) {
            Utils.writeString(buffer, identifierToRemove);
        }

        Utils.writeVarInt(buffer, progressMappings.length);
        for (ProgressMapping progressMapping : progressMappings) {
            progressMapping.write(buffer);
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

        private void write(Buffer buffer) {
            Utils.writeString(buffer, key);
            value.write(buffer);
        }

    }

    public static class Advancement {

        public boolean hasParent;
        public String identifier;
        public boolean hasDisplay;
        public DisplayData displayData;
        public String[] criterions;
        public Requirement[] requirements;

        private void write(Buffer buffer) {
            buffer.putBoolean(hasParent);
            if (identifier != null) {
                Utils.writeString(buffer, identifier);
            }

            buffer.putBoolean(hasDisplay);
            if (hasDisplay) {
                displayData.write(buffer);
            }

            Utils.writeVarInt(buffer, criterions.length);
            for (String criterion : criterions) {
                Utils.writeString(buffer, criterion);
            }

            Utils.writeVarInt(buffer, requirements.length);
            for (Requirement requirement : requirements) {
                requirement.write(buffer);
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

        private void write(Buffer buffer) {
            Utils.writeString(buffer, Chat.rawText(title));
            Utils.writeString(buffer, Chat.rawText(description));
            Utils.writeItemStack(buffer, icon);
            Utils.writeVarInt(buffer, frameType.ordinal());
            buffer.putInt(flags);
            if ((flags & 0x1) != 0) {
                Utils.writeString(buffer, backgroundTexture);
            }
            buffer.putFloat(x);
            buffer.putFloat(y);
        }

    }

    public static class Requirement {

        public String[] requirements;

        private void write(Buffer buffer) {
            Utils.writeVarInt(buffer, requirements.length);
            for (String requirement : requirements) {
                Utils.writeString(buffer, requirement);
            }
        }
    }

    public static class ProgressMapping {
        public String key;
        public AdvancementProgress value;

        private void write(Buffer buffer) {
            Utils.writeString(buffer, key);
            value.write(buffer);
        }
    }

    public static class AdvancementProgress {
        public Criteria[] criteria;

        private void write(Buffer buffer) {
            Utils.writeVarInt(buffer, criteria.length);
            for (Criteria criterion : criteria) {
                criterion.write(buffer);
            }
        }
    }

    public static class Criteria {
        public String criterionIdentifier;
        public CriterionProgress criterionProgress;

        private void write(Buffer buffer) {
            Utils.writeString(buffer, criterionIdentifier);
            criterionProgress.write(buffer);
        }
    }

    public static class CriterionProgress {
        public boolean achieved;
        public long dateOfAchieving;

        private void write(Buffer buffer) {
            buffer.putBoolean(achieved);
            if (dateOfAchieving != 0)
                buffer.putLong(dateOfAchieving);
        }

    }

}
