package seu.hy.killmall.service;

import seu.hy.killmall.pojo.ItemKill;

public interface KillService {
    public Boolean killitem(Integer killId,Integer userId) throws Exception;
    public void commonRecordKillSuccessInfo(ItemKill kill,Integer userId);
}
