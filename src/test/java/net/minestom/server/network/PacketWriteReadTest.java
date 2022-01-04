package net.minestom.server.network;

import com.google.gson.JsonObject;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.handshake.HandshakePacket;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.handshake.ResponsePacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.packet.server.status.PongPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.Ingredient;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Ensures that packet can be written and read correctly.
 */
public class PacketWriteReadTest {
    private static final List<ServerPacket> SERVER_PACKETS = new ArrayList<>();
    private static final List<ClientPacket> CLIENT_PACKETS = new ArrayList<>();

    private static final Component COMPONENT = Component.text("Hey");
    private static final Vec VEC = new Vec(5, 5, 5);

    @BeforeAll
    public static void setupServer() {
        // Handshake
        SERVER_PACKETS.add(new ResponsePacket(new JsonObject().toString()));
        // Status
        SERVER_PACKETS.add(new PongPacket(5));
        // Login
        //SERVER_PACKETS.add(new EncryptionRequestPacket("server", generateByteArray(16), generateByteArray(16)));
        SERVER_PACKETS.add(new LoginDisconnectPacket(COMPONENT));
        //SERVER_PACKETS.add(new LoginPluginRequestPacket(5, "id", generateByteArray(16)));
        SERVER_PACKETS.add(new LoginSuccessPacket(UUID.randomUUID(), "TheMode911"));
        SERVER_PACKETS.add(new SetCompressionPacket(256));
        // Play
        SERVER_PACKETS.add(new AcknowledgePlayerDiggingPacket(VEC, 5, ClientPlayerDiggingPacket.Status.STARTED_DIGGING, true));
        SERVER_PACKETS.add(new ActionBarPacket(COMPONENT));
        SERVER_PACKETS.add(new AttachEntityPacket(5, 10));
        SERVER_PACKETS.add(new BlockActionPacket(VEC, (byte) 5, (byte) 5, 5));
        SERVER_PACKETS.add(new BlockBreakAnimationPacket(5, VEC, (byte) 5));
        SERVER_PACKETS.add(new BlockChangePacket(VEC, 0));
        SERVER_PACKETS.add(new BlockEntityDataPacket(VEC, 5, NBT.Compound(Map.of("key", NBT.String("value")))));
        SERVER_PACKETS.add(new BossBarPacket(UUID.randomUUID(), new BossBarPacket.AddAction(COMPONENT, 5f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS, (byte) 2)));
        SERVER_PACKETS.add(new BossBarPacket(UUID.randomUUID(), new BossBarPacket.RemoveAction()));
        SERVER_PACKETS.add(new BossBarPacket(UUID.randomUUID(), new BossBarPacket.UpdateHealthAction(5f)));
        SERVER_PACKETS.add(new BossBarPacket(UUID.randomUUID(), new BossBarPacket.UpdateTitleAction(COMPONENT)));
        SERVER_PACKETS.add(new BossBarPacket(UUID.randomUUID(), new BossBarPacket.UpdateStyleAction(BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)));
        SERVER_PACKETS.add(new BossBarPacket(UUID.randomUUID(), new BossBarPacket.UpdateFlagsAction((byte) 5)));
        SERVER_PACKETS.add(new CameraPacket(5));
        SERVER_PACKETS.add(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.RAIN_LEVEL_CHANGE, 2));
        SERVER_PACKETS.add(new ChatMessagePacket(COMPONENT, ChatPosition.CHAT, UUID.randomUUID()));
        SERVER_PACKETS.add(new ClearTitlesPacket(false));
        SERVER_PACKETS.add(new CloseWindowPacket((byte) 2));
        SERVER_PACKETS.add(new CollectItemPacket(5, 5, 5));
        SERVER_PACKETS.add(new CraftRecipeResponse((byte) 2, "recipe"));
        SERVER_PACKETS.add(new DeathCombatEventPacket(5, 5, COMPONENT));
        SERVER_PACKETS.add(new DeclareRecipesPacket(
                List.of(new DeclareRecipesPacket.DeclaredShapelessCraftingRecipe(
                            "minecraft:sticks",
                            "sticks",
                            List.of(new Ingredient(List.of(ItemStack.of(Material.OAK_PLANKS)))),
                            ItemStack.of(Material.STICK)
                        ),
                        new DeclareRecipesPacket.DeclaredShapedCraftingRecipe(
                            "minecraft:torch",
                            1,
                            2,
                            "",
                            List.of(new Ingredient(List.of(ItemStack.of(Material.COAL))),
                                    new Ingredient(List.of(ItemStack.of(Material.STICK)))),
                            ItemStack.of(Material.TORCH)
                        ))));

        SERVER_PACKETS.add(new DestroyEntitiesPacket(List.of(5, 5, 5)));
        SERVER_PACKETS.add(new DisconnectPacket(COMPONENT));
        SERVER_PACKETS.add(new DisplayScoreboardPacket((byte) 5, "scoreboard"));
        SERVER_PACKETS.add(new EffectPacket(5, VEC, 5, false));
        SERVER_PACKETS.add(new EndCombatEventPacket(5, 5));
        SERVER_PACKETS.add(new EnterCombatEventPacket());
        SERVER_PACKETS.add(new EntityAnimationPacket(5, EntityAnimationPacket.Animation.TAKE_DAMAGE));
        SERVER_PACKETS.add(new EntityEquipmentPacket(6, Map.of(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.DIAMOND_SWORD))));
        SERVER_PACKETS.add(new EntityHeadLookPacket(5, 90f));
        SERVER_PACKETS.add(new EntityMetaDataPacket(5, List.of()));
        SERVER_PACKETS.add(new EntityPositionAndRotationPacket(5, (short) 0, (short) 0, (short) 0, 45f, 45f, false));
        SERVER_PACKETS.add(new EntityPositionPacket(5, (short) 0, (short) 0, (short) 0, true));
        SERVER_PACKETS.add(new EntityPropertiesPacket(5, List.of()));
        SERVER_PACKETS.add(new EntityRotationPacket(5, 45f, 45f, false));

        SERVER_PACKETS.add(new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_DISPLAY_NAME,
                new PlayerInfoPacket.UpdateDisplayName(UUID.randomUUID(), COMPONENT)));
        SERVER_PACKETS.add(new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_DISPLAY_NAME,
                new PlayerInfoPacket.UpdateDisplayName(UUID.randomUUID(), (Component) null)));
        SERVER_PACKETS.add(new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_GAMEMODE,
                new PlayerInfoPacket.UpdateGameMode(UUID.randomUUID(), GameMode.CREATIVE)));
        SERVER_PACKETS.add(new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_LATENCY,
                new PlayerInfoPacket.UpdateLatency(UUID.randomUUID(), 5)));
        SERVER_PACKETS.add(new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER,
                new PlayerInfoPacket.AddPlayer(UUID.randomUUID(), "TheMode911", List.of(new PlayerInfoPacket.AddPlayer.Property("name", "value")), GameMode.CREATIVE, 5, COMPONENT)));
        SERVER_PACKETS.add(new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER, new PlayerInfoPacket.RemovePlayer(UUID.randomUUID())));

        //SERVER_PACKETS.add(new MultiBlockChangePacket(5,5,5,true, new long[]{0,5,543534,1321}));
    }

    @BeforeAll
    public static void setupClient() {
        CLIENT_PACKETS.add(new HandshakePacket(755, "localhost", 25565, 2));
    }

    @Test
    public void serverTest() {
        SERVER_PACKETS.forEach(PacketWriteReadTest::testPacket);
    }

    @Test
    public void clientTest() {
        CLIENT_PACKETS.forEach(PacketWriteReadTest::testPacket);
    }

    private static void testPacket(Writeable writeable) {
        try {
            BinaryWriter writer = new BinaryWriter();
            writeable.write(writer);
            var readerConstructor = writeable.getClass().getConstructor(BinaryReader.class);

            BinaryReader reader = new BinaryReader(writer.toByteArray());
            var createdPacket = readerConstructor.newInstance(reader);
            assertEquals(writeable, createdPacket);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            fail(writeable.toString(), e);
        }
    }

    private static byte[] generateByteArray(int size) {
        byte[] array = new byte[size];
        ThreadLocalRandom.current().nextBytes(array);
        return array;
    }
}
