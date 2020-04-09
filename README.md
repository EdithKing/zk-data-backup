# zk-data-backup
同步，备份，以及恢复一个zk上的内容

> ### 可使用目的如下
  * 备份zk信息到xml文件
  * 通过xml中的zk信息同步到zk上
  * 直接同步两个zk的信息


### 步骤
1. 先将源码打包成jar
2. 运行jar,根据运行填写的参数进行不同的操作
3. 同步两个zk信息 java -jar zk-data-backup-0.0.1-SNAPSHOT.jar sync zk1的ip：port zk2的ip：port
4. 备份zk信息到xml文件 java -jar zk-data-backup-0.0.1-SNAPSHOT.jar backup zk的ip:port
5. 将xml的信息同步到zk java -jar zk-data-backup-0.0.1-SNAPSHOT.jar recovery zk的ip:port xml文件信息


