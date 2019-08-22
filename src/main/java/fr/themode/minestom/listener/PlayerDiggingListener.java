package fr.themode.minestom.listener;

import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.StartDiggingEvent;
import fr.themode.minestom.instance.CustomBlock;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.net.packet.client.play.ClientPlayerDiggingPacket;
import fr.themode.minestom.net.packet.server.play.EntityEffectPacket;
import fr.themode.minestom.net.packet.server.play.RemoveEntityEffectPacket;
import fr.themode.minestom.utils.BlockPosition;

public class PlayerDiggingListener {

    public static void playerDiggingListener(ClientPlayerDiggingPacket packet, Player player) {
        ClientPlayerDiggingPacket.Status status = packet.status;
        BlockPosition blockPosition = packet.blockPosition;
        switch (status) {
            case STARTED_DIGGING:
                if (player.getGameMode() == GameMode.CREATIVE) {
                    Instance instance = player.getInstance();
                    if (instance != null) {
                        instance.setBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), (short) 0);
                    }
                } else if (player.getGameMode() == GameMode.SURVIVAL) {
                    Instance instance = player.getInstance();
                    if (instance != null) {
                        CustomBlock customBlock = instance.getCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                        if (customBlock != null) {
                            StartDiggingEvent startDiggingEvent = new StartDiggingEvent(customBlock);
                            player.callEvent(StartDiggingEvent.class, startDiggingEvent);
                            if (!startDiggingEvent.isCancelled()) {
                                player.refreshTargetBlock(customBlock, blockPosition);
                            }
                            addEffect(player);
                        } else {
                            player.resetTargetBlock();
                            removeEffect(player);
                        }
                    }
                }
                break;
            case CANCELLED_DIGGING:
                player.sendBlockBreakAnimation(blockPosition, (byte) -1);
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
                        short blockId = instance.getBlockId(blockPosition);
                        instance.breakBlock(player, blockPosition, blockId);
                    }
                }
                break;
            case UPDATE_ITEM_STATE:
                // TODO check if is updatable item
                //player.refreshActiveHand(false, false, false);
                //player.sendPacketToViewers(player.getMetadataPacket());
                break;
        }
    }

    private static void addEffect(Player player) {
        EntityEffectPacket entityEffectPacket = new EntityEffectPacket();
        entityEffectPacket.entityId = player.getEntityId();
        entityEffectPacket.effectId = 4;
        entityEffectPacket.amplifier = -1;
        entityEffectPacket.duration = 0;
        entityEffectPacket.flags = 0;
        player.getPlayerConnection().sendPacket(entityEffectPacket);
    }

    private static void removeEffect(Player player) {
        RemoveEntityEffectPacket removeEntityEffectPacket = new RemoveEntityEffectPacket();
        removeEntityEffectPacket.entityId = player.getEntityId();
        removeEntityEffectPacket.effectId = 4;
        player.getPlayerConnection().sendPacket(removeEntityEffectPacket);
    }

}
