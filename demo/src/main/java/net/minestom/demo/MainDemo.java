package net.minestom.demo;

import net.minestom.demo.commands.GamemodeCommand;
import net.minestom.demo.commands.SaveCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;

public class MainDemo {

    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        MinecraftServer.getCommandManager().register(new GamemodeCommand());
        MinecraftServer.getCommandManager().register(new SaveCommand());

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        DimensionType worldDimension = DimensionType.builder(NamespaceID.from("minestom:fullbright"))
                .ambientLight(2.0f)
                .build();
        MinecraftServer.getDimensionTypeManager().addDimension(worldDimension);

        // Create the instance
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer(worldDimension);
        // Set the ChunkGenerator
        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            player.setPermissionLevel(2);
            event.setSpawningInstance(instanceContainer);
            player.setGameMode(GameMode.CREATIVE);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        globalEventHandler.addListener(PlayerUseItemEvent.class, event -> {
            var gunSpread = 0.0f; // Debug, so it shoots straight

            // Get bullet spawn location
            Pos bulletSpawnLocation = getBulletSpawnLocation(event.getPlayer());

            PlayerProjectile bulletEntity = new PlayerProjectile(Entity.getEntity(event.getPlayer().getUuid()), EntityType.SNOWBALL);
            bulletEntity.setInstance(instanceContainer,bulletSpawnLocation);

            // This does basically nothing if gunspread is nothing
            // So it will always shoot straight
            var originalPitch = event.getPlayer().getPosition().pitch();
            var originalYaw   = event.getPlayer().getPosition().yaw();

            Vec velocityVec   = event.getPlayer().getPosition()
                    .withPitch(originalPitch) // Apply Spread
                    .withYaw(originalYaw) // Apply Spread
                    .direction();

            var bulletSpeed = 200.0;
            bulletEntity.setVelocity(velocityVec.mul(bulletSpeed));
        });

        globalEventHandler.addListener(ProjectileCollideWithBlockEvent.class, event -> {
            System.out.println("BLOCK COLLISION");
            System.out.println(event.getBlock());
            System.out.println(event.getCollisionPosition());
            System.out.println("");
            // This long line is to display the "block breaking" particles
            // as seen in the video
            // shortened to one line for easier readability
            event.getEntity().sendPacketToViewers(ParticleCreator.createParticlePacket(Particle.BLOCK,false,event.getCollisionPosition().x(),event.getCollisionPosition().y(),event.getCollisionPosition().z(),0.3f,0.3f,0.3f,0f,25, binaryWriter -> {binaryWriter.writeVarInt(event.getBlock().stateId());}));
            event.getEntity().remove();
        });

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }

    private static Pos getBulletSpawnLocation(Player evPlayer) {
        Pos playerEventLocation = evPlayer.getPosition();
        Pos returnPosition = new Pos(playerEventLocation.x(),playerEventLocation.y()+evPlayer.getEyeHeight()-0.125,playerEventLocation.z());
        /* This offsets the spawn location of the entity so that it doesn't clip in the player head and trigger a self collision*/
        return returnPosition.add(evPlayer.getPosition().direction().x()*0.3,evPlayer.getPosition().direction().y()*0.3,evPlayer.getPosition().direction().z()*0.3);
    }
}