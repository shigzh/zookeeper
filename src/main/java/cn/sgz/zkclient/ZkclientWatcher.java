package cn.sgz.zkclient;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.util.List;

/**
 * @Description: ZKClient里面并没有类似的watcher、watch参数，这也就是说我们开发人员无需关心反复注册watcher的问题，
 * zkclient给我们提供了一套监听方式，我们可以使用监听节点的方式进行操作，剔除了繁琐的反复watcher操作、简化了代码的复杂程度
 * @Auther: shigzh
 * @create 2019/6/16 9:30
 */
public class ZkclientWatcher {
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
        //对父节点 添加 监听子节点的变化（子节点的新增）
        zkClient.subscribeChildChanges("/super", new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                System.out.println("==================创建节点事件通知===================");
                System.out.println("parentPath=====>>>"+parentPath);
                System.out.println("currentChilds======>>>"+currentChilds);
            }
        });
        //对父节点 添加 监听子节点的变化（子节点的内容修改，删除）
        zkClient.subscribeDataChanges("/super", new IZkDataListener() {
            @Override
            public void handleDataChange(String path, Object data) throws Exception {
                System.out.println("==================修改节点事件通知===================");
                System.out.println("变更的节点为======>>>"+path+"，变更内容为======>>>"+data);
            }

            @Override
            public void handleDataDeleted(String path) throws Exception {
                System.out.println("==================删除节点事件通知===================");
                System.out.println("删除的节点为======>>>"+path);
            }
        });

        try {
            Thread.sleep(9999999);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
