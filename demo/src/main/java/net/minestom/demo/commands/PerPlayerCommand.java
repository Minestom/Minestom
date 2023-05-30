package net.minestom.demo.commands;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.demo.PerPlayer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.network.UuidProvider;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.mojang.MojangUtils;

import java.util.ArrayList;
import java.util.UUID;

import static net.minestom.server.command.builder.arguments.ArgumentType.Boolean;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class PerPlayerCommand extends Command {

    public PerPlayerCommand() {
        super("perplayer", "pp");


        // SKIN
        addSyntax((sender, context) -> {
            if (!(sender instanceof PerPlayer))
                return;
            PerPlayer player = (PerPlayer) sender;

            EntityFinder finder = context.get("target");
            PerPlayer receiver = (PerPlayer) finder.findFirstPlayer(sender);
            assert receiver != null;

            String targetSkin = context.get("skinname");
            player.setSkin(PlayerSkin.fromUsername(targetSkin), receiver);

        }, Entity("target").singleEntity(true).onlyPlayers(true), Literal("skin"), String("skinname"));

        // username
        addSyntax((sender, context) -> {
            if (!(sender instanceof PerPlayer))
                return;
            PerPlayer player = (PerPlayer) sender;

            EntityFinder finder = context.get("target");
            PerPlayer receiver = (PerPlayer) finder.findFirstPlayer(sender);
            assert receiver != null;

            String targetSkin = context.get("username");
            player.setUsername(targetSkin, receiver);

        }, Entity("target").singleEntity(true).onlyPlayers(true), Literal("name"), String("username"));

        // uuid
        addSyntax((sender, context) -> {
            if (!(sender instanceof PerPlayer))
                return;
            PerPlayer player = (PerPlayer) sender;

            EntityFinder finder = context.get("target");
            PerPlayer receiver = (PerPlayer) finder.findFirstPlayer(sender);
            assert receiver != null;

            String username = context.get("username");
            final JsonObject jsonObject = MojangUtils.fromUsername(username);
            if (jsonObject == null) return;
            final String uuid = jsonObject.get("id").getAsString();
            String formattedUUID = uuid.substring(0, 8) + "-"
                    + uuid.substring(8, 12) + "-"
                    + uuid.substring(12, 16) + "-"
                    + uuid.substring(16, 20) + "-"
                    + uuid.substring(20);
            player.setUuid(UUID.fromString(formattedUUID), receiver);

        }, Entity("target").singleEntity(true).onlyPlayers(true), Literal("uuid"), String("username"));


        // DisplayName by Component
        addSyntax((sender, context) -> {
            if (!(sender instanceof PerPlayer))
                return;
            PerPlayer player = (PerPlayer) sender;

            EntityFinder finder = context.get("target");
            PerPlayer receiver = (PerPlayer) finder.findFirstPlayer(sender);
            assert receiver != null;

            player.setDisplayName(context.get("name"), receiver);

        }, Entity("target").singleEntity(true).onlyPlayers(true), Literal("displayname"), Component("name"));


        // DisplayName by Minimessage
        addSyntax((sender, context) -> {
            if (!(sender instanceof PerPlayer))
                return;
            PerPlayer player = (PerPlayer) sender;

            EntityFinder finder = context.get("target");
            PerPlayer receiver = (PerPlayer) finder.findFirstPlayer(sender);
            assert receiver != null;

            ArrayList<String> namelist = context.get("name");
            Component displayname = MiniMessage.miniMessage().deserialize(String.join(" ", namelist));
            player.setDisplayName(displayname, receiver);

        }, Entity("target").singleEntity(true).onlyPlayers(true), Literal("displayname"), StringArray("name"));


        // Gamemode
        addSyntax((sender, context) -> {
            if (!(sender instanceof PerPlayer))
                return;
            PerPlayer player = (PerPlayer) sender;

            EntityFinder finder = context.get("target");
            PerPlayer receiver = (PerPlayer) finder.findFirstPlayer(sender);
            assert receiver != null;

            GameMode gamemode = context.get("GameMode");

            player.setGameMode(gamemode, receiver);

        }, Entity("target").singleEntity(true).onlyPlayers(true), Literal("gamemode"), Enum("GameMode", GameMode.class));
    }
}
