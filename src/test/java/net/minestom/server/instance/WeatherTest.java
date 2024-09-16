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

        instance.setWeather(new Weather(1, 0.5f), 1);
        instance.tick(0);

        // Weather sent on instance join
        var connection = env.createConnection();
        var tracker = connection.trackIncoming(ChangeGameStatePacket.class);
        connection.connect(instance, new Pos(0, 0, 0));
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
        instance.setWeather(new Weather(0, 0), 2);
        instance.tick(0);
        state = tracker2.collect().get(0);
        assertEquals(ChangeGameStatePacket.Reason.RAIN_LEVEL_CHANGE, state.reason());
        assertEquals(0.5f, state.value());
    }
}
