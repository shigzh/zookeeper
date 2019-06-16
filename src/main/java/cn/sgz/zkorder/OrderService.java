package cn.sgz.zkorder;

import cn.sgz.order.OrderNumGenerator;

/**
 * @Description:使用分布式锁生成订单号
 * @Auther: shigzh
 * @create 2019/6/15 21:27
 */
public class OrderService implements Runnable{

    private OrderNumGenerator orderNumGenerator = new OrderNumGenerator();

    //private Lock lock = new ReentrantLock();

    private ZookeeperLock lock =new ZookeeperLock();

    public void run() {
        getNumber();
    }

    public void getNumber() {
        try {
            lock.lock();
            String number = orderNumGenerator.getNumber();
            System.out.println(Thread.currentThread().getName() + ",生成订单ID:" + number);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //释放锁
            lock.unlock();
        }

    }

    public static void main(String[] args) {
        System.out.println("####生成唯一订单号###");
        for (int i = 0; i < 100; i++) {
            new Thread(new OrderService()).start();
        }

    }
}
