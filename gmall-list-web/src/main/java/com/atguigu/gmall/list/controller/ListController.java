package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author York
 * @create 2018-11-12 19:48
 */
@Controller
public class ListController {
    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;
    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams,Map map){
//        skuLsParams.setPageSize(1);
        SkuLsResult search = listService.search(skuLsParams);
        map.put("skuLsInfoList",search.getSkuLsInfoList());
        //从结果中取出平台属性值列表
        List<String> attrValueIdList = search.getAttrValueIdList();
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);

        //已选的属性值列表
        List<BaseAttrValue> baseAttrValuesList = new ArrayList<>();
        String urlParam = makeUrlParam(skuLsParams);
        //由于要将已选中的属性和对应的属性值从页面中移除,所以应该使用迭代器在进行迭代的过程中进行移除
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo next =  iterator.next();
            List<BaseAttrValue> attrValueList = next.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setUrlParam(urlParam);
                if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
                    for (String valueId : skuLsParams.getValueId()) {
                        //判断选中的属性值是否存在于查询结果的属性值中,存在的话直接将attrList中对应的属性和属性值移除
                        if (valueId.equals(baseAttrValue.getId())){
                            iterator.remove();
                            //构造面包屑
                            BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                            //面包屑组成:属性名+属性值名
                            baseAttrValueSelected.setValueName(next.getAttrName()+":"+baseAttrValue.getValueName());
                            //重新加载url将原来的valueId去掉并显示到平台属性列表上
                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);
                            baseAttrValueSelected.setUrlParam(makeUrlParam);
                            baseAttrValuesList.add(baseAttrValueSelected);
                        }
                    }
                }
            }

        }
        //分页
        map.put("pageNo",skuLsParams.getPageNo());
        //总页数
        map.put("totalPages",search.getTotalPages());
        //保存面包屑
        map.put("baseAttrValuesList",baseAttrValuesList);
        //保存关键字
        map.put("keyword",skuLsParams.getKeyword());
        //保存urlparam
        map.put("urlParam",urlParam);
        //保存平台属性值
        map.put("attrList",attrList);
        //获得sku属性值列表
        List<SkuLsInfo>skuLsInfoList=search.getSkuLsInfoList();
        map.put("skuLsInfoList",skuLsInfoList);
        return "list";
    }

    /**
     * 制作url,点击平台属性时跳转页面
     * @param skuLsParams
     * @return
     */
    public String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds){
        String urlParam="";
        //接收的参数里面有关键词
        if (skuLsParams.getKeyword()!=null){
            urlParam += "keyword="+skuLsParams.getKeyword();
        }
        if (skuLsParams.getCatalog3Id()!=null){
            if (urlParam.length()>0){
                urlParam+="&";
            }
            urlParam += "catalog3Id="+skuLsParams.getCatalog3Id();
        }
        //构建属性参数
        if (skuLsParams.getValueId()!=null){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if (excludeValueIds!=null&&excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        // 跳出代码，后面的参数则不会继续追加【后续代码不会执行】
                        continue;
                    }

                }
                if (urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }

        }
        return urlParam;

    }

}
