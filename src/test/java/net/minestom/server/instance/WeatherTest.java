package net.minestom.server.instance;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnvTest
public class WeatherTest {
    @Test
    public void weatherTest(Env env) {
        var instance = env.createFlatInstance();

        // Defaults
        Weather weather = instance.getWeather();
        assertFalse(weather.isRaining());
        assertEquals(0, weather.rainLevel());
        assertEquals(0, weather.thunderLevel());

        instance.setWeather(new Weather(true, 1, 0.5f));

        // Weather sent on instance join
        var connection = env.createConnection();
        var tracker = connection.trackIncoming(ChangeGameStatePacket.class);
        connection.connect(instance, new Pos(0, 0, 0)).join();
        tracker.assertCount(4);
        List<ChangeGameStatePacket> packets = tracker.collect();
        var state = packets.get(0);
        assertEquals(ChangeGameStatePacket.Reason.BEGIN_RAINING, state.reason());

        state = packets.get(1);
        assertEquals(ChangeGameStatePacket.Reason.RAIN_LEVEL_CHANGE, state.reason());
        assertEquals(1, state.value());

        state = packets.get(2);
        assertEquals(ChangeGameStatePacket.Reason.THUNDER_LEVEL_CHANGE, state.reason());
        assertEquals(0.5f, state.value());

        // Weather change while inside instance
        var tracker2 = connection.trackIncoming(ChangeGameStatePacket.class);
        instance.setWeather(instance.getWeather().withRain(false));
        tracker2.assertSingle(ChangeGameStatePacket.class, packet -> {
            assertEquals(ChangeGameStatePacket.Reason.END_RAINING, packet.reason());
        });
    }
}
