package fr.themode.demo.map;

import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.type.EntityItemFrame;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.MapMeta;

public class TestItemFrame implements CommandProcessor {
    @Override
    public String getCommandName() {
        return "itemframe";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean process(CommandSender sender, String command, String[] args) {
        Player player = (Player)sender;
        EntityItemFrame frame = new EntityItemFrame(player.getPosition(), EntityItemFrame.ItemFrameOrientation.SOUTH);
        ItemStack map = new ItemStack(Material.FILLED_MAP, (byte) 1);
        map.setItemMeta(new MapMeta(MapAnimationDemo.MAP_ID));
        frame.setItemStack(map);
        frame.setInstance(player.getInstance());
        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }
}
