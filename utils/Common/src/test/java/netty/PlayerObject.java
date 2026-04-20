package netty;

import io.netty.util.Recycler;

import java.util.Objects;

public class PlayerObject {

    public long id;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerObject that)) return false;
        return id == that.id && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data);
    }
}