package net.minestom.server.entity.type.other;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EntityEndCrystal extends ObjectEntity {

    private BlockPosition beamTarget;
    private boolean showBottom;

    public EntityEndCrystal(@NotNull Position spawnPosition) {
        super(EntityType.END_CRYSTAL, spawnPosition);

        setBoundingBox(2f, 2f, 2f);
    }

    @NotNull
    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 7);
            fillMetadataIndex(packet, 8);
        };
    }

    @Override
    protected void fillMetadataIndex(@NotNull BinaryWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 7) {
            final boolean hasTarget = beamTarget != null;

            packet.writeByte((byte) 7);
            packet.writeByte(METADATA_OPTPOSITION);
            packet.writeBoolean(hasTarget);
            if (hasTarget) {
                packet.writeBlockPosition(beamTarget);
            }
        } else if (index == 8) {
            packet.writeByte((byte) 8);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(showBottom);
        }
    }

    @Nullable
    public BlockPosition getBeamTarget() {
        return beamTarget;
    }

    public void setBeamTarget(@Nullable BlockPosition beamTarget) {
        this.beamTarget = beamTarget;
        sendMetadataIndex(7);
    }

    public boolean showBottom() {
        return showBottom;
    }

    public void setShowBottom(boolean showBottom) {
        this.showBottom = showBottom;
        sendMetadataIndex(8);
    }

    @Override
    public int getObjectData() {
        return 0;
    }
}
