package netty;

import io.netty.util.Recycler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RecycleTest {

    public static Recycler<PlayerObject> RECYCLE;

    @BeforeAll
    public static void setUp() {
        RECYCLE = new Recycler<>() {
            @Override
            protected PlayerObject newObject(Handle<PlayerObject> handle) {
                return new PlayerObject(0L, handle);
            }
        };
    }

    @Test
    public void test() {
        PlayerObject playerObject = RECYCLE.get();
        playerObject.data = "AAA";
        playerObject.recycle();

        PlayerObject secondPlayer = RECYCLE.get();
        Assertions.assertEquals(playerObject, secondPlayer);
    }
}
