package net.minestom.demo.feature.entities;

import net.kyori.adventure.text.Component;
import net.minestom.demo.core.Feature;
import net.minestom.demo.feature.entities.npc.PlayerEntity;
import net.minestom.server.ServerProcess;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.entity.metadata.golem.CopperGolemMeta;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.TrackedWaypointPacket;
import net.minestom.server.network.player.ResolvableProfile;
import net.minestom.server.utils.Either;

import java.util.List;
import java.util.function.Consumer;

/** Entity commands and a first-spawn NPC zoo with tracked waypoints. */
public final class EntitiesFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(
                new SummonCommand(),
                new RemoveCommand(),
                new HorseCommand(),
                new MinecartCommand(),
                new SetEntityType()
        );

        process.eventHandler().addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return;
            Player player = event.getPlayer();

            spawn(player, EntityType.HAPPY_GHAST, new Pos(10, 43, 5, 45, 0),
                    e -> e.setBodyEquipment(ItemStack.of(Material.GREEN_HARNESS)));

            spawn(player, EntityType.COPPER_GOLEM, new Pos(-10, 40, 5, -133, 0), e -> {
                e.setItemInMainHand(ItemStack.of(Material.STICK));
                ((CopperGolemMeta) e.getEntityMeta()).setState(CopperGolemMeta.State.GETTING_ITEM);
            });
            player.getInstance().setBlock(new Vec(-12, 40, 5),
                    Block.WEATHERED_COPPER_GOLEM_STATUE.withProperty("copper_golem_pose", "star"));

            var fakePlayer = new PlayerEntity();
            fakePlayer.setInstance(player.getInstance(), new Pos(-2.5, 40, 6.7, -163, 0));
            track(player, fakePlayer);

            var profile = new ResolvableProfile(new ResolvableProfile.Partial("Minestom", null, List.of()));
            spawn(player, EntityType.MANNEQUIN, new Pos(-4, 40, 6, -131, 0), e -> {
                e.set(DataComponents.CUSTOM_NAME, Component.text("Minestom"));
                e.setItemInMainHand(ItemStack.of(Material.PLAYER_HEAD).with(DataComponents.PROFILE, profile));
                var meta = (MannequinMeta) e.getEntityMeta();
                meta.setCustomNameVisible(true);
                meta.setProfile(profile);
                meta.setImmovable(true);
                meta.setDescription(Component.text("npc"));
            });
        });
    }

    private static void spawn(Player player, EntityType type, Pos pos, Consumer<LivingEntity> configure) {
        var entity = new LivingEntity(type);
        entity.setNoGravity(true);
        configure.accept(entity);
        entity.setInstance(player.getInstance(), pos);
        track(player, entity);
    }

    private static void track(Player player, Entity entity) {
        player.sendPacket(new TrackedWaypointPacket(TrackedWaypointPacket.Operation.TRACK,
                new TrackedWaypointPacket.Waypoint(
                        Either.left(entity.getUuid()),
                        TrackedWaypointPacket.Icon.DEFAULT,
                        new TrackedWaypointPacket.Target.Vec3i(entity.getPosition()))));
    }
}
