package com.study.zkdatabackup.zkclient;

import lombok.Data;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * zookeeper客户端连接类
 */
@Component
@Data
public class ZkClient {

    private final static Logger log = LoggerFactory.getLogger(ZkClient.class);

    private String zookeeper_server = "127.0.0.1:2181";

    private Integer sessionTimeout = 3000;

    private Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            log.info("zk连接出现异常: " + event);
        }
    };

    private Long sessionId = null;

    private byte[] sessionPasswd = null;

    private boolean canBeReadOnly = false;

    /**
     * 创建一个zk
     *
     * @return
     */
    public ZooKeeper createZooKeeper() {
        ZooKeeper zooKeeper = null;
        try {
            if (sessionId == null && sessionPasswd == null) {
                zooKeeper = new ZooKeeper(zookeeper_server, sessionTimeout, watcher);
            } else {
                zooKeeper = new ZooKeeper(zookeeper_server, sessionTimeout, watcher, sessionId, sessionPasswd, canBeReadOnly);
            }
        } catch (IOException e) {
            log.error("主机{}zk客户端连不上，请检查zk是否启动或者是端口不通", zookeeper_server);
        }
        return zooKeeper;
    }
}
