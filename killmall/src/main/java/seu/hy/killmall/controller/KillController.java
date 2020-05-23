package seu.hy.killmall.controller;


import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import seu.hy.killmall.enums.StatusCode;
import seu.hy.killmall.pojo.ItemKill;
import seu.hy.killmall.pojo.KillDto;
import seu.hy.killmall.pojo.KillSuccessUserInfo;
import seu.hy.killmall.respones.BaseResponse;
import seu.hy.killmall.service.IItemService;
import seu.hy.killmall.service.KillService;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/kill")
public class KillController {

    @Autowired
    private KillService killService;
    @Autowired
    private IItemService itemService;

    @RequestMapping(value ="/execute",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse execute(@RequestBody @Validated KillDto killDto, BindingResult result, HttpSession session){
        //首先校验参数是否合法 ,不合法返回无效参数
        if(result.hasErrors()||killDto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        //然后校验用户是否登录
//        Object uid=session.getAttribute("uid");
//        if(uid==null){
//            return new BaseResponse(StatusCode.UserNotLogin);
//        }
        //然后才校验用户是否抢购成功
        BaseResponse bs= new BaseResponse(StatusCode.Success);
        System.out.println("1");
        try{
            Boolean res=killService.killitem(killDto.getKillId(),killDto.getUserId());
            if (!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"商品已经抢购完或者未在时间段");
            }
        }catch (Exception e){
            return new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return bs;
    }
   //抢购成功跳转页面
    @RequestMapping(value = {"success"},method = RequestMethod.GET)
    public String executesuccess(){
        return "executesuccess";
    }
   //抢购失败跳转页面
    @RequestMapping(value={"fail"},method = RequestMethod.GET)
    public String executefail(String msg, Model model){
        model.addAttribute("msg",msg);
        return "executefail";
    }

    //根据订单查询商品信息详情
    @RequestMapping(value = {"/record/detail"})
    public String recorddetail(String code,Model model){
        if(StringUtils.hasLength(code)){
            try {
                KillSuccessUserInfo orderDetail = itemService.getOrderDetail(code);
                model.addAttribute("orderDetail",orderDetail);
                return "killRecord";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "redirect:error";
    }

}
