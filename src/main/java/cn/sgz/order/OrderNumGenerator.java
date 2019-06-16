package cn.sgz.order;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 生成订单编号
 * @Auther: shigzh
 * @create 2019/6/15 21:25
 */
public class OrderNumGenerator {

    //全局订单id
    public static int count = 0;//类的所有实例共享同一个static变量;它不依赖类特定的实例，被类的所有实例共享

    public String getNumber() {
        try {
            Thread.sleep(200);
        } catch (Exception e) {
        }
        SimpleDateFormat simpt = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpt.format(new Date()) + "-" + ++count;
    }
}
