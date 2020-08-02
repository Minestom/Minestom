package fr.themode.demo.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.MapMeta;
import net.minestom.server.map.MapColors;
import net.minestom.server.network.packet.server.play.MapDataPacket;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.time.TimeUnit;

public class MapAnimationDemo {

    public static final int MAP_ID = 1;

    public static void init() {
        SchedulerManager scheduler = MinecraftServer.getSchedulerManager();
        scheduler.buildTask(MapAnimationDemo::tick).repeat(16, TimeUnit.MILLISECOND).schedule();

        MinecraftServer.getConnectionManager().addPlayerInitialization(player -> {
            player.addEventCallback(PlayerSpawnEvent.class, event -> {
                ItemStack map = new ItemStack(Material.FILLED_MAP, (byte) 1);
                map.setItemMeta(new MapMeta(MAP_ID));
                player.getInventory().addItemStack(map);
            });
        });
    }

    public static void tick() {
        MapDataPacket mapDataPacket = new MapDataPacket();
        mapDataPacket.mapId = MAP_ID;
        mapDataPacket.columns = 127;
        mapDataPacket.rows = 127;
        mapDataPacket.icons = new MapDataPacket.Icon[0];
        mapDataPacket.x = 0;
        mapDataPacket.z = 0;
        mapDataPacket.scale = 0;
        mapDataPacket.locked = true;
        mapDataPacket.trackingPosition = true;
        byte[] colors = new byte[128*128];
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                int r = (int) (Math.random() * MapColors.values().length);
                MapColors baseColor = MapColors.values()[r];
                int m = (int) (Math.random() * 4);
                byte colorID = (byte) ((baseColor.ordinal() << 2) + m);
                colors[x+z*128] = colorID;
            }
        }
        mapDataPacket.data = colors;
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
            p.sendPacketToViewersAndSelf(mapDataPacket);
        });
    }
}
