package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

/**
 * @author York
 * @create 2018-11-02 19:29
 */
public interface ManageService {

    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    public BaseAttrInfo getAttrInfo(String attrId);

    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存方法
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /** 根据spuId获取spuImage中的所有图片列表
     *
     */
    List<SpuImage> getSpuImageList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    void saveSku(SkuInfo skuInfo);

    /**
     * 根据skuId查询具体的物品返回页面
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 根据skuInfo查询商品的销售属性和对应的属性值
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * 根据spuId查询获取sku的销售属性值列表
     */
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    /**
     *根据spuId获取该spu下所有skuInfo
     */
    List<SkuInfo> getskuInfoListBySpu(String spuId);

    void removeSpuInfo(String spuId);

    //根据平台属性值Id的集合查询平台属性值的集合
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
