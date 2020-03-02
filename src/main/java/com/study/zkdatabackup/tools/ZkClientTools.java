package com.study.zkdatabackup.tools;

import com.study.zkdatabackup.entity.PathNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import java.util.HashMap;
import java.util.List;

/**
 * 两个zk直接在线同步
 */
@Slf4j
@Data
public class ZkClientTools {
    private ZooKeeper source;
    private ZooKeeper remote;
    private HashMap<String, PathNode> pathNodeMap;

    /**
     * 全量同步
     */
    public void sync() {
        if (source == null) {
            log.error("源zk客户端为null");
            return;
        }
        if (remote == null) {
            log.error("目标zk客户端为null");
            return;
        }
        if (source.equals(remote)) {
            log.error("源zk与目标zk一样，不进行同步");
            return;
        }
        syncChildPath("/");
    }

    /**
     * 同步路径下的子路径，以及当前路径的节点数据
     *
     * @param path
     */
    public void syncChildPath(String path) {
        List<String> childPath = getChildPath(path);
        syncChildData(path);
        for (String temp : childPath) {
            String pathName = "";
            if (!path.equals("/")) {
                pathName = path + "/" + temp;
            } else {
                pathName = "/" + temp;
            }
            syncChildPath(pathName);
        }
    }

    /**
     * 同步数据节点
     *
     * @param path
     */
    public void syncChildData(String path) {
        PathNode pathNode = getData(path);
        try {
            remote.create(path, pathNode.getData().getBytes(), pathNode.getAcls(), CreateMode.PERSISTENT);
        } catch (Exception e) {

        }
    }

    /**
     * 同步路径数据
     *
     * @param path
     * @return
     */
    public PathNode getData(String path) {
        PathNode pathNode = null;
        try {
            byte[] datas = source.getData(path, null, null);
            List<ACL> acls = source.getACL(path, null);
            pathNode = new PathNode();
            pathNode.setPath(path);
            if (datas != null && datas.length != 0) {
                pathNode.setData(new String(datas, "UTF-8"));
            }
            if (acls != null && acls.size() != 0) {
                pathNode.setAcls(acls);
            }
        } catch (Exception e) {
            log.error("获取路径{}的数据异常,{}", path, e);
        }
        log.info("获取的node:{}", pathNode);
        return pathNode;
    }

    /**
     * 获取子节点的路径
     *
     * @param path
     * @return
     */
    public List<String> getChildPath(String path) {
        List<String> paths = null;
        try {
            paths = source.getChildren(path, null);
            return paths;
        } catch (Exception e) {
            log.error("获取路径{}的子节点异常,{}", path, e);
        }
        return paths;
    }


    private void addPathNode(String path, byte[] bytes, List<ACL> acls) {
        String value = String.valueOf(bytes);
        if (pathNodeMap.containsKey(path) && pathNodeMap.get(path).equals(value)) {
            log.info("路径{}数据已存在，并且数据一致", path);
            return;
        }
        pathNodeMap.put(path, new PathNode(path, value, acls));
    }


}
