package com.chauncy.utils.zk;

import com.chauncy.utils.thread.ThreadUtil;
import org.apache.zookeeper.*;

import java.io.IOException;

public class ZooKeeperManager {

    private static final String ZK_ADDRESS = "172.21.240.55:2181";
    private static ZooKeeper zk;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        zk = new ZooKeeper(ZK_ADDRESS, 30000, watchedEvent -> {
            System.out.println("收到事件：" + watchedEvent);
            if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                System.out.println("zk连接成功");
            }
        });

        zk.create("/demo-node", "demo data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        zk.getData("/demo-node", watchedEvent -> {
            System.out.println("收到事件1：" + watchedEvent);
        }, null);

        zk.setData("/demo-node", "update data".getBytes(), -1);

//        zk.delete("/demo-node", -1);

        ThreadUtil.sleepForceQuietly(1000000);
    }

}
