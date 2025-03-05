package netty;

import io.netty.util.Recycler;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RecycleTest {

    public static final Recycler<PlayerObject> RECYCLE = new Recycler<PlayerObject>() {
        @Override
        protected PlayerObject newObject(Handle<PlayerObject> handle) {
            return new PlayerObject(0L, handle);
        }
    };


    @Test
    public void test() {
        PlayerObject playerObject = RECYCLE.get();

        PlayerObject obj = playerObject;
        playerObject.setData("AAA");
        playerObject.recycle();

        PlayerObject secondPlayer = RECYCLE.get();
        Assertions.assertEquals(obj, secondPlayer);
    }

}

@Data
class PlayerObject {

    private long id;
    private final Recycler.Handle<PlayerObject> handle;

    private String data;

    public PlayerObject(long id, Recycler.Handle<PlayerObject> handle) {
        this.id = id;
        this.handle = handle;
    }

    public void recycle() {
        this.id = 0L;
        this.data = null;
        handle.recycle(this);
    }
}
