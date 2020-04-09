package com.study.zkdatabackup;

import com.study.zkdatabackup.tools.ZkClientDataTextTools;
import com.study.zkdatabackup.tools.ZkClientTools;
import com.study.zkdatabackup.zkclient.ZkClient;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.*;
import java.io.File;

@SpringBootApplication
public class ZkDataBackupApplication {

    public static void main(String[] args) throws Exception {

        SpringApplication.run(ZkDataBackupApplication.class, args);
        if (args != null && args.length == 3 && args[0].equals("sync")) {
            ZkClient zkClient = new ZkClient();
            zkClient.setZookeeper_server(args[1]);
            ZooKeeper source = zkClient.createZooKeeper();
            zkClient.setZookeeper_server(args[2]);
            ZooKeeper remote = zkClient.createZooKeeper();
            ZkClientTools zkClientTools = new ZkClientTools();
            zkClientTools.setSource(source);
            zkClientTools.setRemote(remote);
            zkClientTools.sync();
        }
        if (args != null && args[0].equals("backup")) {
            ZkClient zkClient = new ZkClient();
            zkClient.setZookeeper_server(args[1]);
            ZooKeeper source = zkClient.createZooKeeper();
            ZkClientTools zkClientTools = new ZkClientTools();
            zkClientTools.setSource(source);
            ZkClientDataTextTools zkClientDataTextTools = new ZkClientDataTextTools();
            zkClientDataTextTools.setSource(source);
            zkClientDataTextTools.setZkClientTools(zkClientTools);
            if (args.length == 3) {
                zkClientDataTextTools.setFile(new File(args[2]));
            }
            zkClientDataTextTools.backupAll();
        }
        if (args != null && args[0].equals("recovery")) {
            ZkClient zkClient = new ZkClient();
            zkClient.setZookeeper_server(args[1]);
            ZooKeeper source = zkClient.createZooKeeper();
            ZkClientTools zkClientTools = new ZkClientTools();
            zkClientTools.setSource(source);
            ZkClientDataTextTools zkClientDataTextTools = new ZkClientDataTextTools();
            zkClientDataTextTools.setZkClientTools(zkClientTools);
            zkClientDataTextTools.setSource(source);
            zkClientDataTextTools.setFile(new File(args[2]));
            zkClientDataTextTools.recovery();
        }
    }
}
