package net.minestom.server.utils;

import net.minestom.server.utils.binary.BinaryBuffer;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = "1", expect = ACCEPTABLE)
@Outcome(id = "2", expect = ACCEPTABLE)
@State
public class ObjectPoolTest {
    private final ObjectPool<BinaryBuffer> pool = ObjectPool.BUFFER_POOL;

    @Actor
    public void actor1() {
        var buffer = pool.get();
        pool.add(buffer);
    }

    @Actor
    public void actor2() {
        var buffer = pool.get();
        pool.add(buffer);
    }

    @Arbiter
    public void arbiter(L_Result r) {
        r.r1 = pool.count();
    }
}
