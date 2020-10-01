package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityCreeper extends EntityCreature implements Monster {

    private CreeperState creeperState;
    private boolean charged;
    private boolean ignited;

    public EntityCreeper(Position spawnPosition) {
        super(EntityType.CREEPER, spawnPosition);
        setBoundingBox(0.6f, 1.7f, 0.6f);
    }

    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 15);
        };
    }

    @Override
    protected void fillMetadataIndex(BinaryWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 15) {
            packet.writeByte((byte) 15);
            packet.writeByte(METADATA_VARINT);
            packet.writeVarInt(creeperState.getState());
        } else if (index == 16) {
            packet.writeByte((byte) 16);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(charged);
        } else if (index == 17) {
            packet.writeByte((byte) 17);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(ignited);
        }
    }

    public CreeperState getCreeperState() {
        return creeperState;
    }

    public void setCreeperState(CreeperState creeperState) {
        this.creeperState = creeperState;
        sendMetadataIndex(15);
    }

    public boolean isCharged() {
        return charged;
    }

    public void setCharged(boolean charged) {
        this.charged = charged;
        sendMetadataIndex(16);
    }

    public boolean isIgnited() {
        return ignited;
    }

    public void setIgnited(boolean ignited) {
        this.ignited = ignited;
        sendMetadataIndex(17);
    }

    public enum CreeperState {
        IDLE(-1),
        FUSE(1);

        private final int state;

        CreeperState(int state) {
            this.state = state;
        }

        private int getState() {
            return state;
        }
    }
}
