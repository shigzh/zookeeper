package cn.sgz.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * @Description:对zookeeper进行封装
 * @Auther: shigzh
 * @create 2019/6/15 17:31
 */
public class ZookeeperUtil {
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

    private static ZooKeeper zk;

    /**
     * 创建连接
     * @return ZooKeeper
     */
    public static void createConnection() {
        try {
             zk = new ZooKeeper(CONNECT_ADDR, SESSION_OUTTIME, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    // 获取事件的状态
                    Event.KeeperState keeperState = watchedEvent.getState();
                    //获取事件的类型
                    Event.EventType eventType = watchedEvent.getType();
                    //zk节点路径
                    String path = watchedEvent.getPath();
                    // 如果是建立连接
                    if (Event.KeeperState.SyncConnected == keeperState) {
                        if (Event.EventType.None == eventType) {
                            // 如果建立连接成功,则发送信号量,让后续阻塞程序向下执行（这里是主线程）
                            countDownLatch.countDown();//countDownLatch值-1
                            System.out.println("zk========》建立连接");
                        }else if (Event.EventType.NodeCreated  == eventType) {
                            System.out.println("节点新增事件通知====》新增node节点：" + path);
                        }else if (Event.EventType.NodeDataChanged   == eventType) {
                            System.out.println("节点内容修改事件通知====》修改node节点：" + path);
                        }else if (Event.EventType.NodeDeleted   == eventType) {
                            System.out.println("节点删除事件通知====》删除node节点：" + path);
                        }
                    }else  if (Event.KeeperState.Disconnected == keeperState) {
                        System.out.println("zk========》断开连接");
                    }else  if (Event.KeeperState.Expired == keeperState) {
                        System.out.println("zk========》会话超时");
                    }else  if (Event.KeeperState.AuthFailed == keeperState) {
                        System.out.println("zk========》权限检查失败");
                    }

                }
            });
            //主线程进行阻塞，为了不让主线程执行完
            countDownLatch.await();//countDownLatch=0的时候不阻塞
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建节点
     * @param path 节点路径
     * @param data 节点关联数据内容
     * @return
     */
    public static boolean createNode(String path, String data) {
        try {
            createConnection();
            //一般都是在操作节点之前，exists(‘’,true),设置true，watch一下。因为watch是一次性的，exists(‘’,true)只一次有效
            exists(path, true);//节点监听通知
            zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if(zk!=null){
                    zk.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 更新节点
     * @param path 节点路径
     * @param data 节点关联数据内容
     * @return
     */
    public static boolean updateNode(String path,String data){
        try {
            createConnection();
            //一般都是在操作节点之前，exists(‘’,true),设置true，watch一下。因为watch是一次性的，exists(‘’,true)只一次有效
            exists(path, true);
            zk.setData(path, data.getBytes(), -1);//-1表示跳过版本检查
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(zk!=null){
                    zk.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 删除节点
     * @param path 节点路径
     * @return
     */
    public static boolean deleteNode(String path){
        try {
            createConnection();
            //一般都是在操作节点之前，exists(‘’,true),设置true，watch一下。因为watch是一次性的，exists(‘’,true)只一次有效
            exists(path, true);
            zk.delete(path, -1);//-1表示跳过版本检查
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(zk!=null){
                    zk.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 判断指定节点是否存在
     * @param path 节点路径
     * @param needWatch 是否需要监听通知
     */
    public static Stat exists(String path, boolean needWatch) {
        try {
            return zk.exists(path, needWatch);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
