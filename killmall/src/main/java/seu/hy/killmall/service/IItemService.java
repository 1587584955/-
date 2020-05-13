package seu.hy.killmall.service;


import seu.hy.killmall.pojo.ItemKill;

import java.util.List;

/**
 * Created by Administrator on 2019/6/16.
 */
public interface IItemService {

    List<ItemKill> getKillItems() throws Exception;

    ItemKill getKillDetail(Integer id) throws Exception;
}
