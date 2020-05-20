package seu.hy.killmall.service.Impl;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.hy.killmall.enums.SysConstant;
import seu.hy.killmall.mapper.ItemKillMapper;
import seu.hy.killmall.mapper.ItemKillSuccessMapper;
import seu.hy.killmall.pojo.ItemKill;
import seu.hy.killmall.pojo.ItemKillSuccess;
import seu.hy.killmall.service.EmailSenderService;
import seu.hy.killmall.service.KillService;
import seu.hy.killmall.utils.SnowFlake;

@Service
public class KillServiceImpl implements KillService {

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private EmailSenderService emailSenderService;

    SnowFlake snowFlake=new SnowFlake(2,3);

    @Override
    public Boolean killitem(Integer killId, Integer userId) throws Exception{
        //检测用户是否已经抢购过商品
        Boolean rusult=false;
        System.out.println(2);
        if(itemKillSuccessMapper.countByKillUserId(killId, userId)<=0){
            //检测库存是否充足
            ItemKill itemKill = itemKillMapper.selectById(killId);
            if(itemKill!=null && itemKill.getCanKill()==1){
                //扣减库存
                int res = itemKillMapper.updateKillItem(killId);

                if(res>0){
                    //如果扣减库存成功的话 就创建秒杀成功订单
                    commonRecordKillSuccessInfo(itemKill,userId);
                    return true;
                }
            }
        }else {
            throw new Exception("您已经抢购过该商品");
        }
        return rusult;
    }
    //通用方法--记录用户秒杀成功后生成的订单编号
    @Override
    public void commonRecordKillSuccessInfo(ItemKill kill, Integer userId) {
        //记录抢购成功后生成的秒杀订单编号
        System.out.println(3);
        String orderNo =String.valueOf(snowFlake.nextId());
        ItemKillSuccess entry =new ItemKillSuccess();
        entry.setCode(orderNo);
        entry.setCreateTime(DateTime.now().toDate());
        entry.setItemId(kill.getItemId());
        entry.setUserId(userId.toString());
        entry.setKillId(kill.getId());
        entry.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        System.out.println(entry);
        int res = itemKillSuccessMapper.insertSelective(entry);
        if(res>0){
            emailSenderService.senderKillSuccessEmail(orderNo);
        }
    }


}
