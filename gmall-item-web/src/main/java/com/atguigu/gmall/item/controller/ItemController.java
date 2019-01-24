package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author York
 * @create 2018-11-07 18:13
 */
@Controller
public class ItemController {
    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;


    @RequestMapping("{skuId}.html")
    @LoginRequire(autoRedirect = false)
    public String skuInfoPage(@PathVariable(value = "skuId") String skuId, HttpServletRequest request) {
        //存储基本属性的skuInfo信息
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //存储spu,sku的销售属性和销售属性值
        List<SpuSaleAttr> spuSaleAttrList = manageService.selectSpuSaleAttrListCheckBySku(skuInfo);
        //返回获取到了商品销售属性值实现后续的销售属性值切换刷新页面
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        //定义一个字符串(拼成json串返回到前台)
        String valueIdsKey = "";
        //120|130 36   拼成这种形式
        //定义一个map(json串是以k-v的形式存储数值的)
        HashMap<String, Object> map1 = new HashMap<>();
        //拼接字符串
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            if (valueIdsKey.length()!=0){
                valueIdsKey+="|";
            }
            valueIdsKey += skuSaleAttrValue.getSaleAttrValueId();
            if ((i+1)==skuSaleAttrValueListBySpu.size()||!skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())){
                map1.put(valueIdsKey,skuSaleAttrValue.getSkuId());
                valueIdsKey="";
            }
        }
        //将map转化为json串
        String valuesSkuJson = JSON.toJSONString(map1);
        System.out.println(valuesSkuJson);
        // 存储到页面上 ，然后通过js 来 先获取页面的选中的属性值， 使得选中的值，跟valuesSkuJson 做匹配。
        request.setAttribute("valuesSkuJson",valuesSkuJson);

        // 保存spuSaleAttrList 销售属性集合
        request.setAttribute("saleAttrList",spuSaleAttrList);

        request.setAttribute("skuInfo",skuInfo);

        listService.incrHotScore(skuId);
        return "item";
    }


}
