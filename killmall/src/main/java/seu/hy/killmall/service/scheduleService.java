package seu.hy.killmall.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import seu.hy.killmall.mapper.ItemKillSuccessMapper;
import seu.hy.killmall.pojo.ItemKillSuccess;

import java.util.Date;
import java.util.List;

@Service
public class scheduleService {

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;
    @Autowired
    private Environment env;
    /**
     *定时任务服务
     *定时查询数据库，修改那些已经超时的任务
     */
    @Scheduled(cron = "0/59 * * * * ? ")
    public void scheduleExpireOrders(){
        try {
            List<ItemKillSuccess> list = itemKillSuccessMapper.selectExpireOrders();
            System.out.println(list);
            for (ItemKillSuccess item : list) {
                //创建订单的时间超过阈值，如果该订单没有被更新，则更新
                if(item.getDiffTime()>env.getProperty("scheduler.expire.orders.time",Integer.class)){
                    System.out.println(item);
                    int res=itemKillSuccessMapper.expireOrder(item.getCode());
                    System.out.println(res);
                }
            }
            System.out.println("执行定时任务成功"+Thread.currentThread()+(new Date()).toString());
        }catch (Exception e){
            System.out.println("定时任务服务"+e);
        }
    }
}
