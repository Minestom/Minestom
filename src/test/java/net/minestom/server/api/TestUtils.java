package net.minestom.server.api;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.SocketAddress;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {
    public static void waitUntilCleared(WeakReference<?> ref) {
        while (ref.get() != null) {
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignore) {
            }
        }
    }

    public static void assertEqualsSNBT(String snbt, NBTCompound compound) {
        try {
            final var converted = (NBTCompound) new SNBTParser(new StringReader(snbt)).parse();
            assertEquals(converted, compound);
        } catch (NBTException e) {
            fail(e);
        }
    }

    public static void assertEqualsIgnoreSpace(String s1, String s2, boolean matchCase) {
        final String val1 = stripExtraSpaces(s1);
        final String val2 = stripExtraSpaces(s2);
        if (matchCase) {
            assertEquals(val1, val2);
        } else {
            assertTrue(val1.equalsIgnoreCase(val2));
        }
    }

    public static void assertEqualsIgnoreSpace(String s1, String s2) {
        assertEqualsIgnoreSpace(s1, s2, true);
    }

    public static Player createDummyPlayer() {
        return new Player(UUID.randomUUID(), "test", new PlayerConnection() {
            @Override
            public void sendPacket(@NotNull SendablePacket packet) {

            }

            @Override
            public @NotNull SocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public void disconnect() {

            }
        });
    }

    private static String stripExtraSpaces(String s) {
        StringBuilder formattedString = new StringBuilder();
        java.util.StringTokenizer st = new java.util.StringTokenizer(s);
        while (st.hasMoreTokens()) {
            formattedString.append(st.nextToken());
        }
        return formattedString.toString().trim();
    }
}
