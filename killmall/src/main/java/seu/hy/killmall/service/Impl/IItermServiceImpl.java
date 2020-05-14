package seu.hy.killmall.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seu.hy.killmall.mapper.ItemKillMapper;
import seu.hy.killmall.pojo.ItemKill;
import seu.hy.killmall.service.IItemService;

import java.util.List;

@Service
public class IItermServiceImpl implements IItemService {

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Override
    public List<ItemKill> getKillItems() throws Exception {
        return itemKillMapper.selectAll();
    }

    @Override
    public ItemKill getKillDetail(Integer id) throws Exception {
        return itemKillMapper.selectById(id);
    }
}
