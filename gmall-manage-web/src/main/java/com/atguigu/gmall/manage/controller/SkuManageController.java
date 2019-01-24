package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author York
 * @create 2018-11-06 18:41
 */
@Controller
public class SkuManageController {
    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId) {
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return spuImageList;
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
        return manageService.getSpuSaleAttrList(spuId);
    }

    @ResponseBody
    @RequestMapping("saveSku")
    public void saveSku(SkuInfo skuInfo) {
        manageService.saveSku(skuInfo);

    }

    @RequestMapping(value = "onSale", method = RequestMethod.GET)
    @ResponseBody
    public void onSale(String skuId) {
        //先通过skuId查询获取对应的skuInfo对象
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //再将查询获得的skuInfo对象的对应属性值赋值给skuLsInfo对象
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        //属性拷贝
        BeanUtils.copyProperties(skuInfo, skuLsInfo);
        listService.saveSkuInfo(skuLsInfo);


    }


}
