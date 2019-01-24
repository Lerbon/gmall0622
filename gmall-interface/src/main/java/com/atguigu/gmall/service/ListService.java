package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;

/**
 * @author York
 * @create 2018-11-11 19:55
 */
public interface ListService {
    public void saveSkuInfo(SkuLsInfo skuLsInfo);

    public SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 将商品按照热度排序的方法
     * @param skuId
     */
    public void incrHotScore(String skuId);
}
