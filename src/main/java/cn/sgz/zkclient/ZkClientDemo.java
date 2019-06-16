package cn.sgz.zkclient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

/**
 * @Description: zookeeper框架zkClient
 * @Auther: shigzh
 * @create 2019/6/16 9:05
 */
public class ZkClientDemo {
    /**
     * 集群连接地址
     */
    private static final String CONNECT_ADDR = "192.168.184.131:2181,192.168.184.132:2181,192.168.184.133:2181";
    /**
     * session超时时间(心跳检测时间周期)
     */
    private static final int SESSION_OUTTIME = 2000;

    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient(new ZkConnection(CONNECT_ADDR, SESSION_OUTTIME));

        //1.创建节点
       // zkClient.createPersistent("/super/dd", "您好啊");
        zkClient.createPersistent("/super/ee");//值为null
        //zkClient.createEphemeral("/sgzt");
        //zkClient.createEphemeral("/sgzt1","ssa");

        //删除节点
        //zkClient.deleteRecursive("/super");//递归删除节点


    }
}
