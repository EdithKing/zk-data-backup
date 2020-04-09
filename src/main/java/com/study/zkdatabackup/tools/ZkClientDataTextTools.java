package com.study.zkdatabackup.tools;

import com.study.zkdatabackup.entity.PathNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * zk结点的xml文档备份以及恢复
 */
@Data
@Slf4j
public class ZkClientDataTextTools {
    private ZooKeeper source;
    private ZkClientTools zkClientTools;
    private Document document;
    private File file = new File("backup.xml");

    public ZkClientDataTextTools() {
        createDocument();
    }

    /**
     * 创建文档
     */
    public void createDocument() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            this.document = documentBuilder.newDocument();
            document.setXmlStandalone(true);
        } catch (Exception e) {
            log.error("xml解析器有误{}", e);
        }
    }

    /**
     * 备份/下所有数据，并生成xml文件
     */
    public void backupAll() {
        document.appendChild(wirte("/"));
        try {
            TransformerFactory tff = TransformerFactory.newInstance();
            Transformer tf = tff.newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.transform(new DOMSource(document), new StreamResult(file));
        } catch (Exception e) {
            log.error("数据转换成xml文件异常，{}", e);
        }
        log.info("数据已写完");
    }

    /**
     * 将节点数据全部转换成xml文档的内容
     *
     * @param path
     * @return
     */
    public Element wirte(String path) {
        PathNode pathNode = zkClientTools.getData(path);
        Element element = createElement(pathNode);
        List<String> childs = zkClientTools.getChildPath(path);
        for (String childPath : childs) {
            String pathName = "";
            if (!path.equals("/")) {
                pathName = path + "/" + childPath;
            } else {
                pathName = "/" + childPath;
            }
            Element childElemnt = wirte(pathName);
            element.appendChild(childElemnt);
        }
        return element;
    }

    /**
     * 一个节点转换成一个element对象，加入document中
     *
     * @param pathNode
     * @return
     */
    private Element createElement(PathNode pathNode) {
        Element pathElement = document.createElement("path");
        pathElement.setAttribute("pathName", pathNode.getPath());
        if (pathNode.getData() != null) {
            pathElement.setAttribute("data", pathNode.getData());
        }
        if (pathNode.getAcls() != null) {
            for (int i = 0; i < pathNode.getAcls().size(); i++) {
                pathElement.setAttribute("id", pathNode.getAcls().get(i).getId().getId());
                pathElement.setAttribute("schema", pathNode.getAcls().get(i).getId().getScheme());
                pathElement.setAttribute("perms", pathNode.getAcls().get(i).getPerms() + "");
            }
        }
        return pathElement;
    }

    /**
     * 将xml文件内容恢复至zk
     */

    public void recovery() {
        if (file != null && file.exists() && file.getName().lastIndexOf(".xml") != -1) {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                this.document = documentBuilder.parse(file);
                NodeList pathList = document.getElementsByTagName("path");
                for (int i = 0; i < pathList.getLength(); i++) {
                    Element element = (Element) pathList.item(i);
                    String pathName = element.getAttribute("pathName");
                    if (pathName.equals("/")) {
                        continue;
                    }
                    String data = element.getAttribute("data");
                    if (data == null) {
                        data = "";
                    }
                    String id = element.getAttribute("id");
                    String schema = element.getAttribute("schema");
                    String perms = element.getAttribute("perms");
                    ACL acl = new ACL(ZooDefs.Perms.ALL,ZooDefs.Ids.ANYONE_ID_UNSAFE);
                    List<ACL> acls = new ArrayList<ACL>();
                    acls.add(acl);
                    Stat stat = source.exists(pathName, true);
                    if (null == stat) {
                        source.create(pathName, data.getBytes(), acls, CreateMode.PERSISTENT);
                    }
                }
                log.info("数据恢复完成");
            } catch (Exception e) {
                e.printStackTrace();
                log.error("文件读取出现异常" + e);
            }
        } else {
            log.error("文件不存在或者文件名不是xml");
        }
    }

}
