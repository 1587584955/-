package seu.hy.killmall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import seu.hy.killmall.pojo.ItemKill;
import seu.hy.killmall.service.IItemService;

import java.util.List;

/**
 * 秒杀商品展示列表
 *
 * **/
@Controller
public class ItemController {
    @Autowired
    private IItemService itemService;

    @RequestMapping(value = "list")
    public String list(Model model){
        try {
            List<ItemKill> killItems = itemService.getKillItems();
            model.addAttribute("list",killItems);
        }catch (Exception e){
            return "redirect:base/error";
        }

        return  "list";
    }
    @RequestMapping(value = "detail")
    public String detail(Integer id, Model model){
        if(id==null){
            return "redirect:base/error";
        }else {
            try{
                ItemKill killDetail = itemService.getKillDetail(id);
                model.addAttribute("de",killDetail);
                return "info";
            }catch (Exception e){
                System.out.println(e);
                return "redirect:base/error";
            }
        }
    }
}
