package cn.chauncy.manager;

import cn.chauncy.disruptor.GameTickProvider;
import cn.chauncy.gameplay.scene.SceneManager;
import com.google.inject.Inject;

public class GameManager implements GameTickProvider {

    private final SceneManager sceneManager;

    @Inject
    public GameManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void tick() {
        sceneManager.tickAllScenes();
    }
}
