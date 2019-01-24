package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.service.ManageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author York
 * @create 2018-11-03 23:34
 */
@RestController
public class SpuManageController {
    @Reference
    private ManageService manageService;

    @RequestMapping("spuList")
    public List<SpuInfo> spuList(SpuInfo spuInfo){
        List<SpuInfo> spuInfoList = manageService.getSpuInfoList(spuInfo);
        return spuInfoList;
    }

    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){
        //创建保存方法保存数据
        manageService.saveSpuInfo(spuInfo);
        return "success";

    }

    @RequestMapping("skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo> skuInfoListBySpu(String spuId){
       List<SkuInfo> skuInfoList = manageService.getskuInfoListBySpu(spuId);
        return skuInfoList;
    }


}
