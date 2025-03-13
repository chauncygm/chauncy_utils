package netty;

import io.netty.util.Recycler;

public class PlayerObject {

    private long id;
    private final Recycler.Handle<PlayerObject> handle;

    public String data;

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