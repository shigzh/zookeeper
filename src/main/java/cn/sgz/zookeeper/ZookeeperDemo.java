package cn.sgz.zookeeper;

import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

/**
 * @Description: zookeepre小demo实例
 * @Auther: shigzh
 * @create: 2019/6/15 16:15
 */
public class ZookeeperDemo {
    /**
     * 集群连接地址
     */
    private static final String CONNECT_ADDR = "192.168.184.131:2181,192.168.184.132:2181,192.168.184.133:2181";
    /**
     * session超时时间(心跳检测时间周期)
     */
    private static final int SESSION_OUTTIME = 2000;
    /**
     * 信号量,阻塞程序执行,用户等待zookeeper连接成功,发送成功信号，
     */
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        ZooKeeper zk = null;
        try {
             zk = new ZooKeeper(CONNECT_ADDR, SESSION_OUTTIME, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    // 获取事件的状态
                    Event.KeeperState keeperState = watchedEvent.getState();
                    //获取事件的类型
                    Event.EventType eventType = watchedEvent.getType();
                    // 如果是建立连接
                    if (Event.KeeperState.SyncConnected == keeperState) {
                        if (Event.EventType.None == eventType) {
                            // 如果建立连接成功,则发送信号量,让后续阻塞程序向下执行（这里是主线程）
                            countDownLatch.countDown();//countDownLatch值-1
                            System.out.println("zk 建立连接");
                        }
                    }
                }
            });
            //主线程进行阻塞，为了不让主线程执行完
            countDownLatch.await();//countDownLatch=0的时候不阻塞

            //创建父节点
            //String parentNode = zk.create("/sgz", "test".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		    //System.out.println("result:" + parentNode);
            //创建子节点
            //String childrenNode = zk.create("/sgz/aa", "children 12245465".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            //ZooDefs.Ids.READ_ACL_UNSAFE：创建一个只读权限的节点，在这个节点下就不能再创建子节点，否则报错Authentication is not valid : /sgz/aab/cc
            //ZooDefs.Ids下有3个值：OPEN_ACL_UNSAFE，CREATOR_ALL_ACL，OPEN_ACL_UNSAFE(常用)
            String childrenNode = zk.create("/shigzh", "children 12245465".getBytes(), ZooDefs.Ids.READ_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("result:"+childrenNode);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(zk!=null){
                    zk.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
