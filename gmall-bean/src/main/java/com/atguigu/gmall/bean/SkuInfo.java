package com.atguigu.gmall.bean;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author York
 * @create 2018-11-06 18:10
 */
public class SkuInfo implements Serializable {
    /**
     * 商品id,主键自增
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    String id;

    /**
     * spuId
     */
    @Column
    String spuId;

    /**
     * 商品价格
     */
    @Column
    BigDecimal price;

    /**
     * 商品名称
     */
    @Column
    String skuName;

    /**
     * 商品重量
     */
    @Column
    BigDecimal weight;

    /**
     * 商品描述
     */
    @Column
    String skuDesc;

    /**
     * 三级分类id
     */
    @Column
    String catalog3Id;

    /**
     * 商品默认显示图片
     */
    @Column
    String skuDefaultImg;

    /**
     * 商品图片列表
     */
    @Transient
    List<SkuImage> skuImageList;

    /**
     * 商品属性值列表
     */
    @Transient
    List<SkuAttrValue> skuAttrValueList;

    /**
     * 商品销售属性值列表
     */
    @Transient
    List<SkuSaleAttrValue> skuSaleAttrValueList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpuId() {
        return spuId;
    }

    public void setSpuId(String spuId) {
        this.spuId = spuId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getSkuDesc() {
        return skuDesc;
    }

    public void setSkuDesc(String skuDesc) {
        this.skuDesc = skuDesc;
    }

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public String getSkuDefaultImg() {
        return skuDefaultImg;
    }

    public void setSkuDefaultImg(String skuDefaultImg) {
        this.skuDefaultImg = skuDefaultImg;
    }

    public List<SkuImage> getSkuImageList() {
        return skuImageList;
    }

    public void setSkuImageList(List<SkuImage> skuImageList) {
        this.skuImageList = skuImageList;
    }

    public List<SkuAttrValue> getSkuAttrValueList() {
        return skuAttrValueList;
    }

    public void setSkuAttrValueList(List<SkuAttrValue> skuAttrValueList) {
        this.skuAttrValueList = skuAttrValueList;
    }

    public List<SkuSaleAttrValue> getSkuSaleAttrValueList() {
        return skuSaleAttrValueList;
    }

    public void setSkuSaleAttrValueList(List<SkuSaleAttrValue> skuSaleAttrValueList) {
        this.skuSaleAttrValueList = skuSaleAttrValueList;
    }
}
