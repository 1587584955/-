package seu.hy.killmall.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.hy.killmall.mapper.ItemKillMapper;
import seu.hy.killmall.mapper.ItemKillSuccessMapper;
import seu.hy.killmall.pojo.ItemKill;
import seu.hy.killmall.service.KillService;

@Service
public class KillServiceImpl implements KillService {

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;


    @Override
    public Boolean killitem(Integer killId, Integer userId) throws Exception{
        //检测用户是否已经抢购过商品
        Boolean rusult=false;
        if(itemKillSuccessMapper.countByKillUserId(killId, userId)<=0){
            //检测库存是否充足
            ItemKill itemKill = itemKillMapper.selectById(killId);
            if(itemKill!=null && itemKill.getCanKill()==1){
                //扣减库存
                int res = itemKillMapper.updateKillItem(killId);

                if(res>0){
                    //如果扣减库存成功的话 就创建秒杀成功订单
                    return true;
                }
            }
        }else {

                throw new Exception("您已经抢购过该商品");

        }

        return rusult;
    }
}
