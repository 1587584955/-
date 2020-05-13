package seu.hy.killmall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/base")
public class BaseController {

      @GetMapping("index")
      public String index(){
          return "index";
      }
}
