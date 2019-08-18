package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.CustomBlock;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.net.packet.server.play.EntityEffectPacket;
import fr.themode.minestom.net.packet.server.play.RemoveEntityEffectPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

public class ClientPlayerDiggingPacket implements ClientPlayPacket {

    public Status status;
    public Position position;
    public BlockFace blockFace;

    @Override
    public void process(Player player) {
        switch (status) {
            case STARTED_DIGGING:
                if (player.getGameMode() == GameMode.CREATIVE) {
                    Instance instance = player.getInstance();
                    if (instance != null) {
                        instance.setBlock(position.getX(), position.getY(), position.getZ(), (short) 0);
                    }
                } else if (player.getGameMode() == GameMode.SURVIVAL) {
                    Instance instance = player.getInstance();
                    if (instance != null) {
                        CustomBlock customBlock = instance.getCustomBlock(position.getX(), position.getY(), position.getZ());
                        if (customBlock != null) {
                            player.refreshTargetBlock(customBlock, position);
                            addEffect(player);
                        } else {
                            player.resetTargetBlock();
                            removeEffect(player);
                        }
                    }
                }
                break;
            case CANCELLED_DIGGING:
                player.sendBlockBreakAnimation(position, (byte) -1);
                player.resetTargetBlock();
                removeEffect(player);
                break;
            case FINISHED_DIGGING:
                if (player.getCustomBlockTarget() != null) {
                    player.resetTargetBlock();
                    removeEffect(player);
                } else {
                    Instance instance = player.getInstance();
                    if (instance != null) {
                        instance.setBlock(position.getX(), position.getY(), position.getZ(), (short) 0);
                    }
                }
                break;
        }
    }

    private void addEffect(Player player) {
        EntityEffectPacket entityEffectPacket = new EntityEffectPacket();
        entityEffectPacket.entityId = player.getEntityId();
        entityEffectPacket.effectId = 4;
        entityEffectPacket.amplifier = -1;
        entityEffectPacket.duration = 0;
        entityEffectPacket.flags = 0;
        player.getPlayerConnection().sendPacket(entityEffectPacket);
    }

    private void removeEffect(Player player) {
        RemoveEntityEffectPacket removeEntityEffectPacket = new RemoveEntityEffectPacket();
        removeEntityEffectPacket.entityId = player.getEntityId();
        removeEntityEffectPacket.effectId = 4;
        player.getPlayerConnection().sendPacket(removeEntityEffectPacket);
    }

    @Override
    public void read(Buffer buffer) {
        this.status = Status.values()[Utils.readVarInt(buffer)];
        this.position = Utils.readPosition(buffer);
        this.blockFace = BlockFace.values()[Utils.readVarInt(buffer)];
    }

    public enum Status {
        STARTED_DIGGING,
        CANCELLED_DIGGING,
        FINISHED_DIGGING,
        DROP_ITEM_STACK,
        DROP_ITEM,
        UPDATE_ITEM_STATE,
        SWAP_ITEM_HAND;
    }

    public enum BlockFace {
        BOTTOM,
        TOP,
        NORTH,
        SOUTH,
        WEST,
        EAST;
    }

}
