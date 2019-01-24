package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author York
 * @create 2018-11-02 18:10
 */
@Controller
public class ManageController {
    @Reference
    private ManageService manageService;


    @RequestMapping("/attrListPage")
    public String attrListPage(){
        return "attrListPage";
    }


    @RequestMapping("/index")
    public String index(){
        return "index";
    }


    /**
     * 获得一级分类
     * @return
     */
    @RequestMapping("getCatalog1")
    @ResponseBody
    public Object getCatalog1(){
        List<BaseCatalog1> catalog1List = manageService.getCatalog1();
        return catalog1List;
    }

    /**
     * 获得二级分类数据
     *
     */
    @RequestMapping("getCatalog2")
    @ResponseBody
    public Object getCatalog2(String catalog1Id){
        List<BaseCatalog2> catalog2List = manageService.getCatalog2(catalog1Id);
        return catalog2List;

    }

    /**
     * 获得三级分类数据
     */
    @RequestMapping("getCatalog3")
    @ResponseBody
    public Object getCatalog3(String catalog2Id){
        List<BaseCatalog3> catalog3List = manageService.getCatalog3(catalog2Id);
        return catalog3List;
    }

    /**
     * 根据三级分类id获取平台属性
     */

    @RequestMapping("attrInfoList")
    @ResponseBody
    public Object attrInfoList(String catalog3Id){
        List<BaseAttrInfo> attrList = manageService.getAttrList(catalog3Id);
        return attrList;

    }

    /**
     * 添加平台属性值
     */
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }


    /**
     * 修改平台属性
     * @param attrId
     * @return
     */
    @RequestMapping(value = "getAttrValueList",method = RequestMethod.POST)
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){
        BaseAttrInfo attrInfo = manageService.getAttrInfo(attrId);
        List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();
        return attrValueList;
    }

    /**
     * 跳转至商品信息管理页面
     * @return
     */
    @RequestMapping("spuListPage")
    public String tospuListPage(){
        return "spuListPage";
    }

    /**
     * 返回销售属性列表
     */
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return manageService.getBaseSaleAttrList();
    }


    /**
     * 根据spuId删除指定的spuInfo
     *
     */
    @RequestMapping("removeSpuInfo")
    @ResponseBody
    public void removeSpuInfo(String spuId){
        manageService.removeSpuInfo(spuId);
    }
}
