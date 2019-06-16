package cn.sgz.zkorder;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.util.concurrent.CountDownLatch;

/**
 * @Description:使用zookeeper实现分布式锁
 * @Auther: shigzh
 * @create 2019/6/16 10:06
 */
public class ZookeeperLock{

    /**
     * 集群连接地址
     */
    private String CONNECT_ADDR = "192.168.184.131:2181,192.168.184.132:2181,192.168.184.133:2181";
    /**
     * session超时时间(心跳检测时间周期)
     */
    private int SESSION_OUTTIME = 2000;
    /**
     * 创建zk连接
     */
    private ZkClient zkClient = new ZkClient(new ZkConnection(CONNECT_ADDR, SESSION_OUTTIME));

    /**
     * 信号量,阻塞程序执行,用户等待zookeeper连接成功,发送成功信号，
     */
    private CountDownLatch countDownLatch = null;

    /**
     * 创建临时节点路径
     */
    public String PATH = "/lock";


    public void lock() {
        if(getLock()){
            System.out.println("##获取分布式锁======》》》lock锁的资源####");
        }else{
            // 等待其他线程处理完
            waitLock();
            // 其他线程处理完了，该轮到我了吧，我还一直在等着呢；继续获取锁并上锁
            lock();
        }
    }

    public void unlock() {
        if(zkClient!=null){
            //客户端关闭链接，临时节点自动删除
            zkClient.close();
            System.out.println("=============zk 关闭链接=============");
        }
    }

    /**
     * 获取锁
     * @return 节点创建成功返回true，创建失败返回false
     */
    private boolean getLock() {
       try{
           zkClient.createEphemeral(PATH);
           return true;
       }catch (Exception e){
           return false;
       }
    }
    /**
     * 等待
     * @return 节点创建成功返回true，创建失败返回false
     */
    private void  waitLock() {
        IZkDataListener zkDataListener = new IZkDataListener() {//监听事件这里必须放在await（）前面，与业务逻辑没有关系
            @Override
            public void handleDataChange(String path, Object data) throws Exception {
            }

            @Override
            public void handleDataDeleted(String path) throws Exception {
                // 唤醒被等待的线程
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                }
            }
        };
        try{
            //对父节点 添加 监听子节点的变化（子节点的内容修改，删除）
            zkClient.subscribeDataChanges(PATH, zkDataListener);
            //业务逻辑
            if(zkClient.exists(PATH)){
                //这个临时节点已经存在，不能再创建了，只能等待
                countDownLatch = new CountDownLatch(1);
            }
            if(countDownLatch!=null){
                countDownLatch.await();//等待
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        // 删除监听
        zkClient.unsubscribeDataChanges(PATH, zkDataListener);
    }
}
