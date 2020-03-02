package com.study.zkdatabackup.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * zookeeper节点数据信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PathNode {
    private String path;
    private String data;
    private List<ACL> acls;
}
