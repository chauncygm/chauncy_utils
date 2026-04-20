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
        playerObject.id = 101L;
        playerObject.data = "AAA";
        playerObject.recycle();

        PlayerObject secondPlayer = RECYCLE.get();
        playerObject.id = 202L;
        secondPlayer.data = "BBB";
        Assertions.assertSame(playerObject, secondPlayer);
        Assertions.assertEquals(playerObject, secondPlayer);
    }
}
