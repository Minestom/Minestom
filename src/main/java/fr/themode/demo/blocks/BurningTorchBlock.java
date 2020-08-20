package fr.themode.demo.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;

/**
 * Custom block which burns entities that touch it
 */
public class BurningTorchBlock extends CustomBlock {
    public BurningTorchBlock() {
        super(Block.TORCH.getBlockId(), "torch_block");
    }

    @Override
    public void handleContact(Instance instance, BlockPosition position, Entity touching) {
        System.out.println("touching " + touching);
        if (touching instanceof LivingEntity) {
            ((LivingEntity) touching).damage(DamageType.GRAVITY, 0.1f);
        }
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {
        System.out.println(blockPosition);
    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        return false;
    }

    @Override
    public short getCustomBlockId() {
        return 3;
    }
}
