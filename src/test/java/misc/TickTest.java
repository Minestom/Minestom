package misc;

import net.minestom.server.utils.time.Tick;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TickTest {

    @Test
    public void testTicks() {
        assertEquals(50, Duration.of(1, Tick.CLIENT).toMillis());
        assertEquals(100, Duration.of(2, Tick.CLIENT).toMillis());
        assertEquals(0, Tick.CLIENT.fromDuration(Duration.ofMillis(0)));
        assertEquals(0, Tick.CLIENT.fromDuration(Duration.ofMillis(10)));
        assertEquals(1, Tick.CLIENT.fromDuration(Duration.ofMillis(60)));
        assertEquals(2, Tick.CLIENT.fromDuration(Duration.ofMillis(100)));
    }
}
