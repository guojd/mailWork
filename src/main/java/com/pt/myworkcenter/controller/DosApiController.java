package com.pt.myworkcenter.controller;

import com.pt.myworkcenter.pojo.Response;
import com.pt.myworkcenter.service.DosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Controller
@CrossOrigin(originPatterns = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@RequestMapping("/api/v1/dos")
public class DosApiController {
    @Autowired
    DosService dosService;

    @PostMapping("/{setType}")
    @ResponseBody
    public Object setData(@PathVariable("setType") String setType, @RequestBody Map<String,Object> params){
        Response response=new Response();
        switch (setType){
            case "get":
                List<Map<String,Object>> results=dosService.getNetsh(params);
                response.setCount(results.size());
                response.setData(results);
                break;
            case "add":
                response.setMsg(dosService.add(params));
                break;
            case "delete":
                response.setMsg(dosService.del(params));
                break;
            default:
                break;
        }
        return response;
    }
}
