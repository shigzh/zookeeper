package cn.sgz.zookeeper;

/**
 * @Description:
 * @Auther: shigzh
 * @create 2019/6/15 17:48
 */
public class ZookeepTest {
    public static void main(String[] args) {
        ZookeeperUtil.createNode("/shigzh4", "hello world");
        ZookeeperUtil.updateNode("/shigzh4", "hello world122424");
        ZookeeperUtil.deleteNode("/shigzh4");
    }
}
